package app

import data.DatabaseManager
import data.dao.OperationDaoImpl
import model.Operation
import service.Calculadora
import service.HistoryManager
import ui.Consola
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class Aplicacion(
    private val consola: Consola,
    private val calculadora: Calculadora
) {
    private lateinit var historyManager: HistoryManager

    fun ejecutar(args: Array<String> = emptyArray()) {
        val dbManager = DatabaseManager()
        val operationDao = OperationDaoImpl(dbManager)
        historyManager = HistoryManager(operationDao)

        when (args.size) {
            0, 1 -> mostrarHistorialInicial()
            4 -> procesarArgumentos(args)
            else -> consola.mostrarError("Número de argumentos no válido")
        }

        consolaPausa()
        consola.refresh()
        ejecutarCalculadoraEnBucle()
    }

    private fun mostrarHistorialInicial() {
        try {
            consola.mostrarInfo("\n───────────────────────────")
            consola.mostrarInfo("     Historial Reciente     ")
            consola.mostrarInfo("───────────────────────────")
            historyManager.obtenerHistorial().takeLast(3).forEach { op ->
                val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm").format(op.fecha)
                consola.mostrarInfo("[$fechaFormateada] ${op.operacion} = ${op.resultado}")
            }
        } catch (e: SQLException) {
            consola.mostrarError("Error al cargar historial: ${e.message}")
        }
    }

    private fun procesarArgumentos(args: Array<String>) {
        val num1 = args[1].toDoubleOrNull()
        val operador = args[2].firstOrNull()
        val num2 = args[3].toDoubleOrNull()

        if (num1 == null || operador == null || num2 == null) {
            consola.mostrarError("Argumentos no válidos")
            return
        }

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
            guardarErrorEnHistorial(e)
        }
    }

    private fun ejecutarCalculadoraEnBucle() {
        var continuar = true
        while (continuar) {
            try {
                consola.mostrarInfo("───────────────────────────")
                consola.mostrarInfo("       Menú Principal      ")
                consola.mostrarInfo("───────────────────────────")
                consola.mostrarInfo("[1] Nueva operación")
                consola.mostrarInfo("[2] Ver historial")
                consola.mostrarInfo("[3] Salir\n")

                when (consola.leerOpcion("[+] Seleccione: ")) {
                    1 -> realizarOperacion()
                    2 -> gestionarHistorial()
                    3 -> continuar = false
                    else -> consola.mostrarError("Opción no válida")
                }
            } catch (e: InputMismatchException) {
                consola.mostrarError("Debe ingresar un número")
                consola.scanner.next()
            }
        }
        consola.mostrarInfo("\n[+] PROGRAMA FINALIZADO SATISFACTORIAMENTE :D")
    }

    private fun realizarOperacion() {
        try {
            val num1 = consola.leerNumero("[+] Primer número: ")
            val operador = consola.leerOperador("[+] Operador (+, -, *, /): ")
            val num2 = consola.leerNumero("[+] Segundo número: ")

            val resultado = calculadora.calcular(num1, num2, operador)
            consola.mostrarResultado(resultado)

            val operacion = Operation(
                operacion = "$num1 $operador $num2",
                resultado = resultado
            )
            historyManager.guardarOperacion(operacion)

        } catch (e: ArithmeticException) {
            consola.mostrarError("Error matemático: ${e.message}")
            guardarErrorEnHistorial(e)
        } catch (e: IllegalArgumentException) {
            consola.mostrarError("Operación no válida: ${e.message}")
            guardarErrorEnHistorial(e)
        } catch (e: SQLException) {
            consola.mostrarError("Error al guardar operación: ${e.message}")
        }
    }

    private fun gestionarHistorial() {
        var enHistorial = true
        while (enHistorial) {
            try {
                consola.mostrarMenuHistorial()
                when (consola.leerOpcion("")) {
                    1 -> mostrarUltimasOperaciones()
                    2 -> buscarPorResultado()
                    3 -> enHistorial = false
                    else -> consola.mostrarError("Opción no válida")
                }
            } catch (e: InputMismatchException) {
                consola.mostrarError("Debe ingresar un número válido")
                consola.scanner.next()
            } catch (e: SQLException) {
                consola.mostrarError("Error al acceder al historial: ${e.message}")
            }
        }
    }

    private fun mostrarUltimasOperaciones() {
        try {
            val operaciones = historyManager.obtenerHistorial().takeLast(10).reversed()
            consola.mostrarHistorial(operaciones)
        } catch (e: SQLException) {
            consola.mostrarError("Error al cargar historial: ${e.message}")
        }
    }

    private fun buscarPorResultado() {
        try {
            val resultado = consola.leerNumero("[+] Ingrese resultado a buscar: ")
            val operaciones = historyManager.obtenerHistorial()
                .filter { it.resultado == resultado }
            consola.mostrarHistorial(operaciones)
        } catch (e: InputMismatchException) {
            consola.mostrarError("Debe ingresar un número válido")
        }
    }

    private fun guardarErrorEnHistorial(e: Exception) {
        try {
            val errorOp = Operation(
                operacion = "ERROR: ${e.message?.take(50)}",
                resultado = 0.0
            )
            historyManager.guardarOperacion(errorOp)
        } catch (dbEx: SQLException) {
            consola.mostrarError("CRÍTICO: Fallo al guardar error en BD")
            consola.mostrarError("Detalles: ${dbEx.message}")
        }
    }

    private fun consolaPausa() {
        consola.mostrarInfo("\n[?] Pulse ENTER para continuar...")
        readlnOrNull()
    }
}