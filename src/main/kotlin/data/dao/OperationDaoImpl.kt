package data.dao

import data.DatabaseManager
import model.Operation
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

class OperationDaoImpl(private val dbManager: DatabaseManager) : OperationDao {

    override fun insertOperation(operation: Operation): Long {
        val connection = dbManager.getConnection()
        var statement: java.sql.PreparedStatement? = null
        try {
            val query = """
                INSERT INTO operations (operacion, resultado, fecha) 
                VALUES (?, ?, ?)
            """.trimIndent()

            statement = connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)

            statement.setString(1, operation.operacion)
            statement.setDouble(2, operation.resultado)
            statement.setTimestamp(3, Timestamp(operation.fecha.time))

            statement.executeUpdate()
            connection.commit()

            val generatedKeys = statement.generatedKeys
            return if (generatedKeys.next()) {
                generatedKeys.getLong(1)
            } else {
                throw SQLException("[-] Error al obtener ID generado")
            }
        } catch (e: SQLException) {
            connection.rollback()
            throw SQLException("[-] Error DAO al insertar: ${e.message}")
        } finally {
            statement?.close()
            connection.close()
        }
    }

    override fun getAllOperations(): List<Operation> {
        val operations = mutableListOf<Operation>()
        val connection = dbManager.getConnection()
        var statement: java.sql.Statement? = null
        var resultSet: ResultSet? = null
        try {
            statement = connection.createStatement()
            resultSet = statement.executeQuery("SELECT id, operacion, resultado, fecha FROM operations")

            while (resultSet.next()) {
                operations.add(
                    Operation(
                        id = resultSet.getLong("id"),
                        operacion = resultSet.getString("operacion"),
                        resultado = resultSet.getDouble("resultado"),
                        fecha = resultSet.getTimestamp("fecha")
                    )
                )
            }
            return operations
        } catch (e: SQLException) {
            throw SQLException("[-] Error DAO al leer: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            connection.close()
        }
    }

    override fun deleteOperationById(id: Long): Boolean {
        throw UnsupportedOperationException("[-] No implementado aún")
    }
}