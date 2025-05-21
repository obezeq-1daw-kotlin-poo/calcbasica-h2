import app.Aplicacion
import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import service.Calculadora
import service.HistoryManager
import ui.Consola
import java.sql.SQLException

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "test-db") {
        pruebaBaseDeDatos()
    } else {
        Aplicacion(Consola(), Calculadora()).ejecutar(args)
    }
}

/** REALIZO UN TEST MANUAL SIMPLE PARA PROBAR LA BASE DE DATOS */
fun pruebaBaseDeDatos() {
    println("\n──────────────────────────────────────────────────────")
    println("           INICIALIZANDO CALCULADORA 3000             ")
    println("──────────────────────────────────────────────────────")

    val dbManager = DatabaseManager()
    val dao = OperationDaoImpl(dbManager)
    val historyManager = HistoryManager(dao)

    val operacionDePrueba = Operation(
        operacion = "2 + 2",
        resultado = 4.0
    )

    try {
        val id = dao.insertOperation(operacionDePrueba)
        println("[+] Operacion insertada con ID: $id")
    } catch (e: SQLException) {
        println("[-] Error insertando operación: ${e.message}")
        return
    }

    try {
        val operaciones = dao.getAllOperations()
        println("\n───────────────────────────")
        println("     OPERACIONES EN BD     ")
        println("───────────────────────────")
        operaciones.forEach {
            println("ID ${it.id}: ${it.operacion} = ${it.resultado}")
        }
    } catch (e: SQLException) {
        println("[-] Error leyendo operaciones: ${e.message}")
    }

    println("[+] Prueba realizada con exito, muchas gracias y que tenga un buen dia ;D")
}