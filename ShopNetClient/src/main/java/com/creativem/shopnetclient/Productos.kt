package com.creativem.shopnetclient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creativem.shopnetclient.databinding.ProductosBinding

class Productos : AppCompatActivity() {

    private lateinit var binding: ProductosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar binding
        binding = ProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mantener tu configuración de insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 📌 Recuperar idEmpresa enviado desde EmpresasAdapter
        val idEmpresa = intent.getStringExtra("idEmpresa")
        val nombreEmpresa = intent.getStringExtra("nombreEmpresa")

        if (idEmpresa != null) {
            binding.tvTitulo.text = "Productos de $nombreEmpresa"
            // Aquí más adelante cargaremos los crear_productos desde Realtime Database usando idEmpresa
        } else {
            Toast.makeText(this, "No se recibió la empresa", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
