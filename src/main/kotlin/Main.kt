import data.DatabaseManager

fun main() {
    val dbManager = DatabaseManager()
    val conn = dbManager.getConnection()
    println("[+] Conexion exitosa: ${!conn.isClosed}")
    dbManager.closeConnection(conn)
}

