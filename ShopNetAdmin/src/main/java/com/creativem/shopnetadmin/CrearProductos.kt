package com.creativem.shopnetadmin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CrearProductos : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: CrearProductosBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseDatabase.getInstance().reference
    private var productoEditar: Producto? = null
    private val referenciasDrive = mutableMapOf<String, String>()

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val delayMillis: Long = 800 // tiempo de espera antes de consultar Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CrearProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }

        cargarJSONDesdeDrive()
        binding.etReferenciaProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val referencia = s.toString().trim()

                // Cancelar cualquier consulta pendiente
                runnable?.let { handler.removeCallbacks(it) }

                if (referencia.isNotEmpty()) {
                    // Programar consulta diferida para no saturar Firebase mientras se escribe
                    runnable = Runnable { consultarReferencia(referencia) }
                    handler.postDelayed(runnable!!, delayMillis)
                } else {
                    // Solo limpiar si el campo está vacío, así no borraremos productoEditar antes de tiempo
                    productoEditar = null
                    limpiarFormulario()
                }

                // Actualizar vista previa de imagen según JSON
                val urlImagen = referenciasDrive[referencia]
                if (!urlImagen.isNullOrEmpty()) {
                    Glide.with(this@CrearProductos)
                        .load(urlImagen)
                        .placeholder(R.drawable.icono)
                        .error(R.drawable.icono)
                        .into(binding.ivPreviewImagen)
                    binding.etUrlImagen.setText(urlImagen)
                } else {
                    binding.ivPreviewImagen.setImageDrawable(null)
                }
            }
        })
        binding.tvNuevoProducto.setOnClickListener {
            // Limpiar formulario
            limpiarFormulario()
            // Limpiar referencia
            binding.etReferenciaProducto.text?.clear()
            productoEditar = null
            ultimaReferenciaConsultada = null
            binding.ivPreviewImagen.setImageDrawable(null)
        }


        productoEditar = intent.getSerializableExtra("producto") as? Producto
        productoEditar?.let { cargarProductoEnFormulario(it) }

        binding.btnGuardarProducto.setOnClickListener { guardarProducto() }
        binding.tvIrProductos.setOnClickListener {
            startActivity(Intent(this, Productos::class.java))
        }
    }

    private var ultimaReferenciaConsultada: String? = null

    private fun consultarReferencia(referencia: String) {
        if (referencia == ultimaReferenciaConsultada) return
        ultimaReferenciaConsultada = referencia

        val idEmpresa = mAuth.currentUser?.uid ?: return
        db.child(idEmpresa).child("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                var encontrado = false
                for (child in snapshot.children) {
                    val producto = child.getValue(Producto::class.java)
                    if (producto != null && producto.referencia.equals(referencia, ignoreCase = true)) {
                        if (productoEditar?.idProducto != producto.idProducto) {
                            productoEditar = producto
                            cargarProductoEnFormulario(producto)
                            Toast.makeText(this, "Producto cargado para edición ✅", Toast.LENGTH_SHORT).show()
                        }
                        encontrado = true
                        break
                    }
                }
                if (!encontrado) {
                    productoEditar = null
                    // Aquí NO limpiamos el formulario, así no se borra lo del JSON
                    Toast.makeText(this, "Nueva referencia, listo para crear producto ✅", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar la referencia ❌", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarAutocompletado() {
        val referencias = referenciasDrive.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, referencias)
        val autoComplete = binding.etReferenciaProducto as AutoCompleteTextView
        autoComplete.setAdapter(adapter)
        autoComplete.threshold = 1

        autoComplete.setOnItemClickListener { parent, view, position, id ->
            val seleccion = parent.getItemAtPosition(position) as String
            val urlImagen = referenciasDrive[seleccion]
            if (!urlImagen.isNullOrEmpty()) {
                Glide.with(this)
                    .load(urlImagen)
                    .placeholder(R.drawable.icono)
                    .error(R.drawable.icono)
                    .into(binding.ivPreviewImagen)
                binding.etUrlImagen.setText(urlImagen)
            }
        }
    }

    private fun cargarJSONDesdeDrive() {
        val urlJSON = "https://drive.google.com/uc?export=download&id=1mGqnHpLxP3mWUPHVUyJtSb9WiwRlaQ_C"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonText = URL(urlJSON).readText()
                val json = JSONObject(jsonText)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    referenciasDrive[key] = json.getString(key)
                }
                runOnUiThread { configurarAutocompletado() }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@CrearProductos, "Error cargando JSON ❌", Toast.LENGTH_SHORT).show()
                }
            }
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
