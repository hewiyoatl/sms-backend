#### Build Locally 
```sbt run -Dhttp.port=10002```

#### Run Unit/Integration Test Locally 
```sbt test```

#### Debug purposes (also pointed your intellij to the port 9999 on debug mode) Locally
```sbt -jvm-debug 9998 -Dhttp.port=10002 run```
