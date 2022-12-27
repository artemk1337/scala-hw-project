package model

import java.util.UUID

// add some money to user by id
case class AddMoney(id: UUID, money: Int)

// subtract some money from user by id
case class SubMoney(id: UUID, money: Int)

// add or subtract user money
case class ModifyMoney(id: UUID, money: Int)

// users transaction
case class UserTransactions(
                             dstUserId: UUID,
                             srcUserId: UUID,
                             moneyChange: Int,
                             category: Int = 0,
                           )

// result
case class ChangeUserMoneyResult(srcUserId: UUID, moneyResult: Int)
