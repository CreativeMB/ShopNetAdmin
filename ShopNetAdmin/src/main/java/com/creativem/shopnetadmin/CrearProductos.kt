package com.creativem.shopnetadmin

import android.content.Intent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable
import java.util.*

class CrearProductos : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: CrearProductosBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseDatabase.getInstance().reference

    private var productoEditar: Producto? = null // para edición

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.tvCerrarSesion.setOnClickListener {
            // 1️⃣ Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // 2️⃣ Cerrar sesión de Google
            googleSignInClient.signOut().addOnCompleteListener {
                // 3️⃣ Ir a Login y permitir elegir cuenta
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
        }



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

        // 🔹 Revisar si viene un producto para editar
        productoEditar = intent.getSerializableExtra("producto") as? Producto
        productoEditar?.let { cargarProductoEnFormulario(it) }

        // Guardar producto
        binding.btnGuardarProducto.setOnClickListener {
            guardarProducto()
        }

        binding.tvIrProductos.setOnClickListener {
            val intent = Intent(this, Productos::class.java)
            startActivity(intent)
        }
    }

    private fun cargarProductoEnFormulario(producto: Producto) {
        binding.etReferenciaProducto.setText(producto.referencia)
        binding.etNombreProducto.setText(producto.nombre)
        binding.etCategoriaProducto.setText(producto.categoria)
        binding.etDescripcionProducto.setText(producto.descripcion)
        binding.etValor.setText(producto.valor.toString())
        binding.etValorPromocion.setText(producto.valorPromocion?.toString() ?: "")
        binding.etUrlImagen.setText(producto.imagenUrl)
        binding.switchPromocion.isChecked = producto.promocion
        binding.switchAgotado.isChecked = producto.agotado

        Glide.with(this).load(producto.imagenUrl)
            .placeholder(R.drawable.icono)
            .error(R.drawable.icono)
            .into(binding.ivPreviewImagen)
    }

    private fun guardarProducto() {
        val referencia = binding.etReferenciaProducto.text.toString().trim()
        val nombre = binding.etNombreProducto.text.toString().trim()
        val categoria = binding.etCategoriaProducto.text.toString().trim()
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
        val idProducto = productoEditar?.idProducto ?: db.child(idEmpresa).child("productos").push().key ?: UUID.randomUUID().toString()

        val producto = Producto(
            idProducto = idProducto,
            referencia = referencia,
            nombre = nombre,
            categoria = categoria,
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
                if (productoEditar == null) limpiarFormulario()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar ❌", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarFormulario() {
        binding.etReferenciaProducto.text?.clear()
        binding.etNombreProducto.text?.clear()
        binding.etCategoriaProducto.text?.clear()
        binding.etDescripcionProducto.text?.clear()
        binding.etValor.text?.clear()
        binding.etValorPromocion.text?.clear()
        binding.etUrlImagen.text?.clear()
        binding.ivPreviewImagen.setImageDrawable(null)
        binding.switchPromocion.isChecked = false
        binding.switchAgotado.isChecked = false
    }
}
