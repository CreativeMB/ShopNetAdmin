package com.creativem.shopnetadmin

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
    var idProducto: String = "",
    var categoria: String = "",
    var nombre: String = "",
    val referencia: String = "",
    var descripcion: String = "",
    var valor: Double = 0.0,
    var valorPromocion: Double? = null,
    var imagenUrl: String = "",
    var fechaCreacion: Long = System.currentTimeMillis(),
    var agotado: Boolean = false,
    var promocion: Boolean = false
)