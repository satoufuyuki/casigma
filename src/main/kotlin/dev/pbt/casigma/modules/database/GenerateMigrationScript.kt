@file:OptIn(ExperimentalDatabaseMigrationApi::class)
package dev.pbt.casigma.modules.database

import dev.pbt.casigma.modules.Argon2
import dev.pbt.casigma.modules.database.models.User
import dev.pbt.casigma.utils.MigrationUtils
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val MIGRATIONS_DIRECTORY = "migrations" // Location of migration scripts

val database = Database.connect(
    DB().db
)

fun main() {
    transaction(database) {
        User.insert {
            it[User.email] = "admin@pbt.com"
            it[User.password] = Argon2().hash("password")
            it[User.createdAt] = java.time.LocalDateTime.now()
            it[User.updatedAt] = java.time.LocalDateTime.now()
        }
        generateMigrationScript()
    }
}

fun generateMigrationScript() {
    MigrationUtils.generateMigrationScript(
        User,
        scriptDirectory = MIGRATIONS_DIRECTORY,
        scriptName = "V1__create_users_table",
    )
}
