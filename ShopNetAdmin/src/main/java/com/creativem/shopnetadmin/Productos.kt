package com.creativem.shopnetadmin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
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

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // Adapter con click y long click
        adapter = ProductosAdapter(listaProductos,
            onProductoClick = { producto ->
                // Click normal: editar producto
                val intent = Intent(this, CrearProductos::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onProductoLongClick = { producto ->
                // Click largo: eliminar producto
                mostrarDialogoEliminar(producto)
            }
        )

        val layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewProductos.layoutManager = layoutManager
        binding.recyclerViewProductos.adapter = adapter

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

    private fun mostrarDialogoEliminar(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Sí") { dialog, _ ->
                eliminarProducto(producto)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun eliminarProducto(producto: Producto) {
        val idEmpresa = mAuth.currentUser?.uid ?: return
        val idProducto = producto.idProducto ?: return

        db.child(idEmpresa).child("productos").child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Producto eliminado ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar ❌", Toast.LENGTH_SHORT).show()
            }
    }
}
