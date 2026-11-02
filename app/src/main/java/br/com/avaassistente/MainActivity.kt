package br.com.avaassistente

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

/**
 * Protótipo de navegação e leitura assistida do AVA.
 * O aluno autentica-se diretamente no portal institucional.
 * Login e senha não são capturados nem armazenados pelo aplicativo.
 */
class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var status: TextView

    private val avaUrl = "https://www.anhanguera.com/aluno/ava/login"
    private val prefs by lazy { getSharedPreferences("study_notes", Context.MODE_PRIVATE) }

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
        root.addView(Button(this).apply {
            text = "Caderno de estudos"
            setOnClickListener { openStudyNotebook() }
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
                    status.text = "Página aberta • Leitura assistida e caderno disponíveis"
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

    private fun openStudyNotebook() {
        val editor = EditText(this).apply {
            hint = "Cole aqui o texto da aula ou escreva suas anotações…"
            setText(prefs.getString("notes", ""))
            minLines = 8
            maxLines = 18
            setPadding(28, 20, 28, 20)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Caderno de estudos")
            .setMessage("As anotações ficam salvas localmente neste aparelho.")
            .setView(editor)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Fechar", null)
            .setNeutralButton("Copiar pedido de resumo", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                prefs.edit().putString("notes", editor.text.toString()).apply()
                status.text = "Anotações salvas neste aparelho"
                dialog.dismiss()
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val studyPrompt = "Com base no material abaixo, produza um resumo organizado para estudo. " +
                    "Explique os conceitos com clareza, diferencie termos semelhantes, apresente exemplos " +
                    "e finalize com 5 perguntas de revisão com gabarito comentado. Não invente conteúdo.\n\n" +
                    editor.text.toString()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Material para resumo", studyPrompt))
                status.text = "Pedido de resumo copiado. Cole em uma ferramenta de IA para estudar."
            }
        }
        dialog.show()
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
              const result = {title, headings, paragraphs, hasVideo, nextLabel: next ? clean(next.innerText) : ''};
              AVAReader.onPageRead(JSON.stringify(result));
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private inner class PageReader {
        @JavascriptInterface
        fun onPageRead(json: String) {
            runOnUiThread {
                try {
                    val data = JSONObject(json)
                    val title = data.optString("title", "Página sem título")
                    val headings = data.optJSONArray("headings")
                    val paragraphs = data.optJSONArray("paragraphs")
                    val text = StringBuilder(title).append("\n\n")

                    if (headings != null && headings.length() > 0) {
                        text.append("Tópicos identificados:\n")
                        for (i in 0 until headings.length()) {
                            text.append("• ").append(headings.optString(i)).append("\n")
                        }
                        text.append("\n")
                    }

                    if (paragraphs != null && paragraphs.length() > 0) {
                        text.append("Texto encontrado:\n")
                        for (i in 0 until paragraphs.length()) {
                            text.append(paragraphs.optString(i)).append("\n\n")
                        }
                    } else {
                        text.append("Não encontrei parágrafos de texto nesta página. ")
                        text.append("Pode ser um vídeo, uma atividade ou um conteúdo carregado de outra forma.\n\n")
                    }

                    text.append("Vídeo detectado: ")
                        .append(if (data.optBoolean("hasVideo")) "sim" else "não")
                    val nextLabel = data.optString("nextLabel")
                    if (nextLabel.isNotBlank()) text.append("\nBotão de avanço: ").append(nextLabel)

                    text.append("\n\nA leitura apenas organiza elementos visíveis. ")
                        .append("O aplicativo não responde nem envia avaliações.")

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Leitura assistida")
                        .setMessage(text.toString())
                        .setPositiveButton("Copiar texto") { _, _ ->
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Conteúdo AVA", text.toString()))
                            status.text = "Texto identificado copiado para a área de transferência"
                        }
                        .setNegativeButton("Fechar", null)
                        .show()
                } catch (_: Exception) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Leitura assistida")
                        .setMessage("Não foi possível interpretar esta página. Tente novamente após o conteúdo terminar de carregar.")
                        .setPositiveButton("Fechar", null)
                        .show()
                }
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
