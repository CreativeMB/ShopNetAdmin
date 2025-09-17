package com.creativem.shopnetclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.creativem.shopnetclient.databinding.ActivityWebFormBinding

class WebFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebFormBinding

    private val FILE_CHOOSER_REQUEST_CODE = 1001
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: "https://www.floristerialoslirios.com/orden-compra"

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.allowFileAccess = true
        binding.webView.settings.allowContentAccess = true

        binding.webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@WebFormActivity.filePathCallback?.onReceiveValue(null)
                this@WebFormActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    intent?.let { startActivityForResult(it, FILE_CHOOSER_REQUEST_CODE) }
                } catch (e: Exception) {
                    Toast.makeText(this@WebFormActivity, "No se puede abrir el selector de archivos", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
        }

        binding.webView.loadUrl(url)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            filePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            )
            filePathCallback = null
        }
    }
}
