package repository

import model._

import java.util.UUID
import scala.concurrent.Future

// interfaces
trait UserRepository {
  def get(id: UUID): Future[User]

  def find(id: UUID): Future[Option[User]]

  def list(): Future[Seq[User]]

  def create(user: Create): Future[User]

  def addMoney(user: AddMoney): Future[Either[String, User]]

  def subMoney(user: SubMoney): Future[Either[String, User]]

  def moneyTransfer(userTransaction: UserTransactions): Future[Either[String, ChangeUserMoneyResult]]

  def delete(id: UUID): Future[Unit]
}
