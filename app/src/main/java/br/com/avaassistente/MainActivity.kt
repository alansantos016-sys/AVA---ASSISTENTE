package br.com.avaassistente

import android.app.Activity
import android.os.Bundle
import android.graphics.Bitmap
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Protótipo de navegação para o AVA.
 * O aluno realiza autenticação diretamente no site institucional.
 * Não capturamos nem armazenamos login ou senha.
 */
class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var status: TextView

    private val avaUrl = "https://www.anhanguera.com/aluno/ava/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 8)
        }

        status = TextView(this).apply {
            text = "AVA Assistente • Faça login no portal"
            textSize = 18f
            setPadding(0, 0, 0, 8)
        }
        root.addView(status)

        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        toolbar.addView(Button(this).apply {
            text = "Abrir AVA"
            setOnClickListener { webView.loadUrl(avaUrl) }
        })
        toolbar.addView(Button(this).apply {
            text = "Voltar"
            setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        })
        toolbar.addView(Button(this).apply {
            text = "Atualizar"
            setOnClickListener { webView.reload() }
        })
        root.addView(toolbar)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    status.text = "Carregando página do AVA…"
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    status.text = "Portal aberto • Autentique-se no próprio AVA"
                }
            }
            webChromeClient = WebChromeClient()
        }

        root.addView(webView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(avaUrl)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
