package com.creativem.shopnetadmin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.creativem.shopnetadmin.databinding.CrearProductosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CrearProductos : AppCompatActivity() {

    private lateinit var binding: CrearProductosBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CrearProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

        // 🔹 Listener en el campo URL para mostrar la vista previa automática
        binding.etUrlImagen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val urlImagen = s.toString().trim()
                if (urlImagen.isNotEmpty()) {
                    Glide.with(this@CrearProductos)
                        .load(urlImagen)
                        .placeholder(R.drawable.icono)
                        .error(R.drawable.icono)
                        .into(binding.ivPreviewImagen)
                } else {
                    binding.ivPreviewImagen.setImageDrawable(null)
                }
            }
        })

        // Guardar producto
        binding.btnGuardarProducto.setOnClickListener {
            guardarProducto()
        }
    }

    private fun guardarProducto() {
        val referencia = binding.etReferenciaProducto.text.toString().trim()
        val nombre = binding.etNombreProducto.text.toString().trim()
        val categoria = binding.etCategoriaProducto.text.toString().trim()  // 🔹 Nueva categoría
        val descripcion = binding.etDescripcionProducto.text.toString().trim()
        val valor = binding.etValor.text.toString().toDoubleOrNull()
        val valorPromocion = binding.etValorPromocion.text.toString().toDoubleOrNull()
        val urlImagen = binding.etUrlImagen.text.toString().trim()
        val promocion = binding.switchPromocion.isChecked
        val agotado = binding.switchAgotado.isChecked

        if (referencia.isEmpty() || nombre.isEmpty() || categoria.isEmpty() || descripcion.isEmpty() || valor == null || urlImagen.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val idEmpresa = mAuth.currentUser?.uid ?: return
        val idProducto = db.child(idEmpresa).child("productos").push().key ?: UUID.randomUUID().toString()

        val producto = Producto(
            idProducto = idProducto,
            referencia = referencia,
            nombre = nombre,
            categoria = categoria,   // 🔹 Guardando la categoría
            descripcion = descripcion,
            valor = valor,
            valorPromocion = valorPromocion,
            imagenUrl = urlImagen,
            promocion = promocion,
            agotado = agotado
        )

        db.child(idEmpresa).child("productos").child(idProducto)
            .setValue(producto)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto guardado ✅", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar ❌", Toast.LENGTH_SHORT).show()
            }
    }



    private fun limpiarFormulario() {
        binding.etReferenciaProducto.text?.clear() // 🔹 limpiar referencia
        binding.etNombreProducto.text?.clear()
        binding.etDescripcionProducto.text?.clear()
        binding.etValor.text?.clear()
        binding.etValorPromocion.text?.clear()
        binding.etUrlImagen.text?.clear()
        binding.ivPreviewImagen.setImageDrawable(null)
        binding.switchPromocion.isChecked = false
        binding.switchAgotado.isChecked = false
    }

}
