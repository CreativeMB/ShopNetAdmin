package com.creativem.shopnetadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.creativem.shopnetadmin.databinding.PerfilEmpresaBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PerfilEmpresa : AppCompatActivity() {

    private lateinit var binding: PerfilEmpresaBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = PerfilEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Inicializar Firebase y Google
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Cargar datos del usuario autenticado
        val user = mAuth.currentUser
        user?.let {
            binding.etEmail.setText(it.email ?: "Sin correo")
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(System.currentTimeMillis()))
            binding.etFechaCreacion.setText(fecha)

            Glide.with(this)
                .load(it.photoUrl)
                .placeholder(R.drawable.icono)
                .into(binding.ivFoto)
        }

        // Guardar empresa
        binding.tvGuardar.setOnClickListener {
            guardarEmpresa()
        }

        // 🔹 Botón cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            mAuth.signOut()
            mGoogleSignInClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }
    }

    private fun guardarEmpresa() {
        val user = mAuth.currentUser ?: return
        val idEmpresa = user.uid // 👈 usamos el uid del usuario como idEmpresa

        val nombre = binding.etNombre.text.toString().trim()
        val whatsapp = binding.etWhatsapp.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()

        if (nombre.isEmpty() || whatsapp.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val empresa = Empresa(
            idEmpresa = idEmpresa,
            nombre = nombre,
            descripcion = binding.etDescripcion.text.toString().trim(),
            categoria = binding.etCategoria.text.toString().trim(),
            whatsapp = whatsapp,
            telefonoFijo = binding.etTelefono.text.toString().trim(),
            email = user.email ?: "",
            logoUrl = binding.etLogoUrl.text.toString().trim(),
            portadaUrl = binding.etPortadaUrl.text.toString().trim(),
            webUrl = binding.etWeb.text.toString().trim(),
            facebookUrl = binding.etFacebook.text.toString().trim(),      // Nuevo campo
            instagramUrl = binding.etInstagram.text.toString().trim(),    // Nuevo campo
            direccion = direccion,
            ciudad = binding.etCiudad.text.toString().trim(),
            pais = binding.etPais.text.toString().trim(),
            horarioAtencion = binding.etHorario.text.toString().trim(),
            fechaCreacion = binding.etFechaCreacion.text.toString()
        )

        FirebaseDatabase.getInstance().reference
            .child(idEmpresa)
            .child("perfilEmpresa")
            .setValue(empresa)
            .addOnSuccessListener {
                Toast.makeText(this, "Empresa guardada correctamente ✅", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CrearProductos::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la empresa ❌", Toast.LENGTH_SHORT).show()
            }
    }
}
