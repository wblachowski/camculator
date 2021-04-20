package com.github.wblachowski.camculator.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.model.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.model.result.equation.Solution
import com.github.wblachowski.camculator.utils.PixelConverter
import kotlinx.android.synthetic.main.solutions_view.view.*

class SolutionsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val pixelConverter: PixelConverter
    private var lastSolutions = listOf<Solution>()

    init {
        addView((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.solutions_view, null))
        pixelConverter = PixelConverter(resources.displayMetrics)
    }

    fun updateSolutions(result: EquationProcessingResult) {
        visibility = if (result.correct) View.VISIBLE else View.GONE
        if (lastSolutions != result.solutions) {
            createSolutionViews(solutionsHolder, result.solutions)
            lastSolutions = result.solutions
        }
    }

    private fun createSolutionViews(solutionsHolder: LinearLayout, solutions: List<Solution>) {
        solutionsHolder.removeAllViews()
        solutions.forEachIndexed { i, solution ->
            val linearView = LinearLayout(context)
            solutionsHolder.addView(linearView)
            if (i != solutions.size - 1) {
                linearView.layoutParams = (linearView.layoutParams as LayoutParams).apply {
                    bottomMargin = pixelConverter.fromDp(8).toInt()
                }
            }
            val mathView = ScrollableMathView(context, null)
            mathView.setText(solution.latexStringRepresentation)
            linearView.addView(mathView)
        }
    }
}