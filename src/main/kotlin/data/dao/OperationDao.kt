package data.dao

import model.Operation

interface OperationDao {
    fun insertOperation(operation: Operation): Long

    fun getAllOperations(): List<Operation>

    fun deleteOperationById(id: Long): Boolean
}
