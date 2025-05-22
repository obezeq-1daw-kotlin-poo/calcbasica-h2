package data.dao

import data.DatabaseManager
import model.Operation
import java.sql.SQLException
import java.sql.Timestamp

class OperationDaoImpl(private val dbManager: DatabaseManager) : OperationDao {

    override fun insertOperation(operation: Operation): Long {
        require(operation.operacion.isNotBlank()) { "[-] Operación no puede estar vacía" }
        return dbManager.getConnection().use { connection ->
            try {
                // Me aseguro que el autocommit este en false para poder hacer commits y rollbacks manualmente
                connection.autoCommit = false

                val query = """
                    INSERT INTO operations (operacion, resultado, fecha) 
                    VALUES (?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, operation.operacion)
                    statement.setDouble(2, operation.resultado)
                    statement.setTimestamp(3, Timestamp(operation.fecha.time))

                    val affectedRows = statement.executeUpdate()
                    if (affectedRows == 0) {
                        throw SQLException("[-] Error: Ninguna fila afectada")
                    }

                    connection.commit()

                    statement.generatedKeys.use { generatedKeys ->
                        return if (generatedKeys.next()) {
                            generatedKeys.getLong(1)
                        } else {
                            throw SQLException("[-] Error al obtener ID generado")
                        }
                    }
                }
            } catch (e: SQLException) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun getAllOperations(): List<Operation> {
        return dbManager.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT id, operacion, resultado, fecha FROM operations").use { resultSet ->
                    val operations = mutableListOf<Operation>()

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
                    operations
                }
            }
        }
    }
}