package service

class Calculadora {
    fun sumar(a: Double, b: Double) = a + b
    fun restar(a: Double, b: Double) = a - b
    fun multiplicar(a: Double, b: Double) = a * b
    fun dividir(a: Double, b: Double): Double {
        if (b == 0.0) throw ArithmeticException("No se puede dividir por cero")
        return a / b
    }

    fun calcular(a: Double, b: Double, operador: Char): Double {

        return when (operador) {
            '+' -> sumar(a, b)
            '-' -> restar(a, b)
            '*' -> multiplicar(a, b)
            '/' -> dividir(a, b)
            else -> throw IllegalArgumentException("Operador no válido")
        }

    }

}