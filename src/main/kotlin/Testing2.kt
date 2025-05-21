import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import java.sql.SQLException

fun main() {
    val dbManager = DatabaseManager()
    val dao = OperationDaoImpl(dbManager)

    val op = Operation(
        operaction = "5 + 3",
        resultado = 8.0
    )

    try {
        val id = dao.insertOperation(op)
        println("Operación insertada con ID: $id")

        val ops = dao.getAllOperations()
        println("Operaciones en BD: ${ops.size}")
    } catch (e: SQLException) {
        println("Error FATAL: ${e.message}")
    }
}