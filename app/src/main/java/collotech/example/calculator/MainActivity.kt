package collotech.example.calculator

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.HorizontalScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        // Set text colors for the modern dark theme
        binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
        binding.output.setTextColor(resources.getColor(android.R.color.white, null))

        setNumberClickListeners()
        setOperatorClickListeners()
        setEqualAndDeleteListeners()

        // Setup navigation arrow with animation
        val arrow = findViewById<ImageView>(R.id.arrow)
        startArrowPulseAnimation(arrow)

        arrow.setOnClickListener {
            val intent = Intent(this, scientific::class.java)
            startActivity(intent)
            // Add transition animation
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    // Pulsing animation for the navigation arrow
    private fun startArrowPulseAnimation(arrow: ImageView) {
        // Scale animation (pulse effect)
        val scaleX = ObjectAnimator.ofFloat(arrow, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(arrow, "scaleY", 1f, 1.2f, 1f)

        scaleX.duration = 1500
        scaleY.duration = 1500
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        scaleX.start()
        scaleY.start()

        // Rotation animation (slight rotation)
        val rotation = ObjectAnimator.ofFloat(arrow, "rotation", 0f, 10f, 0f, -10f, 0f)
        rotation.duration = 2000
        rotation.repeatCount = ValueAnimator.INFINITE
        rotation.interpolator = AccelerateDecelerateInterpolator()
        rotation.start()
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
                    // Reset text color back to white when starting new expression
                    binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
                    resetDelButton()
                }

                expression += btn.text.toString()
                binding.expressiontxt.text = expression
                scrollToRight(R.id.expressionScrollView)

                try {
                    val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                    if (!result.isNaN() && !result.isInfinite()) {
                        binding.output.text = formatResult(result)
                        scrollToRight(R.id.outputScrollView)
                    }
                } catch (_: Exception) {
                    binding.output.text = ""
                }
            }
        }
    }

    private fun setOperatorClickListeners() {
        // Map TextView operators to actual math operators
        val operatorMap = mapOf(
            binding.addition to "+",
            binding.subtract to "-",
            binding.multiple to "*",
            binding.divide to "/"
        )

        operatorMap.forEach { (btn, operator) ->
            btn.setOnClickListener {
                // Allow minus at the start for negative numbers
                if (expression.isEmpty() && operator == "-") {
                    expression = operator
                    binding.expressiontxt.text = expression
                    scrollToRight(R.id.expressionScrollView)
                    return@setOnClickListener
                }

                // If expression is not empty
                if (expression.isNotEmpty()) {
                    // If result was shown, continue with result
                    if (resultShown) {
                        resultShown = false
                        binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
                        resetDelButton()
                    }

                    // If last character is an operator, replace it with the new operator
                    if (isLastCharOperator()) {
                        expression = expression.dropLast(1) + operator
                    } else {
                        expression += operator
                    }

                    binding.expressiontxt.text = expression
                    scrollToRight(R.id.expressionScrollView)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEqualAndDeleteListeners() {
        binding.equal.setOnClickListener {
            try {
                val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()

                if (result.isInfinite()) {
                    binding.output.text = "∞"
                    showToast("Cannot divide by zero")
                } else if (result.isNaN()) {
                    binding.output.text = "Error"
                    showToast("Invalid expression")
                } else {
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
                }
            } catch (e: Exception) {
                binding.output.text = "Error"
                showToast(e.message ?: "Invalid expression")
            }
        }

        binding.del.setOnClickListener {
            if (isClearMode) {
                // Clear everything
                expression = ""
                binding.expressiontxt.text = ""
                binding.output.text = ""
                binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
                resultShown = false
                resetDelButton()
            } else {
                // Normal DEL behavior
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    binding.expressiontxt.text = expression
                    scrollToRight(R.id.expressionScrollView)

                    try {
                        if (expression.isNotEmpty() && !isLastCharOperator()) {
                            val result = ExpressionBuilder(preprocessExpression(expression)).build().evaluate()
                            if (!result.isNaN() && !result.isInfinite()) {
                                binding.output.text = formatResult(result)
                                scrollToRight(R.id.outputScrollView)
                            }
                        } else {
                            binding.output.text = ""
                        }
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
        return lastChar in listOf('+', '-', '*', '/')
    }

    // Preprocess expression to handle negative numbers properly
    private fun preprocessExpression(expr: String): String {
        if (expr.isEmpty()) return expr

        var processed = expr

        // Handle negative number at the start: -5 becomes (0-5)
        if (processed.startsWith("-")) {
            processed = "(0$processed)"
        }

        return processed
    }

    // Format result to remove unnecessary decimal zeros
    private fun formatResult(result: Double): String {
        return when {
            result == result.toLong().toDouble() -> result.toLong().toString()
            kotlin.math.abs(result) < 0.000001 -> String.format("%.10f", result).trimEnd('0').trimEnd('.')
            kotlin.math.abs(result) > 1E10 -> String.format("%.4E", result)
            else -> {
                val formatted = String.format("%.10f", result).trimEnd('0').trimEnd('.')
                if (formatted.length > 15) {
                    String.format("%.6f", result).trimEnd('0').trimEnd('.')
                } else {
                    formatted
                }
            }
        }
    }

    // Scroll HorizontalScrollView to the right to show latest digits
    private fun scrollToRight(scrollViewId: Int) {
        val scrollView = findViewById<HorizontalScrollView>(scrollViewId)
        scrollView?.postDelayed({
            scrollView.fullScroll(android.view.View.FOCUS_RIGHT)
        }, 100)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}