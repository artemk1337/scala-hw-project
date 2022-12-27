import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce._

import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

import db.InitDb
import repository._
import route._

object App extends App with FailFastCirceSupport {
  implicit private val system: ActorSystem = ActorSystem("UserApp")
  implicit private val ec: ExecutionContextExecutor = system.dispatcher
  implicit private val db: PostgresProfile.backend.Database = PostgresProfile.backend.Database.forConfig("database.postgres")

  private val ListenPort = 8081
  private val ListenAddr = "0.0.0.0"
  private val repository: UserRepositoryDb = new UserRepositoryDb
  private val helloRoute = new HelloRoute().route
  private val userRoute = new Routes(repository).route
  private val transferRoute = new TransferRoute(repository).route

  // create table in db
  new InitDb().init()

  // listen server
  Http().newServerAt(interface = ListenAddr, port = ListenPort).
    bind(helloRoute ~ userRoute ~ transferRoute)
  println("Server is running, enter any character to disable it")
  StdIn.readLine()
}
