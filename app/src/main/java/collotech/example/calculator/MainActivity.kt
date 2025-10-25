package collotech.example.calculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.HorizontalScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import collotech.example.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import android.widget.ImageView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var expression: String = ""
    private var resultShown: Boolean = false
    private var isClearMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set text colors explicitly to ensure visibility
        binding.expressiontxt.setTextColor(resources.getColor(android.R.color.black, null))
        binding.output.setTextColor(resources.getColor(android.R.color.black, null))

        setNumberClickListeners()
        setOperatorClickListeners()
        setEqualAndDeleteListeners()

        val arrow = findViewById<ImageView>(R.id.arrow)

        arrow.setOnClickListener {
            val intent = Intent(this, scientific::class.java)
            startActivity(intent)
        }
    }

    private fun setNumberClickListeners() {
        val numberButtons = listOf(
            binding.num0, binding.num1, binding.num2, binding.num3,
            binding.num4, binding.num5, binding.num6,
            binding.num7, binding.num8, binding.num9, binding.dot
        )

        numberButtons.forEach { btn ->
            btn.setOnClickListener {
                if (resultShown) {
                    expression = ""
                    binding.output.text = ""
                    resultShown = false
                    // Reset text color back to black when starting new expression
                    binding.expressiontxt.setTextColor(resources.getColor(android.R.color.black, null))
                    resetDelButton()
                }

                expression += btn.text.toString()
                binding.expressiontxt.text = expression
                scrollToRight(R.id.expressionScrollView)

                try {
                    val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                    binding.output.text = formatResult(result)
                    scrollToRight(R.id.outputScrollView)
                } catch (_: Exception) {
                    binding.output.text = ""
                }
            }
        }
    }

    private fun setOperatorClickListeners() {
        val operatorButtons = listOf(
            binding.addition, binding.subtract,
            binding.multiple, binding.divide
        )

        operatorButtons.forEach { btn ->
            btn.setOnClickListener {
                val operator = btn.text.toString()

                // Allow minus at the start for negative numbers
                if (expression.isEmpty() && operator == "-") {
                    expression = operator
                    binding.expressiontxt.text = expression
                    scrollToRight(R.id.expressionScrollView)
                    return@setOnClickListener
                }

                // If expression is not empty
                if (expression.isNotEmpty()) {
                    // If last character is an operator, replace it with the new operator
                    if (isLastCharOperator()) {
                        expression = expression.dropLast(1) + operator
                        binding.expressiontxt.text = expression
                        scrollToRight(R.id.expressionScrollView)
                    } else {
                        // Add operator normally
                        resultShown = false
                        resetDelButton()
                        expression += operator
                        binding.expressiontxt.text = expression
                        scrollToRight(R.id.expressionScrollView)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEqualAndDeleteListeners() {
        binding.equal.setOnClickListener {
            try {
                val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                val resultString = formatResult(result)

                // Animate result upward
                binding.output.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(250)
                    .withEndAction {
                        binding.expressiontxt.text = resultString
                        // Change text color to green for the result
                        binding.expressiontxt.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                        scrollToRight(R.id.expressionScrollView)
                        binding.output.text = ""
                        binding.output.alpha = 1f
                        binding.output.translationY = 0f
                        expression = resultString
                        resultShown = true

                        // Change DEL to CLR after showing result
                        binding.del.text = "CLR"
                        isClearMode = true
                    }
                    .start()

            } catch (_: Exception) {
                binding.output.text = "Error"
            }
        }

        binding.del.setOnClickListener {
            if (isClearMode) {
                // 🧹 Clear everything
                expression = ""
                binding.expressiontxt.text = ""
                binding.output.text = ""
                resultShown = false
                resetDelButton()
            } else {
                //  Normal DEL behavior
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    binding.expressiontxt.text = expression
                    scrollToRight(R.id.expressionScrollView)

                    try {
                        val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                        binding.output.text = formatResult(result)
                        scrollToRight(R.id.outputScrollView)
                    } catch (_: Exception) {
                        binding.output.text = ""
                    }
                }
            }
        }
    }

    // Restore DEL button state
    @SuppressLint("SetTextI18n")
    private fun resetDelButton() {
        binding.del.text = "DEL"
        isClearMode = false
    }

    private fun isLastCharOperator(): Boolean {
        if (expression.isEmpty()) return false
        val lastChar = expression.last()
        return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/'
    }

    // Preprocess expression to handle negative numbers properly
    private fun preprocessExpression(expr: String): String {
        if (expr.isEmpty()) return expr

        var processed = expr

        // Handle negative number at the start: -5 becomes (0-5)
        if (processed.startsWith("-")) {
            processed = "(0$processed)"
        }

        // No need to handle +- or -- since we now replace operators
        // The expression will always have single operators between numbers

        return processed
    }

    // Format result to remove unnecessary decimal zeros
    private fun formatResult(result: Double): String {
        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    }

    // Scroll HorizontalScrollView to the right to show latest digits
    private fun scrollToRight(scrollViewId: Int) {
        val scrollView = findViewById<HorizontalScrollView>(scrollViewId)
        scrollView?.postDelayed({
            scrollView.fullScroll(android.view.View.FOCUS_RIGHT)
        }, 100)
    }
}