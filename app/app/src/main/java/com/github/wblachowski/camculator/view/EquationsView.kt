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

    fun updateEquations(result: EquationProcessingResult) {
        equationsTitle.text = if (result.correct) "Equations" else "Equations (incorrect)"
        equationsTitle.setTextColor(if (result.correct) resources.getColor(R.color.white) else resources.getColor(R.color.red))
        if (result.latexEquations != equationsMathView.getText()) {
            equationsMathView.setText(result.latexEquations)
        }
    }

}