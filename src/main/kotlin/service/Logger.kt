package service

import ui.Consola
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Logger(private val ruta: String = "./log") {

    private val consola = Consola()
    private var logActual: File? = null

    init {
        val carpeta = File(ruta)
        if (!carpeta.exists()) {
            carpeta.mkdirs()
            consola.mostrarInfo("[*] Ruta $ruta creada")
        }
    }

    fun crearNuevoLog() {
        val formato = SimpleDateFormat("yyyyMMddHHmmss")
        val nombreArchivo = "log${formato.format(Date())}.txt"
        logActual = File(ruta, nombreArchivo)
    }

    fun escribirLog(linea: String) {
        logActual?.appendText("${fechaActual()} - $linea\n")
    }

    fun mostrarUltimoLog() {
        val carpeta = File(ruta)

        val archivos = carpeta.listFiles()

        val logs = mutableListOf<File>()

        if (archivos != null) {
            for (archivo in archivos) {
                if (archivo.name.startsWith("log")) {
                    logs.add(archivo)
                }
            }
        }

        logs.sortByDescending { it.lastModified() }

        if (logs == null || logs.isEmpty()) {
            consola.mostrarInfo("[!] No existen ficheros de Log")
        } else {

            consola.mostrarInfo("[*] Mostrando contenido del log más reciente: ${logs[0].name}")

            val archivoLog = logs[0]

            val lector = archivoLog.bufferedReader()

            var linea: String?
            while (true) {
                linea = lector.readLine()
                if (linea == null) {
                    break
                }
                consola.mostrarInfo(linea)
            }

            lector.close()
        }
    }

    private fun fechaActual(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formato.format(Date())
    }
}
