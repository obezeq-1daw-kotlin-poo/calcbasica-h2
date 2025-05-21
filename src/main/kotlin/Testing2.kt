import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import java.sql.SQLException

fun main() {
    val dbManager = DatabaseManager()
    val dao = OperationDaoImpl(dbManager)

    val op = Operation(
        operacion = "10 / 2",
        resultado = 5.0
    )

    try {
        val id = dao.insertOperation(op)
        val savedOp = dao.getAllOperations().first()
        println("[+] Operación guardada: ${savedOp.operacion} = ${savedOp.resultado}")
    } catch (e: SQLException) {
        println("[-] Error crítico: ${e.message}")
    }
}