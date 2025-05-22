# Calculadora en H2

- Rama: `main`

---

# Índice

[**1. Introducción**](#1-introducción)

[**2. Proceso de creación**](#2-proceso-de-creación)
- [**Configuración inicial de la base de datos**](#configuración-inicial-de-la-base-de-datos)
- [**Implementación de HikariCP y Primeros Problemas de Conexión**](#implementación-de-hikaricp-y-primeros-problemas-de-conexión)
- [**s**]()
- [**s**]()
- [**s**]()
- [**s**]()
- [**s**]()
- [**s**]()
- [**s**]()

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
