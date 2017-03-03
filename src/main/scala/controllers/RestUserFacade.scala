package controllers

import models.{RestUser, RestUserOutbound, RestUsers}

import scala.concurrent.Future

object RestUserFacade {

  def addRestUser(restUser: RestUser): Future[Option[RestUserOutbound]] = {
    RestUsers.add(restUser)
  }

  def deleteRestUser(id: Long): Future[Int] = {
    RestUsers.delete(id)
  }

  def listAllRestUsers: Future[Seq[RestUserOutbound]] = {
    RestUsers.listAll
  }

  def retrieveRestUser(id: Long): Future[Option[RestUserOutbound]] = {
    RestUsers.retrieveRestUser(id)
  }

  def patchRestUser(restUser: RestUser): Future[Option[RestUserOutbound]] = {
    RestUsers.patchRestUser(restUser)
  }

}
