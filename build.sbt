import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import play.sbt.PlayImport.PlayKeys
import play.sbt.{PlayAkkaHttpServer, PlayLayoutPlugin, PlayNettyServer, PlayScala}
import play.twirl.sbt.Import.TwirlKeys
import sbtassembly.{MergeStrategy, PathList}
import scoverage.ScoverageKeys

name := "sms-backend"

lazy val root = (project in file(".")).settings(
  bashScriptExtraDefines ++= Seq(
    "export LC_ALL=C.UTF-8",
    "export LANG=C.UTF-8"
  )).enablePlugins(PlayScala, PlayNettyServer).disablePlugins(PlayAkkaHttpServer)

disablePlugins(PlayLayoutPlugin)
//PlayKeys.playMonitoredFiles ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value

scalaVersion in ThisBuild := "2.11.8"
//scalaVersion in ThisBuild := "2.13.1"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases/"
resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  ws,
  filters,
  guice,
  ehcache,
  "com.google.crypto.tink" % "tink" % "1.3.0-rc3", //https://github.com/google/tink
  //metrics for database connections
  "nl.grons" %% "metrics-scala" % "3.5.4_a2.3",
  "com.kenshoo" %% "metrics-play" % "2.7.0_0.8.0",
  "io.prometheus" % "simpleclient" % "0.0.16",
  "io.prometheus" % "simpleclient_hotspot" % "0.0.16",
  "io.prometheus" % "simpleclient_servlet" % "0.0.16",
  "io.prometheus" % "simpleclient_pushgateway" % "0.0.16",
  "mysql" % "mysql-connector-java" % "5.1.34",
  specs2 % Test,
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.pauldijou" %% "jwt-play" % "0.19.0")

//unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

// disable .jar publishing
publishArtifact in (Compile, packageBin) := false

//sbtPlugin := true

publishMavenStyle := true

//to remove the reverse files generated from play for the coverage report
ScoverageKeys.coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;kafka\..*.*;"""

//to unable the parallel execution for testing purposes
parallelExecution in Test := false

// for the liquid support

// first read the local file from play application.conf
def getConfig: com.typesafe.config.Config = {
  val classLoader = new java.net.URLClassLoader( Array( new File("./src/main/resources/").toURI.toURL ) )
  ConfigFactory.load(classLoader)
}

maintainer := "info@talachitas.com"

//import com.github.sbtliquibase.SbtLiquibase

//enablePlugins(SbtLiquibase)

//liquibaseUsername := getConfig.getString("db.default.user") // "hespinosa"

//liquibasePassword := getConfig.getString("db.default.password") // "hespinosa"

//liquibaseDriver := getConfig.getString("db.default.driver") // "org.postgresql.Driver"

//liquibaseUrl := getConfig.getString("db.default.url")  //"jdbc:postgresql://localhost:5432/documents?currentSchema=template"

// liquibaseUsername := "postgres"

// liquibasePassword := "postgres"

// liquibaseDriver := "org.postgresql.Driver"

// liquibaseUrl := "jdbc:postgresql://10.1.100.95:5432/rldev8?currentSchema=template"

// Full path to your changelog file. This defaults 'src/main/migrations/changelog.xml'.
// liquibaseChangelog := file("./liquidbase/scripts/changesets/changelog.xml")


//flywayUrl := getConfig.getString("db.default.url")
//
//flywaySchemas := Seq("doc_answer")
//
//flywayUser := getConfig.getString("db.default.user")
//
//flywayPassword := getConfig.getString("db.default.password")
//
//flywaySqlMigrationPrefix := ""
//
//flywayRepeatableSqlMigrationPrefix := ""
//
//flywayLocations := Seq("filesystem:./data/sql")
//
//flywaySqlMigrationSeparator := "_"
//
//flywayTable := "flyway_schema_version"
enablePlugins(FlywayPlugin)
//version := "0.0.1"
//name := "flyway-sbt-test1"

//local
flywayUrl := getConfig.getString("slick.dbs.default.db.url")

//server remote
//flywayUrl := "jdbc:mysql://localhost:13306/talachitas"

flywaySchemas := Seq("talachitas_sms")

flywayUser := getConfig.getString("slick.dbs.default.db.user")

//remote
//flywayUser := "admin"

flywayPassword := getConfig.getString("slick.dbs.default.db.password")

//remote
//flywayPassword := "Kkconqueso1."

//flywaySqlMigrationPrefix := ""

//flywayRepeatableSqlMigrationPrefix := ""

flywayLocations := Seq("filesystem:./data/sql")

//flywaySqlMigrationSeparator := "."

flywayTable := "flyway_schema_version"


// to generate the docker images
enablePlugins(JavaAppPackaging)

//microservice plugin for mini kubernetus
//enablePlugins(DirectoryMicroservice)

// for the standalone jar
assemblyMergeStrategy in assembly := {
  // Building fat jar without META-INF
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  // Take last config file
  case PathList(ps @ _*) if ps.last endsWith ".conf" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.concat
  case PathList("reference-overrides.conf") => MergeStrategy.concat
  case PathList("org", "slf4j", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.first
  case PathList("com", "zaxxer", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", "log4j", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", "commons", "logging", xs @ _*) => MergeStrategy.discard
  // case PathList("org", "joda", "joda-convert", xs @ _*) => MergeStrategy.concat
  // case PathList("javax", "xml", "bind", "jaxb-api", xs @ _*) => MergeStrategy.concat
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x if x.endsWith("application.conf") => MergeStrategy.first
  case x if x.endsWith("spring.tooling") => MergeStrategy.first
  case x if x.endsWith("logback.xml") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {_.data.getName.contains("slf4j-log4j12")}
}

mainClass in assembly := Some("play.core.server.ProdServerStart")


fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
