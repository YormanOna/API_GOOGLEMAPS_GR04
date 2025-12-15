package ec.edu.monster.model

import com.google.gson.annotations.SerializedName

data class Sucursal(
    @SerializedName("codigo")
    val codigo: String = "",

    @SerializedName("nombre")
    val nombre: String = "",

    @SerializedName("ciudad")
    val ciudad: String = "",

    @SerializedName("direccion")
    val direccion: String = "",

    // En tu backend el campo se llama contadorCuentas
    @SerializedName("contadorCuentas")
    val contadorCuentas: Int = 0,

    // Pueden venir null desde DB
    @SerializedName("latitud")
    val latitud: Double? = null,

    @SerializedName("longitud")
    val longitud: Double? = null,

    // OJO: por cómo está el getter/setter en Java, a veces puede llegar como "active" o "isActive".
    // Para hacerlo robusto, leemos "active" por defecto.
    @SerializedName("active")
    val active: Boolean = true
)
