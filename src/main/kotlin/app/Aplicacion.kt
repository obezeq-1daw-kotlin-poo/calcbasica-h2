package app

import service.Calculadora
import service.Logger
import ui.Consola

class Aplicacion (private val consola: Consola, private val calculadora: Calculadora) {

    private lateinit var logger: Logger

    fun ejecutar(args: Array<String> = emptyArray()) {

        when (args.size) {
            0 -> {
                logger = Logger()
                logger.mostrarUltimoLog()
            }

            1 -> {
                logger = Logger(args[0])
                logger.mostrarUltimoLog()
            }

            4 -> {
                logger = Logger(args[0])
                logger.crearNuevoLog()

                val num1 = args[1].toDoubleOrNull()
                val operador = args[2].firstOrNull()
                val num2 = args[3].toDoubleOrNull()

                if (num1 == null || operador == null || num2 == null) {
                    consola.mostrarInfo("[!] Error en los argumentos")
                    logger.escribirLog("Error en los argumentos: ${args}")
                } else {
                    try {
                        val resultado = calculadora.calcular(num1, num2, operador)
                        consola.mostrarInfo("[*] Resultado: $resultado")
                        logger.escribirLog("$num1 $operador $num2 = $resultado")
                    } catch (e: Exception) {
                        consola.mostrarInfo("[!] ${e.message}")
                        logger.escribirLog("Error: ${e.message}")
                    }
                }
            }

            else -> {
                consola.mostrarInfo("[!] Número de argumentos no válido")
                return
            }
        }

        consolaPausa()

        consola.refresh()
        logger.crearNuevoLog()
        ejecutarCalculadoraEnBucle()
    }

    private fun consolaPausa() {
        consola.mostrarInfo("\n[?] Pulse ENTER para continuar...")
        readlnOrNull()
    }

    private fun ejecutarCalculadoraEnBucle() {
        var continuar = true

        while (continuar) {
            try {
                consola.mostrarInfo("\n───────────────────────────")
                consola.mostrarInfo("        CALCULADORA        ")
                consola.mostrarInfo("───────────────────────────\n")

                val num1 = consola.leerNumero("[+] Ingrese primer número: ")
                val operador = consola.leerOperador("[+] Ingrese operación (+, -, *, /): ")
                val num2 = consola.leerNumero("[+] Ingrese segundo número: ")

                val resultado = calculadora.calcular(num1, num2, operador)
                consola.mostrarResultado(resultado)
                logger.escribirLog("$num1 $operador $num2 = $resultado")

            } catch (e: Exception) {
                consola.mostrarError(e.message ?: "[-] Error desconocido")
                logger.escribirLog("Error: ${e.message}")
            }

            continuar = preguntarRepetir()
        }

        consola.mostrarInfo("\n[+] PROGRAMA FINALIZADO SATISFACTORIAMENTE :D")
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
