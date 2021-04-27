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


class ScrollableMathView(context: Context?, attrs: AttributeSet? = null) : WebView(context, attrs) {
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
        val template = if (mEngine == Engine.KATEX) "katex" else "mathjax"
        val loader = AndroidTemplates(context)
        return Theme(loader).makeChunk(template)
    }

    fun setText(text: String?) {
        mText = text
        val chunk = getChunk()
        chunk["formula"] = mText
        chunk["config"] = mConfig
        loadDataWithBaseURL(null, chunk.toString(), "text/html", "utf-8", "about:blank")
    }

    fun getText() = mText

    private fun setEngine(engine: Int) {
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