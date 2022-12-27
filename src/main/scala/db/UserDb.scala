
package db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import java.util.UUID

import model.User

object UserDb {

  //  case class User(id: Option[Int], phoneNumber: Int, money: Int)

  class Users(tag: Tag) extends Table[User](tag, "users") {
    // tried to use O.AutoInc, but can't hold user id,
    // solution: use select by unique phone number,
    // but it is better to avoid unnecessary transactions in the database :)
    val id = column[UUID]("id", O.PrimaryKey)
    val phoneNumber = column[Int]("phoneNumber", O.Unique)
    val email = column[String]("email", O.Unique)
    val money = column[Int]("money")

    def * = (id, phoneNumber, email, money) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[Users]
}
