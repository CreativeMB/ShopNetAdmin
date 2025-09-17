package com.creativem.shopnetclient

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.DialogFragment


class WebViewDialogFragment(private val url: String) : DialogFragment() {

    private val FILE_CHOOSER_REQUEST_CODE = 1001
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_webview_bottomsheet, null)
        dialog.setContentView(view)

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog.window?.setLayout(width, height)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val webView = view.findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.isFocusableInTouchMode = true
        webView.requestFocus()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@WebViewDialogFragment.filePathCallback?.onReceiveValue(null)
                this@WebViewDialogFragment.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    intent?.let { startActivityForResult(it, FILE_CHOOSER_REQUEST_CODE) }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se puede abrir el selector de archivos", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
        }

        webView.loadUrl(url)
        return dialog
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
