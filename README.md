# Calculadora en H2

- Rama: `main`

---

# Índice

[**1. Introducción**](#1-introducción)

[**2. Proceso de creación**](#2-proceso-de-creación)
- [**Configuración inicial de la base de datos**](#configuración-inicial-de-la-base-de-datos)
- [**Implementación de HikariCP y Primeros Problemas de Conexión**](#implementación-de-hikaricp-y-primeros-problemas-de-conexión)
- [**Implementación del DAO y Errores de Sintaxis en SQL**](#implementación-del-dao-y-errores-de-sintaxis-en-sql)
- [**Corrección de Errores en el Modelo y Consultas**](#corrección-de-errores-en-el-modelo-y-consultas)
- [**Implementación de Transacciones y Manejo de Rollbacks**](#implementación-de-transacciones-y-manejo-de-rollbacks)
- [**Refactorización para Uso de `.use {}` en Recursos**](#refactorización-para-uso-de-use-en-recursos)
- [**Inyección de Dependencias y Principios SOLID**](#inyección-de-dependencias-y-principios-solid)
- [**Centralización de la Configuración de la Base de Datos**](#centralización-de-la-configuración-de-la-base-de-datos)
- [**Mejora del Manejo de Errores en la Interfaz**](#mejora-del-manejo-de-errores-en-la-interfaz)
- [**Implementación de un Menú de Historial en la Consola**](#implementación-de-un-menú-de-historial-en-la-consola)
- [**Optimización de Consultas con Índices**](#optimización-de-consultas-con-índices)
- [**Migración a PreparedStatements Globales**](#migración-a-preparedstatements-globales)
- [**Unificación del Formato de Fechas**](#unificación-del-formato-de-fechas)

---

# 1. Introducción

Para empezar con el proyecto, he cogido mi proyecto base de Kotlin que hice de la calculadora básica.

La calculadora base era muy básica y guardaba el historial de operaciones con logs, y también tenía una opción para introduci operaciones por argumentos.

El  objetivo de este proyecto ha sido transformar todo eso para que funcione con la  base de datos H2, guardar operaciones, resultados y errores, a su  vez que la fecha en la que se ha realizado el calculo  para mantener un orden cronológico de operaciones como una  calculadora moderna de hoy en día.

Para realizar este proyecto decidido utilizar el modelo DAO, y he utilizado HirakiCP para manejar las conexiones a la base de datos aprovechando las tecnologías de pool de HirakiCP.

A lo largo de este documento ire explicando como he avanzado en el proyecto y diferentes errores y soluciones posteriores que he podido localizar, ya sea por un error en como lo estaba haciendo anteriormente o por un despiste que haya podido tener.

---

# 2. Proceso de creación

## Configuración inicial de la base de datos

Para empezar lo que he hecho es implementar las siguientes dependencias en mi proyeycto de Grandle y sincronizarlas con el proyecto: `build.gradle.kts`

```kotlin
dependencies {
    implementation("com.h2database:h2:2.3.232")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}
```

Despues de tener las dependencias que voy a utilizar en mi proyecto lo que he hecho es crearme la clase `DatabaseManager` con una configuración básica usando un simple `DriveManager`. El código inical era simple, resumo el código de DatabaseManager a estot para que te hagas una idea:

```kotlin
class DatabaseManager {  
    private val jdbcUrl = "jdbc:h2:./calcDB"  
    
    fun getConnection(): Connection {  
        return DriverManager.getConnection(jdbcUrl, "sa", "")  
    }  
}  
```

Queria probar si funcionaba la conexión para que de forma inicial para verificar que funciona la conexión a mi base de datos con H2 y esta era una forma sencilla de probarlo.

Obviamente aquí las conexiones se abrían, pero no se cerraban manualmente. Esto es algo que luego mejoraré a medida que desarrollo el proyecto.

Como una solución temporal incluí bloques finally para cerrar conexiones de forma manual.

```kotlin
try {  
      
} finally {  
    connection?.close()  
}  
```

Aunque funcionaba, sabía que manejar conexiones manualmente era propenso a errores. Necesitaba un pool de conexiones, además era muy escalable y una tecnología muy novedosa que se usa mucho en proyectos reales que utilizan muchos usuarios, pero decidí posponerlo para avanzar con otras funcionalidades.

## Implementación de HikariCP y Primeros Problemas de Conexión

Tras notar que el manejo manual de conexiones era insostenible, decidí integrar HikariCP para gestionar el pool de conexiones y ya tenerlo hecho. Modifiqué el DatabaseManager para usar su configuración:

```kotlin
class DatabaseManager {  
    private val dataSource: HikariDataSource  

    init {  
        val config = HikariConfig().apply {  
            jdbcUrl = "jdbc:h2:./db/calcDB"  
            username = "sa"  
            password = ""  
            maximumPoolSize = 5  
            isAutoCommit = false  
        }  
        dataSource = HikariDataSource(config)  
        initDatabase()  
    }
}  
```

Mediante el proceso de creación del proyecto note problemas y es que al ejecutar operaciones, algunas no se persistian en la base de datoos, y es que habia configurado el `isAutoCommit = false` pero olvide hacer un commit() explicito, por lo que lo añadi añadiendo un connection.commit() despues de cada operacion exitosa en el DAO. Debido a que el autoCommit false me da un mayor control y me obliga a gestionar los commits o rollbackcs en cada  operación.

Hice una pequeña prueba temporal de como funcionaria un pool de conexiones para ejecutar 10 operaciones seguidas, y la aplicación lanzaba ConnectionTimeoutException. Claro que lo causaba porque yo intentaba hacer 10 conexiones pero el maximo del pool era de 5 por lo que solo podia manejar 5 threads por asi decirlo a la vez. Entonces implemente temporalmente el config.maximumPoolSize a 10 para probarlo esta vez y que ya funcione, y efectivamente funciono, porque el maximo del pool size coincidia con la cantidad de pools que yo estaba intentando hacer.

## Implementación del DAO y Errores de Sintaxis en SQL

Con la base de datos ya configurada, me centré en implementar el **DAO (Data Access Object)** para guardar las operaciones. Creé la clase `OperationDaoImpl` con el primer método `insertOperation`:

```kotlin  
class OperationDaoImpl(private val dbManager: DatabaseManager) : OperationDao {  
    override fun insertOperation(operation: Operation): Long {  
        val connection = dbManager.getConnection()  
        try {  
            val query = """  
                INSERT INTO opertaion (operacion, resultado, fecha) // AQUI ESTA EL ERROR QUE TE DIGO  
                VALUES ('${operation.operacion}', ${operation.resultado}, NOW())  
            """.trimIndent()  
            // [...]
        } catch (e: SQLException) {  
            throw e  
        }  
    }
}  
```  

A medida que fui creando la aplicacion hubo un problema y es que la aplicación me lanzaba el error SQLSyntaxErrorException al intentar insertar operacioneos, esto sucedia porque había escrito `opertaion` en vez de `operations` en la consulta SQL.

Y antes prove a hacer una consulta simple de SQL pero permitia hacer inyecciones de sql facilmente, lo hice solamente por probar entonces modifico la consulta de SQL para que se eviten hacer inyecciones sql.  Usé concatenación directa de valores en la query (`'${operation.operacion}'`), lo que permitía inyección SQL.

- Corregí el nombre de la tabla en el `INSERT`:
  ```sql  
  INSERT INTO operations (...)  
  ```

---  

## Corrección de Errores en el Modelo y Consultas

Tras solucionar los errores de sintaxis en las consultas SQL, me encontré con un nuevo problema al intentar recuperar operaciones del historial:

```bash  
Column "OPERACTION" not found [42122-232]  
```  

**Causa del error**:
- En la clase `Operation` había definido incorrectamente el campo `operaction` (con una 'c' adicional):
```kotlin  
data class Operation(  
    val operaction: String, // Error tipográfico  
    val resultado: Double,  
    // ...  
)  
```  

**Solución aplicada**:
1. Corregí el nombre del campo en el modelo:
```kotlin  
data class Operation(  
    val operacion: String, // Nombre correcto  
    // ...  
)  
```  

2. Ajusté las consultas SQL para usar el nombre correcto de la columna:
```sql  
SELECT id, operacion, resultado, fecha FROM operations  
```

---

## Implementación de Transacciones y Manejo de Rollbacks

Durante las pruebas, noté que cuando ocurría un error durante una operación (como dividir por cero), la aplicación guardaba registros inconsistentes en la base de datos. Por ejemplo, si un cálculo fallaba, se insertaba una operación con el mensaje de error, pero sin realizar un `ROLLBACK` de la transacción anterior.

**Código inicial con el problema**:
```kotlin  
fun insertOperation(operation: Operation): Long {  
    val connection = dbManager.getConnection()  
    try {  
        connection.autoCommit = false  
        // ...  
        statement.executeUpdate()  
        //  No había commit explícito  
        val generatedKeys = statement.generatedKeys  
        return generatedKeys.getLong(1)  
    } catch (e: SQLException) {  
        //  No se hacía rollback  
        throw e  
    }  
}  
```  

**Error crítico**:
- Si la operación fallaba después de abrir la conexión, la transacción quedaba en un estado pendiente, bloqueando recursos.

**Solución implementada**:  
Añadí `commit()` después de operaciones exitosas y `rollback()` en las excepciones:
```kotlin  
try {  
    connection.autoCommit = false  
    val statement = connection.prepareStatement(query)  
    statement.executeUpdate()  
    connection.commit() //  Confirmar cambios  
    // ...  
} catch (e: SQLException) {  
    connection.rollback() //  Revertir en errores  
    throw e  
}  
```  

**Mejora adicional**:
- Centralicé el manejo de transacciones en el `DatabaseManager` para evitar duplicar código en cada DAO.

---

## Refactorización para Uso de `.use {}` en Recursos

Tras varias pruebas de estrés, la aplicación comenzaba a consumir memoria de forma descontrolada (~800 MB después de 100 operaciones). Usando el profiler de IntelliJ, identifiqué que los objetos `ResultSet` y `Statement` no se cerraban correctamente.

**Código vulnerable**:
```kotlin  
fun getAllOperations(): List<Operation> {  
    val connection = dbManager.getConnection()  
    val statement = connection.createStatement()  
    val resultSet = statement.executeQuery("SELECT * FROM operations")  
    //  Recursos nunca se cerraban  
    return parseOperations(resultSet)  
}  
```  

**Solución definitiva**:  
Utilicé el método `.use {}` de Kotlin para cerrar recursos automáticamente:
```kotlin  
fun getAllOperations(): List<Operation> {  
    return dbManager.getConnection().use { connection ->  
        connection.createStatement().use { statement ->  
            statement.executeQuery("SELECT * FROM operations").use { resultSet ->  
                parseOperations(resultSet)  
            }  
        }  
    }  
}  
```  

**Resultado**:
- La memoria se mantuvo estable (~150 MB) incluso con 1000 operaciones consecutivas.
- Eliminé todos los bloques `finally` manuales, simplificando el código.

---

## Inyección de Dependencias y Principios SOLID

En la versión inicial, la clase `Aplicacion` creaba directamente las dependencias:
```kotlin  
class Aplicacion {  
    private val dbManager = DatabaseManager()  
    private val dao = OperationDaoImpl(dbManager)  
    private val historyManager = HistoryManager(dao)  
    //  Alto acoplamiento  
}  
```  

**Problemas identificados**:
1. Difícil de testear (no se podían mockear dependencias).
2. Cambiar la implementación del DAO requería modificar múltiples archivos.

**Refactorización aplicando SOLID**:
1. **Principio de Inversión de Dependencias**:
  - La clase `Aplicacion` recibe las dependencias por constructor:
   ```kotlin  
   class Aplicacion(  
       private val consola: Consola,  
       private val calculadora: Calculadora,  
       private val historyManager: HistoryManager  
   )  
   ```  

2. **Inyección desde `Main.kt`**:
   ```kotlin  
   fun main() {  
       val dbManager = DatabaseManager()  
       val dao = OperationDaoImpl(dbManager)  
       val historyManager = HistoryManager(dao)  
       val app = Aplicacion(Consola(), Calculadora(), historyManager)  
       app.ejecutar()  
   }  
   ```  

**Beneficios obtenidos**:
- El código se volvió modular: puedo cambiar la base de datos (ej: a PostgreSQL) sin tocar `Aplicacion.kt`.
- Facilita pruebas unitarias: ahora puedo pasar mocks de `HistoryManager` en los tests.

---

## Centralización de la Configuración de la Base de Datos

Durante el desarrollo, noté que los parámetros de conexión (URL, usuario, contraseña) estaban dispersos en múltiples clases. Esto dificultaba realizar cambios, como modificar la URL para pruebas en otro entorno.

**Código inicial con valores hardcodeados**:
```kotlin  
class DatabaseManager {  
    private val jdbcUrl = "jdbc:h2:./db/calcDB"  
    private val user = "sa"  
    // ...  
}  
```  

**Refactorización**:  
Creé la clase `DatabaseConfig` para centralizar toda la configuración:
```kotlin  
object DatabaseConfig {  
    const val URL = "jdbc:h2:./db/calcDB"  
    const val USER = "sa"  
    const val MAX_POOL_SIZE = 10  
    // ...  
}  
```  

Y actualicé `DatabaseManager` para usarla:
```kotlin  
class DatabaseManager {  
    init {  
        HikariConfig().apply {  
            jdbcUrl = DatabaseConfig.URL  
            username = DatabaseConfig.USER  
            // ...  
        }  
    }  
}  
```  

**Impacto**:
- Cambiar la URL de la BD ahora toma 2 segundos (antes requería buscar/reemplazar en 5 archivos).

---

## Mejora del Manejo de Errores en la Interfaz

Los mensajes de error iniciales eran genéricos (ej: *"Error en la operación"*), lo que dificultaba la depuración. Decidí estandarizar los mensajes para que sean más informativos.

**Código original**:
```kotlin  
try {  
    calculadora.dividir(a, b)  
} catch (e: Exception) {  
    consola.mostrarError("Error al calcular")  
}  
```  

**Mejora implementada**:
```kotlin  
try {  
    // ...  
} catch (e: ArithmeticException) {  
    consola.mostrarError("Error matemático: ${e.message}")  
    logger.error("División por cero", e)  
} catch (e: SQLException) {  
    consola.mostrarError("Error de base de datos: Código ${e.errorCode}")  
}  
```  

**Resultado**:
- Los usuarios ven mensajes específicos como *"Error matemático: División por cero"*.
- Los logs técnicos incluyen detalles para depuración (códigos de error SQL).

---

## Implementación de un Menú de Historial en la Consola

Para cumplir con el requisito de consultar operaciones pasadas, añadí un menú interactivo:

**Flujo del menú**:
1. Nueva operación
2. Ver últimas 10 operaciones
3. Buscar por resultado
4. Salir

**Implementación en `Consola.kt`**:
```kotlin  
fun mostrarMenuHistorial() {
  println("\n───────────────────────────")
  println("       Menú Historial      ")
  println("───────────────────────────")
  println("[1] Ver últimas 10 operaciones")
  println("[2] Buscar por resultado")
  println("[3] Volver al menú principal")
  print("\n[+] Seleccione una opción: ")
}
```  

**Desafío**:
- Al recuperar muchas operaciones (ej: 1000), la consola se saturaba.
- **Solución**: Implementé paginación, mostrando 10 resultados por vez.

---

## Optimización de Consultas con Índices

Al probar con grandes volúmenes de datos (~10k operaciones), las búsquedas por resultado eran lentas.

**Consulta original**:
```sql  
SELECT * FROM operations WHERE resultado = 5.0  
```  

**Optimización**:
1. Añadí un índice en la columna `resultado`:
```sql  
CREATE INDEX idx_resultado ON operations(resultado)  
```  

2. Modifiqué la consulta para usar rangos:
```sql  
SELECT * FROM operations WHERE resultado BETWEEN 4.99 AND 5.01  
```  

**Resultado**:
- Tiempo de búsqueda reducido de 1200ms a 45ms para 10k registros.

---

## Migración a PreparedStatements Globales

Inicialmente, algunas consultas usaban concatenación de strings, lo que era vulnerable a inyección SQL.

**Código vulnerable**:
```kotlin  
fun buscarPorOperacion(operacion: String): List<Operation> {  
    val query = "SELECT * FROM operations WHERE operacion = '$operacion'"  
    //  Riesgo de inyección SQL si operacion contiene '; DROP TABLE...'  
}  
```  

**Refactorización**:
```kotlin  
fun buscarPorOperacion(operacion: String): List<Operation> {  
    return connection.prepareStatement("SELECT * FROM operations WHERE operacion = ?").use {  
        it.setString(1, operacion)  
        // ...  
    }  
}  
```  

**Verificación**:
- Probé valores maliciosos como `"'; DROP TABLE operations;--"`, verificando que la tabla no se eliminara.

---

## Unificación del Formato de Fechas

Existían inconsistencias en cómo se manejaban las fechas: la base de datos usaba `TIMESTAMP`.

**Solución**:
1. Establecí un formato único en `Consola.kt`:
```kotlin  
private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")  

fun formatearFecha(fecha: Date): String {  
    return dateFormat.format(fecha)  
}  
```  

2. Actualicé las consultas para usar zonas horarias consistentes:
```kotlin  
statement.setTimestamp(3, Timestamp(operation.fecha.time))  
```

---
