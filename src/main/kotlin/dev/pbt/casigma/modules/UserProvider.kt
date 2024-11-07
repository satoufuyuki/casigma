package dev.pbt.casigma.modules;

import dev.pbt.casigma.modules.database.DB;
import dev.pbt.casigma.modules.database.models.User
import dev.pbt.casigma.modules.database.models.UserRole
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class UserModel (val email: String, val name: String, val role: UserRole)

class UserProvider(private val db: DB, private val argon2: Argon2) {
    var authenticatedUser: UserModel? = null
    fun authenticate(email: String, password: String): Boolean {
        return transaction(db.conn) {
            val user = User.selectAll().where {
                User.email eq email
            }.firstOrNull()

            if (user == null) {
                return@transaction false
            }

            val isPasswordValid = argon2.verify(password, user[User.password])
            if (!isPasswordValid) {
                return@transaction false
            }

            authenticatedUser = UserModel(
                user[User.email], user[User.name], user[User.role]
            )

            return@transaction true
        }
    }
}
