import app.Aplicacion
import service.Calculadora
import ui.Consola

fun main(args: Array<String>) {
    Aplicacion(Consola(), Calculadora()).ejecutar(args)
}

