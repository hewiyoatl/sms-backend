package controllers

import model.{User, UserOutbound, Users}

import scala.concurrent.Future

object ClientFacade {

  def addUser(user: User): Future[Option[UserOutbound]] = {
    Users.add(user)
  }

  def deleteUser(id: Long): Future[Int] = {
    Users.delete(id)
  }

  def listAllUsers: Future[Seq[UserOutbound]] = {
    Users.listAll
  }

  def retrieveUser(id: Long): Future[Option[UserOutbound]] = {
    Users.retrieveUser(id)
  }

  def patchUser(user: User): Future[Option[UserOutbound]] = {
    Users.patchUser(user)
  }

}
