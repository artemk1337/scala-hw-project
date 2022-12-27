package repository

import db.UserDb._
import model._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}


class UserRepositoryDb(implicit val ec: ExecutionContext, db: Database) extends UserRepository {

  private val category = Map(
    0 -> 0.0,  // simple user transaction
    1 -> 0.01,  // cashback: shopping in the store
    2 -> 0.1,  // big cashback 10%!!!
  )
  private val defaultCashback = 0.0

  override def create(user: Create): Future[User] = {

    val newUser = User(phoneNumber = user.phoneNumber, email = user.email, money = user.money)

    for {
      _ <- db.run(
        users += newUser
      )
      res <- get(
        newUser.id
      )
    }

    yield res
  }

  override def get(id: UUID): Future[User] = {
    db.run(
      users.
        filter(i => i.id === id).
        result.
        head
    )
  }

  override def list(): Future[Seq[User]] = {
    db.run(
      users.
        result
    )
  }

  override def find(id: UUID): Future[Option[User]] = {
    db.run(
      users.
        filter(i => i.id === id).
        result.
        headOption
    )
  }

  def modifyMoney(user: ModifyMoney): Future[Either[String, User]] = {
    val query = users.
      filter(_.id === user.id).
      map(_.money)

    for {

      startMoney <- db.run(
        query.
          result.
          headOption
      )

      buffMoney = user.money // if add money positive int, if subtract negative int

      updateMoney = startMoney.map {
        startMoney =>
          if ((startMoney + buffMoney) < 0)
            Left("Not enough money to subtract")  // only called when buffMoney < 0 and |buffMoney| > startMoney
          else
            Right(startMoney + buffMoney)  // success add or subtract
      }.getOrElse(
        Left("Not found")
      )

      future = updateMoney.map(money => db.run {
        query.
          update(money)
      }) match {
        case Right(future) => future.map(Right(_))
        case Left(s) => Future.successful(Left(s))
      }

      updated <- future
      tmp_res <- find(user.id)
      res = updated.map(_ => tmp_res.get)
      
    } yield res
  }

  override def addMoney(user: AddMoney): Future[Either[String, User]] = {
    modifyMoney(ModifyMoney(user.id, user.money))
  }

  override def subMoney(user: SubMoney): Future[Either[String, User]] = {
    modifyMoney(ModifyMoney(user.id, -user.money))
  }

  override def moneyTransfer(userTransaction: UserTransactions): Future[Either[String, ChangeUserMoneyResult]] = {
    val userSrc = users.
        filter(_.id === userTransaction.srcUserId).
        map(_.money)

    val userDst = users.
      filter(_.id === userTransaction.dstUserId).
      map(_.money)

    for {

      userSrcOpt <- db.run(userSrc.result.headOption) // source user subtract money
      userDstOpt <- db.run(userDst.result.headOption) // destination user add money

      diffMoney = userTransaction.moneyChange // money to transfer

      // cashback money, default 0
      cashbackMoney = (
        diffMoney.toDouble * category.getOrElse(userTransaction.category, defaultCashback)).toInt
      diffMoneyWithCashback = diffMoney - cashbackMoney

      updateUserSrc = userSrcOpt.map {
        userSrcMoney => {
        if (userSrcMoney >= diffMoneyWithCashback)
          Right(userSrcMoney - diffMoneyWithCashback)
        else
          Left("Not enough money to transfer")
      }}.getOrElse(
        Left("Not found")
      )

      userDstUpd = userDstOpt.map {
        userDstMoney => {
        Right(userDstMoney + diffMoney)
      }}.getOrElse(
        Left("Not found")
      )

      userSrcFuture = updateUserSrc.map {
        money =>
          db.run {
            userSrc.update(money)
          }
      } match {
        case Right(_) =>
          userDstUpd.map(money =>
            db.run {
              userDst.update(money)
            }
          ) match {
            case Right(future) => future.map(Right(_))
            case Left(s) => Future.successful(Left(s))
          }
        case Left(s) => Future.successful(Left(s))
      }

      updated <- userSrcFuture
      res <- find(userTransaction.srcUserId)

    } yield updated.
      map(_ => ChangeUserMoneyResult(userTransaction.srcUserId, res.get.money))
  }

  override def delete(id: UUID): Future[Unit] = {
    db.run(
      users.
        filter(_.id === id).
        delete).
      map(_ => ())
  }
}

