package com.github.wblachowski.camculator.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.model.result.equation.EquationProcessingResult
import kotlinx.android.synthetic.main.equations_view.view.*

class EquationsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    init {
        addView((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.equations_view, null))
    }

    fun updateEquations(result: EquationProcessingResult, hardUpdate: Boolean) {
        equationsTitle.text = when {
            result.equations.isEmpty() -> context.getString(R.string.equations_not_found)
            result.correct -> context.getString(R.string.equations)
            else -> context.getString(R.string.equations_incorrect)
        }
        equationsTitle.setTextColor(if (result.correct) resources.getColor(R.color.white) else resources.getColor(R.color.red))
        if (hardUpdate) {
            hardUpdate(result)
        } else {
            softUpdate(result)
        }
    }

    private fun hardUpdate(result: EquationProcessingResult) {
        if (equationsMathViewWrapper.childCount == 0 || result.latexEquations != (equationsMathViewWrapper.getChildAt(0) as ScrollableMathView).getText()) {
            equationsMathViewWrapper.removeAllViews()
            equationsMathViewWrapper.addView(ScrollableMathView(context).apply {
                setText(result.latexEquations)
            })
        }
    }

    private fun softUpdate(result: EquationProcessingResult) {
        if (equationsMathViewWrapper.childCount == 0) {
            equationsMathViewWrapper.removeAllViews()
            equationsMathViewWrapper.addView(ScrollableMathView(context).apply {
                setText(result.latexEquations)
            })
        } else if (result.latexEquations != (equationsMathViewWrapper.getChildAt(0) as ScrollableMathView).getText()) {
            (equationsMathViewWrapper.getChildAt(0) as ScrollableMathView).setText(result.latexEquations)
        }
    }
}

