package com.creativem.shopnetadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creativem.shopnetadmin.databinding.LoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginDebug", "Launcher activado")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            Log.d("LoginDebug", "Cuenta obtenida: ${account?.email}")
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: ApiException) {
            Log.e("LoginDebug", "Error en login Google: ${e.statusCode}", e)
            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()


        binding.tvGoogleLogin.setOnClickListener {
            Log.d("LoginDebug", "Usuario hizo clic en Google Login")
            signInWithGoogle()
        }
    }

    override fun onStart() {
        super.onStart()
        // 👉 Revisar si ya hay usuario autenticado
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            Log.d("LoginDebug", "Usuario ya autenticado: ${currentUser.email}")
            checkPerfilEmpresa(currentUser)
        }
    }

    private fun signInWithGoogle() {
        Log.d("LoginDebug", "Iniciando Google Sign-In")
        val signInIntent = mGoogleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LoginDebug", "Autenticando con Firebase: ${acct.email}")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = mAuth.currentUser
                Log.d("LoginDebug", "Autenticación exitosa: ${user?.displayName}")
                Toast.makeText(this, "Bienvenido ${user?.displayName}", Toast.LENGTH_SHORT).show()

                user?.let { checkPerfilEmpresa(it) }
            } else {
                Log.e("LoginDebug", "Fallo la autenticación", task.exception)
                Toast.makeText(this, "Falló la autenticación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPerfilEmpresa(user: FirebaseUser) {
        val dbRef = FirebaseDatabase.getInstance().reference
            .child(user.uid) // 👈 primero uid como raíz
            .child("perfilEmpresa")

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d("LoginDebug", "Perfil existente, redirigiendo a CrearProductos")
                startActivity(Intent(this, CrearProductos::class.java))
            } else {
                Log.d("LoginDebug", "Perfil no existe, redirigiendo a PerfilEmpresa")
                startActivity(Intent(this, PerfilEmpresa::class.java))
            }
            finish()
        }.addOnFailureListener {
            Log.e("LoginDebug", "Error al verificar perfil", it)
            Toast.makeText(this, "Error al verificar perfil", Toast.LENGTH_SHORT).show()
        }
    }
}
