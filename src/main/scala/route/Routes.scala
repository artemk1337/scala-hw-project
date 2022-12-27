package route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce._
import io.circe.generic.auto._
import model._
import repository._

import java.util.UUID


class Routes(repository: UserRepository) extends FailFastCirceSupport {
  def route: Route = {
    // users list
    (get & path("users")) {
      val list = repository.list()
      complete(list)
    } ~
      (post & path("user")) {
        (post & entity(as[Create])) {
          (user) => complete(repository.create(user))
        }
      } ~
      (get & path("user") & parameters("id".as[UUID])) {
        (id) => complete(repository.get(id))
      } ~
      (delete & path("user") & parameters("id".as[UUID])) {
        (id) => complete(repository.delete(id))
      } ~
      (put & path("user" / "money" / "add") & parameters("id".as[UUID], "money".as[Int])) {
        (id, money) =>
          onSuccess(repository.addMoney(AddMoney(id, money))) {
            case Right(value) => complete(value)
            case Left(s) => complete(StatusCodes.NotAcceptable, s)
          }
      } ~
      (put & path("user" / "money" / "sub") & parameters("id".as[UUID], "money".as[Int])) {
        (id, money) =>
          onSuccess(repository.subMoney(SubMoney(id, money))) {
            case Right(value) => complete(value)
            case Left(s) => complete(StatusCodes.NotAcceptable, s)
          }
      }
  }
}
