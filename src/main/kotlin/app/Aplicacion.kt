package app

import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import service.Calculadora
import service.HistoryManager
import ui.Consola
import java.sql.SQLException

class Aplicacion(
    private val consola: Consola,
    private val calculadora: Calculadora
) {
    private lateinit var historyManager: HistoryManager

    fun ejecutar(args: Array<String> = emptyArray()) {
        when (args.size) {
            0, 1 -> {
                // REALIZO UNA CONFIGURACION BASICA DE LA BASE DE DATOS
                val dbManager = DatabaseManager()
                val operationDao = OperationDaoImpl(dbManager)
                historyManager = HistoryManager(operationDao)

                try {
                    val historial = historyManager.obtenerHistorial()
                    consola.mostrarInfo("\n───────────────────────────")
                    consola.mostrarInfo("     Historial Reciente    ")
                    consola.mostrarInfo("───────────────────────────")
                    historial.forEach { op ->
                        consola.mostrarInfo("${op.fecha} | ${op.operacion} = ${op.resultado}")
                    }
                } catch (e: SQLException) {
                    consola.mostrarError("Error al cargar historial: ${e.message}")
                }
            }

            4 -> {
                // AQUI HAGO LA OPERACION CON ARGUMENTOS COMO ANTERIORMENTE PERO UTILIZANDO LA BASE DE DATOS
                val dbManager = DatabaseManager()
                val operationDao = OperationDaoImpl(dbManager) //TODO: Mejorar esto despues
                historyManager = HistoryManager(operationDao)

                val num1 = args[1].toDoubleOrNull()
                val operador = args[2].firstOrNull()
                val num2 = args[3].toDoubleOrNull()

                if (num1 == null || operador == null || num2 == null) {
                    consola.mostrarError("Argumentos no válidos")
                } else {
                    try {
                        val resultado = calculadora.calcular(num1, num2, operador)
                        val operacion = Operation(
                            operacion = "$num1 $operador $num2",
                            resultado = resultado
                        )
                        historyManager.guardarOperacion(operacion)
                        consola.mostrarResultado(resultado)
                    } catch (e: Exception) {
                        consola.mostrarError(e.message ?: "Error desconocido")
                        val errorOp = Operation(
                            operacion = "ERROR: ${e.message}",
                            resultado = 0.0
                        )
                        historyManager.guardarOperacion(errorOp)
                    }
                }
            }

            else -> consola.mostrarError("Número de argumentos no válido")
        }

        consolaPausa()

        consola.refresh()
        ejecutarCalculadoraEnBucle()
    }

    private fun ejecutarCalculadoraEnBucle() {
        var continuar = true

        while (continuar) {
            try {
                consola.mostrarInfo("\n───────────────────────────")
                consola.mostrarInfo("     CALCULADORA (BD)      ")
                consola.mostrarInfo("───────────────────────────\n")

                val num1 = consola.leerNumero("[+] Primer número: ")
                val operador = consola.leerOperador("[+] Operador (+, -, *, /): ")
                val num2 = consola.leerNumero("[+] Segundo número: ")

                val resultado = calculadora.calcular(num1, num2, operador)
                consola.mostrarResultado(resultado)

                try {
                    val operacion = Operation(
                        operacion = "$num1 $operador $num2",
                        resultado = resultado
                    )
                    historyManager.guardarOperacion(operacion)
                } catch (e: SQLException) {
                    consola.mostrarError("Error grave: No se pudo guardar en el historial")
                    consola.mostrarError("Detalles técnicos: ${e.message}")
                    // AQUI LO QUE HAGO ES BASICAMENTE UN ROLLBACK IMPLICITO GRACIAS AL MANEJO QUE TENGO DIRECTAMENTE EN EL DAO
                }

            } catch (e: ArithmeticException) {
                consola.mostrarError("Error matemático: ${e.message}")
                guardarErrorEnHistorial(e)
            } catch (e: IllegalArgumentException) {
                consola.mostrarError("Operación no válida: ${e.message}")
                guardarErrorEnHistorial(e)
            } catch (e: Exception) {
                consola.mostrarError("Error inesperado: ${e.javaClass.simpleName}")
                guardarErrorEnHistorial(e)
            }

            continuar = preguntarRepetir()
        }
    }

    private fun guardarErrorEnHistorial(e: Exception) {
        try {
            val errorOp = Operation(
                operacion = "ERROR: ${e.message?.take(50)}", // CON ESTO LO QUE HAGO ES LIMPIAR LA LONGITUD DEL ERROR PARA QUE NO HAYA PROBLEMAS EN CUANTO A LONGITUD MAXIMA
                resultado = 0.0
            )
            historyManager.guardarOperacion(errorOp)
        } catch (dbEx: SQLException) {
            consola.mostrarError("CRITICO: Fallo al guardar error en BD")
            consola.mostrarError("Detalles: ${dbEx.message}")
        }
    }

    private fun consolaPausa() {
        consola.mostrarInfo("\n[?] Pulse ENTER para continuar...")
        readlnOrNull()
    }

    private fun preguntarRepetir(): Boolean {
        consola.mostrarInfo("\n[?] ¿Desea realizar otra operación? (S/N): ")
        val respuesta = consola.scanner.next().trim().lowercase()
        if (respuesta == "s" || respuesta == "si") {
            consola.refresh()
            return true
        }
        return false
    }
}
