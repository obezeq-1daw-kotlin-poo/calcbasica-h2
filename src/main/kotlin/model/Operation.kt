package model

import java.util.Date

data class Operation(
    val id: Long? = null,
    val operacion: String,
    val resultado: Double,
    val fecha: Date = Date()
)