package route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class HelloRoute {
  def route: Route =
    (path("hello") & get) {
      complete("Hello scala world!")
    }
}
