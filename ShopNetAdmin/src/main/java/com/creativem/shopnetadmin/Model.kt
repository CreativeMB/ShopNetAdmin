package com.creativem.shopnetadmin
import java.io.Serializable
data class Empresa(
    val idEmpresa: String = "",              // UID único de la empresa en Firebase
    val nombre: String = "",          // Nombre comercial de la empresa
    val logoUrl: String = "",         // URL del logo
    val whatsapp: String = "",        // Número de WhatsApp
    val telefonoFijo: String = "",    // Teléfono local (opcional)
    val email: String = "",           // Correo electrónico de contacto
    val webUrl: String = "",          // Página web oficial
    val direccion: String = "",       // Dirección física principal
    val ciudad: String = "",          // Ciudad de la empresa
    val pais: String = "",            // País
    val fechaCreacion: String = "",   // Fecha de registro en el sistema
    val descripcion: String = "",     // Descripción corta
    val categoria: String = "",       // Categoría de negocio (ej: Floristería, Restaurante)
    val horarioAtencion: String = "", // Horarios de apertura/cierre
    val calificacion: Double = 0.0,   // Promedio de reseñas (ej: 4.5 estrellas)
    val portadaUrl: String = ""       // Imagen de portada/banner
)

data class Producto(
    var idProducto: String? = null,
    var referencia: String? = null,
    var nombre: String? = null,
    var categoria: String? = null,
    var descripcion: String? = null,
    var valor: Double? = null,
    var valorPromocion: Double? = null,
    var imagenUrl: String? = null,
    var promocion: Boolean = false,
    var agotado: Boolean = false
) : Serializable {
    constructor() : this(
        idProducto = null,
        referencia = null,
        nombre = null,
        categoria = null,
        descripcion = null,
        valor = null,
        valorPromocion = null,
        imagenUrl = null,
        promocion = false,
        agotado = false
    )
}