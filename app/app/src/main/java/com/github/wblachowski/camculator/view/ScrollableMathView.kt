package com.github.wblachowski.camculator.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import com.github.wblachowski.camculator.R
import com.x5.template.Chunk
import com.x5.template.Theme
import com.x5.template.providers.AndroidTemplates
import io.github.kexanie.library.MathView


class ScrollableMathView(context: Context?, attrs: AttributeSet?) : WebView(context, attrs) {
    private var mText: String? = null
    private var mConfig: String? = null
    private var mEngine = 0

    init {
        settings.javaScriptEnabled = true
        isVerticalScrollBarEnabled = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        setBackgroundColor(Color.TRANSPARENT)

        val mTypeArray = context!!.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MathView,
                0, 0
        )

        try {
            setEngine(mTypeArray.getInteger(R.styleable.MathView_engine, 0))
            setText(mTypeArray.getString(R.styleable.MathView_text))
        } finally {
            mTypeArray.recycle()
        }
    }

    override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int, scrollRangeY: Int, maxOverScrollX: Int, maxOverScrollY: Int, isTouchEvent: Boolean): Boolean {
        return super.overScrollBy(deltaX, 0, scrollX, 0, scrollRangeX, 0, maxOverScrollX, 0, isTouchEvent)
    }

    private fun getChunk(): Chunk {
        val TEMPLATE_KATEX = "katex"
        val TEMPLATE_MATHJAX = "mathjax"
        var template = TEMPLATE_KATEX
        val loader = AndroidTemplates(context)
        when (mEngine) {
            Engine.KATEX -> template = TEMPLATE_KATEX
            Engine.MATHJAX -> template = TEMPLATE_MATHJAX
        }
        return Theme(loader).makeChunk(template)
    }

    fun setText(text: String?) {
        mText = text
        val chunk = getChunk()
        val TAG_FORMULA = "formula"
        val TAG_CONFIG = "config"
        chunk[TAG_FORMULA] = mText
        chunk[TAG_CONFIG] = mConfig
        loadDataWithBaseURL(null, chunk.toString(), "text/html", "utf-8", "about:blank")
    }

    fun getText() = mText

    fun config(config: String?) {
        if (mEngine == MathView.Engine.MATHJAX) {
            mConfig = config
        }
    }

    fun setEngine(engine: Int) {
        mEngine = when (engine) {
            Engine.KATEX -> Engine.KATEX
            Engine.MATHJAX -> Engine.MATHJAX
            else -> Engine.KATEX
        }
    }

    object Engine {
        const val KATEX = 0
        const val MATHJAX = 1
    }
}