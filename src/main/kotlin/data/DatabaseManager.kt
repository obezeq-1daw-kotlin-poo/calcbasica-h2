package data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException

class DatabaseManager {
    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:./db/calcDB"
            username = "sa"
            password = ""
            maximumPoolSize = 5
            isAutoCommit = false
        }
        dataSource = HikariDataSource(config)
        initDatabase()
    }

    private fun initDatabase() {
        var connection: Connection? = null
        try {
            connection = dataSource.connection
            val statement = connection.createStatement()
            statement.execute("""
                CREATE TABLE IF NOT EXISTS operations (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    operacion VARCHAR(255) NOT NULL,
                    resultado DOUBLE NOT NULL,
                    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)
            connection.commit()
            println("[*] Tabla 'operations' creada/verificada")
        } catch (e: SQLException) {
            connection?.rollback()
            System.err.println("[-] Error inicialización BD: ${e.message}")
        } finally {
            connection?.close()
        }
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }
}
