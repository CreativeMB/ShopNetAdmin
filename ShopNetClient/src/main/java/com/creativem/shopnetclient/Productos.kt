package com.creativem.shopnetclient

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.creativem.shopnetclient.databinding.ProductosBinding
import com.google.firebase.database.*

class Productos : AppCompatActivity() {

    private lateinit var binding: ProductosBinding
    private lateinit var db: DatabaseReference
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var adapter: ProductosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Evitar que la pantalla se apague
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Ajustar padding por barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pantalla completa / modo kiosco
        val controller = ViewCompat.getWindowInsetsController(binding.root)
        controller?.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Base de datos Firebase
        db = FirebaseDatabase.getInstance().reference

        // Inicializar adapter
        adapter = ProductosAdapter(listaProductos)

        // Configurar RecyclerView
        binding.recyclerViewProductos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewProductos.adapter = adapter


        binding.fabAgregar.setOnClickListener {
            val dialog = WebViewDialogFragment("https://www.floristerialoslirios.com/orden-compra")
            dialog.show(supportFragmentManager, "webview_dialog")
        }



        // Cargar productos
        cargarProductos()
    }

    // Mantener modo kiosco siempre activo
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ViewCompat.getWindowInsetsController(binding.root)?.hide(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
            )
        }
    }

    private fun cargarProductos() {
        db.child("AYnkeosYGSSwsJV4aeoYcMybOwy1").child("productos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaProductos.clear()
                    for (prodSnap in snapshot.children) {
                        val producto = prodSnap.getValue(Producto::class.java)
                        if (producto != null) listaProductos.add(producto)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Productos, "Error cargando productos", Toast.LENGTH_SHORT).show()
                }
            })
    }



}
