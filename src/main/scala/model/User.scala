package model

import java.util.UUID

// create user with random uuid and extra params from Create()
case class User(id: UUID = UUID.randomUUID(), phoneNumber: Int, email: String, money: Int) {
}

// create user with phone number and start money
case class Create(phoneNumber: Int, email: String, money: Int)

