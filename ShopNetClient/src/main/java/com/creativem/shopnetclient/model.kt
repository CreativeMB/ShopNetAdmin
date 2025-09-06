package com.creativem.shopnetclient


data class Empresa(
    val idEmpresa: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val whatsapp: String = "",
    val telefonoFijo: String = "",
    val email: String = "",
    val logoUrl: String = "",
    val portadaUrl: String = "",
    val webUrl: String = "",
    val direccion: String = "",
    val ciudad: String = "",
    val pais: String = "",
    val horarioAtencion: String = "",
    val fechaCreacion: String = ""
)
data class Producto(
    var idProducto: String = "",
    var nombre: String = "",
    var referencia: String = "",
    var descripcion: String = "",
    var valor: Int = 0,
    var valorPromocion: Int? = null,
    var promocion: Boolean = false,
    var imagenUrl: String = "",
    var agotado: Boolean = false,
    var categoria: String = ""
)
