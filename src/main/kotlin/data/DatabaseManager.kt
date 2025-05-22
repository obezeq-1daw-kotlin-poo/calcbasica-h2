package data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException

class DatabaseManager {
    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig().apply {
            jdbcUrl = DatabaseConfig.JDBC_URL
            username = DatabaseConfig.USERNAME
            password = DatabaseConfig.PASSWORD
            maximumPoolSize = DatabaseConfig.MAX_POOL_SIZE
            isAutoCommit = false
        }
        dataSource = HikariDataSource(config)
        initDatabase()
    }

    private fun initDatabase() {
        dataSource.connection.use { connection ->
            try {
                connection.createStatement().use { statement ->
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
                }
            } catch (e: SQLException) {
                connection.rollback()
                System.err.println("[-] Error inicialización BD: ${e.message}")
            }
        }
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }
}
