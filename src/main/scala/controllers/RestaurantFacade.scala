package controllers

import models.{Restaurant, RestaurantOutbound, Restaurants}

import scala.concurrent.Future

object RestaurantFacade {

  def addRestaurant(restaurant: Restaurant): Future[Option[RestaurantOutbound]] = {
    Restaurants.add(restaurant)
  }

  def deleteRestaurant(id: Long): Future[Int] = {
    Restaurants.delete(id)
  }

  def listAllRestaurants: Future[Seq[RestaurantOutbound]] = {
    Restaurants.listAll
  }

  def retrieveRetaurant(id: Long): Future[Option[RestaurantOutbound]] = {
    Restaurants.retrieveRestaurant(id)
  }

  def patchRestaurant(restaurant: Restaurant): Future[Option[RestaurantOutbound]] = {
    Restaurants.patchRestaurant(restaurant)
  }

}
