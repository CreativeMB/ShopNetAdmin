package com.creativem.shopnetadmin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.creativem.shopnetadmin.databinding.ProductosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Productos : AppCompatActivity() {

    private lateinit var binding: ProductosBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var adapter: ProductosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Inicializar Firebase antes de usarlo
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // RecyclerView con adapter y click
        adapter = ProductosAdapter(listaProductos) { producto ->
            val intent = Intent(this, CrearProductos::class.java)
            intent.putExtra("producto", producto)
            startActivity(intent)
        }
        binding.recyclerViewProductos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProductos.adapter = adapter

        // ✅ Ahora sí cargamos los productos
        cargarProductos()
    }


    private fun cargarProductos() {
        val idEmpresa = mAuth.currentUser?.uid ?: return
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
