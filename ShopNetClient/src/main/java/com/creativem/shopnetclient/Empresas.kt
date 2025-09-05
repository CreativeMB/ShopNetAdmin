package com.creativem.shopnetclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.creativem.shopnetclient.databinding.EmpresasBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class Empresas : AppCompatActivity() {

    private lateinit var binding: EmpresasBinding
    private lateinit var adapterEmpresas: EmpresasAdapter
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val listaEmpresas = mutableListOf<Empresa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Binding
        binding = EmpresasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar RecyclerView con tu nuevo adapter
        adapterEmpresas = EmpresasAdapter(listaEmpresas) { empresa ->
            val intent = Intent(this, Productos::class.java)
            intent.putExtra("idEmpresa", empresa.idEmpresa)
            intent.putExtra("nombreEmpresa", empresa.nombre)
            startActivity(intent)
        }
        adapterEmpresas = adapterEmpresas
        binding.recyclerEmpresas.layoutManager = LinearLayoutManager(this)
        binding.recyclerEmpresas.adapter = adapterEmpresas

        // Cargar datos desde Firestore
        cargarEmpresas()
    }

    private fun cargarEmpresas() {
        val ref = FirebaseDatabase.getInstance().reference

        listaEmpresas.clear()

        ref.get().addOnSuccessListener { snapshot ->
            for (empresaSnapshot in snapshot.children) {
                val perfilSnapshot = empresaSnapshot.child("perfilEmpresa")
                val empresa = perfilSnapshot.getValue(Empresa::class.java)

                empresa?.let {
                    // Le pasamos también el idEmpresa
                    listaEmpresas.add(it.copy(idEmpresa = empresaSnapshot.key ?: ""))
                }
            }
            adapterEmpresas.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al cargar empresas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
