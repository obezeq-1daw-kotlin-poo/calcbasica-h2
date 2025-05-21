package ui

import model.Operation
import java.text.SimpleDateFormat
import java.util.Scanner

class Consola {
    val scanner = Scanner(System.`in`)

    fun refresh() {
        for (i in 1..33) {
            println("")
        }
    }

    fun leerNumero(mensaje: String): Double {
        print(mensaje)
        return scanner.nextDouble()
    }

    fun leerOperador(mensaje: String): Char {
        print(mensaje)
        return scanner.next()[0]
    }

    fun mostrarResultado(resultado: Double) {
        println("\n[*] Resultado: $resultado")
    }

    fun mostrarError(mensaje: String) {
        println("\n[-] Error: $mensaje")
    }

    fun mostrarInfo(mensaje: String) {
        println(mensaje)
    }

    fun mostrarMenuHistorial() {
        println("\n───────────────────────────")
        println("       Menú Historial      ")
        println("───────────────────────────")
        println("[1] Ver últimas 10 operaciones")
        println("[2] Buscar por resultado")
        println("[3] Volver al menú principal")
        print("\n[+] Seleccione una opción: ")
    }

    fun mostrarHistorial(operaciones: List<Operation>) {
        if (operaciones.isEmpty()) {
            println("\n[!] No hay operaciones registradas")
            return
        }
        println("\n────────────────────────────────────")
        println("       Historial de Operaciones     ")
        println("────────────────────────────────────")
        operaciones.forEach { op ->
            val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm").format(op.fecha)
            println("[$fechaFormateada] ${op.operacion} = ${op.resultado}")
        }
    }

    fun leerOpcion(mensaje: String): Int {
        print(mensaje)
        return scanner.nextInt()
    }

}