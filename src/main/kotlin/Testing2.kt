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
        dao.insertOperation(op)
    } catch (e: SQLException) {
        println("[-] Error esperado: ${e.message}")
    }
}
