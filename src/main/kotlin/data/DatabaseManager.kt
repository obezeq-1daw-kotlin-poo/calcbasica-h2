package data

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager {
    private val jdbcUrl = "jdbc:h2:./db/calcDB"
    private val user = "sa"
    private val password = ""

    fun getConnection(): Connection {
        return try {
            Class.forName("org.h2.Driver")
            DriverManager.getConnection(jdbcUrl, user, password)
        } catch (e: Exception) {
            throw SQLException("Error al conectar a H2: ${e.message}")
        }
    }

    fun closeConnection(connection: Connection) {
        try {
            if (!connection.isClosed) {
                connection.close()
            }
        } catch (e: SQLException) {
            System.err.println("[-] Error al cerrar conexión: ${e.message}")
        }
    }
}
