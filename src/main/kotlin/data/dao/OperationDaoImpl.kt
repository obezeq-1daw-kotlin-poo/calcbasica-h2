package data.dao

import data.DatabaseManager
import model.Operation
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class OperationDaoImpl(private val dbManager: DatabaseManager) : OperationDao {

    override fun insertOperation(operation: Operation): Long {
        val connection = dbManager.getConnection()
        try {
            val query = """
                INSERT INTO operations (operaction, resultado, fecha) 
                VALUES ('${operation.operaction}', ${operation.resultado}, '${operation.fecha}')
            """.trimIndent()

            val statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
            statement.executeUpdate()

            val generatedKeys: ResultSet = statement.generatedKeys
            return if (generatedKeys.next()) {
                generatedKeys.getLong(1)
            } else {
                throw SQLException("[-] Error al obtener ID generado")
            }
        } catch (e: SQLException) {
            throw SQLException("[-] Error DAO al insertar: ${e.message}")
        } finally {
            connection.close()
        }
    }
}
