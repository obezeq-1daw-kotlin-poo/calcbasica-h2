package service

import data.dao.OperationDao
import model.Operation
import java.sql.SQLException

class HistoryManager(private val operationDao: OperationDao) {

    fun guardarOperacion(operacion: Operation) {
        try {
            operationDao.insertOperation(operacion)
        } catch (e: SQLException) {
            throw SQLException("[-] Error al guardar en historial: ${e.message}")
        }
    }

    fun obtenerHistorial(): List<Operation> {
        return try {
            operationDao.getAllOperations()
        } catch (e: SQLException) {
            emptyList()
        }
    }
}
