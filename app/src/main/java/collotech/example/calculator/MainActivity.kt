package collotech.example.calculator

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
                    binding.output.setText("")
                    resultShown = false
                    resetDelButton()
                }

                expression += btn.text.toString()
                binding.expressiontxt.setText(expression)
                scrollToRight(R.id.expressionScrollView)

                try {
                    val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                    binding.output.setText(formatResult(result))
                    scrollToRight(R.id.outputScrollView)
                } catch (e: Exception) {
                    binding.output.setText("")
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
                    binding.expressiontxt.setText(expression)
                    scrollToRight(R.id.expressionScrollView)
                    return@setOnClickListener
                }

                // Allow minus after an operator for negative numbers (e.g., "5+-2")
                if (expression.isNotEmpty()) {
                    if (operator == "-" && isLastCharOperator() && expression.last() != '-') {
                        resultShown = false
                        resetDelButton()
                        expression += operator
                        binding.expressiontxt.setText(expression)
                        scrollToRight(R.id.expressionScrollView)
                    } else if (!isLastCharOperator()) {
                        resultShown = false
                        resetDelButton()
                        expression += operator
                        binding.expressiontxt.setText(expression)
                        scrollToRight(R.id.expressionScrollView)
                    }
                }
            }
        }
    }

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
                        binding.expressiontxt.setText(resultString)
                        scrollToRight(R.id.expressionScrollView)
                        binding.output.setText("")
                        binding.output.alpha = 1f
                        binding.output.translationY = 0f
                        expression = resultString
                        resultShown = true

                        // Change DEL to CLR after showing result
                        binding.del.text = "CLR"
                        isClearMode = true
                    }
                    .start()

            } catch (e: Exception) {
                binding.output.setText("Error")
            }
        }

        binding.del.setOnClickListener {
            if (isClearMode) {
                // 🧹 Clear everything
                expression = ""
                binding.expressiontxt.setText("")
                binding.output.setText("")
                resultShown = false
                resetDelButton()
            } else {
                //  Normal DEL behavior
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    binding.expressiontxt.setText(expression)
                    scrollToRight(R.id.expressionScrollView)

                    try {
                        val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                        binding.output.setText(formatResult(result))
                        scrollToRight(R.id.outputScrollView)
                    } catch (e: Exception) {
                        binding.output.setText("")
                    }
                }
            }
        }
    }

    // Restore DEL button state
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

        // Handle negative numbers after operators: 5+-2 becomes 5+(0-2)
        processed = processed.replace("+-", "+(0-")
        processed = processed.replace("--", "-(0-")
        processed = processed.replace("*-", "*(0-")
        processed = processed.replace("/-", "/(0-")

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