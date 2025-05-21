package data.dao

import model.Operation

class FakeOperationDao : OperationDao {

    private val operations = mutableListOf<Operation>()

    override fun insertOperation(operation: Operation) = 1L

    override fun getAllOperations() = operations

}
