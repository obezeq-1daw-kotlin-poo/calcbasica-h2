package ui

import java.util.Scanner

class Consola {
    val scanner = Scanner(System.`in`)

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

    fun refresh() {
        for (i in 1..33) {
            println("")
        }
    }

}