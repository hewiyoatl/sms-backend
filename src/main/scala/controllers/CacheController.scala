package controllers

import java.util
import java.util.Map.Entry
import javax.inject.Inject

import io.swagger.annotations._
import net.sf.ehcache._
import play.api.Logger
import play.api.cache.CacheManagerProvider
import play.api.http.ContentTypes
import play.api.mvc._

/**
 * Class to do all the management related to cache
 *
 * @param cacheManagerProvider
 */
@Api(value = "/caches", protocols = "http, https", produces = "text/plain", consumes = "text/plain")
class CacheController @Inject()(cacheManagerProvider: CacheManagerProvider) extends Controller {
  val PLAY_CACHE = "play"

  def cacheReference: Cache = cacheManagerProvider.get.getCache(PLAY_CACHE)

  def clear = cacheReference.removeAll

  @ApiOperation(
    nickname = "clearCache",
    value = "Clears the mem cache",
    httpMethod = "POST",
    produces = "text/plain")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Cache clear successfully")))
  def clearCache = Action { request =>
    Logger.info("Clear cache invoked")
    clear
    Ok("Clear cache").as(ContentTypes.TEXT)
  }

  @ApiOperation(
    nickname = "listCache",
    value = "List all the mem cache",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of Cache")))
  def listCache = Action { request =>

    Logger.info("List cache invoked")
    val output = listKeys

    Ok(output).as(ContentTypes.TEXT)
  }

  /**
   * Method to get all the keys store on the cache
   * @return
   */
  def listKeys: String = {
    val cache: Cache = cacheReference
    val listKeys: util.List[_] = cache.getKeys
    val elements: util.Map[Object, Element] = cache.getAll(listKeys)
    val iterator: util.Iterator[Entry[Object, Element]] = elements.entrySet().iterator()
    generatedOutput(iterator)
  }

  /**
   * Method to generate a string output of the keys
   * store on cache
   * @param  iterator
   * @return
   */
  def generatedOutput(iterator: util.Iterator[Entry[Object, Element]]): String = {
    val output: StringBuilder = new StringBuilder
    while (iterator.hasNext) {
      val element: Entry[Object, Element] = iterator.next()
      output.append(element.getKey + " " + element.getValue + "    ")
    }
    output.toString
  }
}