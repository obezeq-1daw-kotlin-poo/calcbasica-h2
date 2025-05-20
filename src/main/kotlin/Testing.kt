import data.DatabaseManager

fun main() {
    val dbManager = DatabaseManager()
    repeat(6) { i ->
        val conn = dbManager.getConnection()
        println("Conexión ${i + 1}: ${!conn.isClosed}")
        Thread.sleep(369)
    }
}

