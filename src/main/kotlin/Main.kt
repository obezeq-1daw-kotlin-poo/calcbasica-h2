import app.Aplicacion
import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import service.Calculadora
import service.HistoryManager
import ui.Consola
import java.sql.SQLException

fun main(args: Array<String>) {
    val dbManager = DatabaseManager()
    val operationDao = OperationDaoImpl(dbManager)
    val historyManager = HistoryManager(operationDao)
    val consola = Consola()
    val calculadora = Calculadora()

    if (args.isNotEmpty() && args[0] == "testDB") {
        pruebaBaseDeDatos(operationDao, historyManager)
    } else {
        Aplicacion(consola, calculadora, historyManager).ejecutar(args)
    }
}

/** TEST MANUAL DE BASE DE DATOS CON ESTILO CALCULADORA 3000 */
fun pruebaBaseDeDatos(dao: OperationDaoImpl, history: HistoryManager) {
    println("──────────────────────────────────────────────────────")
    println("               PRUEBA DE BASE DE DATOS                ")
    println("──────────────────────────────────────────────────────")

    val operacionDePrueba = Operation(
        operacion = "2 + 2",
        resultado = 4.0
    )

    try {
        val id = dao.insertOperation(operacionDePrueba)
        println("[*] Operación insertada con ID: $id")
    } catch (e: SQLException) {
        println("\n[-] CRÍTICO: Fallo en inserción")
        println("[-] Razón: ${e.message}")
        return
    }

    try {
        val operaciones = dao.getAllOperations()
        println("\n───────────────────────────")
        println("       REGISTROS EN BD     ")
        println("───────────────────────────")
        operaciones.forEach {
            println("[*] ID ${it.id}: ${it.operacion.padEnd(15)} = ${it.resultado}")
        }
    } catch (e: SQLException) {
        println("\n[-] CRÍTICO: Fallo en lectura")
        println("[-] Razón: ${e.message}")
        return
    }
}
