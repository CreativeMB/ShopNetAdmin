package com.creativem.shopnetclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.creativem.shopnetclient.databinding.ProductosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Productos : AppCompatActivity() {

    private lateinit var binding: ProductosBinding
    private lateinit var db: DatabaseReference
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var adapter: ProductosAdapter
    private var idEmpresa: String = "" // <-- ID de la empresa seleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajustar padding por barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener idEmpresa desde el Intent
        idEmpresa = intent.getStringExtra("idEmpresa") ?: ""
        if (idEmpresa.isEmpty()) {
            Toast.makeText(this, "No se recibió el ID de la empresa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = FirebaseDatabase.getInstance().reference

        // Inicializar adapter
        adapter = ProductosAdapter(listaProductos)

        // Configurar RecyclerView
        binding.recyclerViewProductos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewProductos.adapter = adapter

        // Cargar productos
        cargarProductos()
    }

    private fun cargarProductos() {
        db.child(idEmpresa).child("productos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaProductos.clear()
                    for (prodSnap in snapshot.children) {
                        val producto = prodSnap.getValue(Producto::class.java)
                        if (producto != null) listaProductos.add(producto)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
