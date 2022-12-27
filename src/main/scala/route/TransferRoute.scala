package route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce._
import io.circe.generic.auto._
import model._
import repository._

class TransferRoute(repository: UserRepository) extends FailFastCirceSupport {
  def route: Route =
    path("transfer") {
      (put & entity(as[UserTransactions])) { moneyTransfer =>
        onSuccess(repository.moneyTransfer(moneyTransfer)) {
          case Right(value) => complete(value)
          case Left(s) => complete(StatusCodes.NotAcceptable, s)
        }
      }
    }
}
