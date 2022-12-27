package db

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}


class InitDb(implicit val ec: ExecutionContext, db: Database) {
  def init(): Future[_] = {
    db.run(UserDb.users.schema.createIfNotExists) // create table if not exist
  }
}
