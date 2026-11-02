package br.com.avaassistente

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Bitmap
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Protótipo de navegação e leitura assistida do AVA.
 * O aluno autentica-se diretamente no portal institucional.
 * Login e senha não são capturados nem armazenados pelo aplicativo.
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
            setPadding(12, 10, 12, 8)
        }

        status = TextView(this).apply {
            text = "AVA Assistente • Entre no portal e abra um conteúdo"
            textSize = 16f
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

        root.addView(Button(this).apply {
            text = "Identificar conteúdo da página"
            setOnClickListener { readPageContent() }
        })

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            addJavascriptInterface(PageReader(), "AVAReader")
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    status.text = "Carregando página do AVA…"
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    status.text = "Página aberta • Use “Identificar conteúdo” para leitura assistida"
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

    private fun readPageContent() {
        val script = """
            (function() {
              const clean = s => (s || '').replace(/\\s+/g, ' ').trim();
              const title = clean(document.title);
              const headings = Array.from(document.querySelectorAll('h1,h2,h3'))
                .map(e => clean(e.innerText)).filter(Boolean).slice(0, 18);
              const paragraphs = Array.from(document.querySelectorAll('main p, article p, p'))
                .map(e => clean(e.innerText)).filter(s => s.length > 35).slice(0, 35);
              const hasVideo = !!document.querySelector('video, iframe[src*="youtube"], iframe[src*="vimeo"]');
              const next = Array.from(document.querySelectorAll('button,a,[role="button"]'))
                .find(e => /próximo|proximo|continuar|avançar|avancar/i.test(clean(e.innerText)));
              const nextLabel = next ? clean(next.innerText) : '';
              const result = {title, headings, paragraphs, hasVideo, nextLabel};
              AVAReader.onPageRead(JSON.stringify(result));
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private inner class PageReader {
        @JavascriptInterface
        fun onPageRead(json: String) {
            runOnUiThread {
                val safeText = json
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                    .replace("\\u0026", "&")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Leitura da página")
                    .setMessage("O aplicativo identificou os elementos do conteúdo.\n\n" +
                        "Dados extraídos da página:\n$safeText\n\n" +
                        "Este recurso organiza informações visíveis. Ele não responde nem envia avaliações.")
                    .setPositiveButton("Fechar", null)
                    .show()
            }
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
