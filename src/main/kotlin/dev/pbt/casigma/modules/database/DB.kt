package dev.pbt.casigma.modules.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class DB {
    var db: DataSource = connect();
    val conn = Database.connect(db)

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://localhost:3306/casigma"
        config.username = "root"
        config.password = ""
        config.driverClassName = "com.mysql.cj.jdbc.Driver"

        return HikariDataSource(config)

    }

    fun migrate(): MigrateResult? {
        val flyway = Flyway.configure()
            .dataSource(db)
            .locations("filesystem:$MIGRATIONS_DIRECTORY")
            .baselineOnMigrate(true) // Used when migrating an existing database for the first time
            .load()

        return flyway.migrate()
    }
}