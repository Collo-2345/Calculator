package collotech.example.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.HorizontalScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import collotech.example.calculator.databinding.ActivityScientificBinding
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.*

class scientific : AppCompatActivity() {

    private lateinit var binding: ActivityScientificBinding
    private var expression = ""
    private var resultShown = false
    private var angleMode = "NONE" // NONE, RAD, or DEG
    private var lastAnswer = "0"
    private var isInverse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScientificBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Numbers and dot
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btnDot
        )

        numberButtons.forEach { btn ->
            btn.setOnClickListener {
                if (resultShown) {
                    expression = ""
                    binding.output.text = ""
                    resultShown = false
                    binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
                }
                expression += btn.text.toString()
                updateExpression()
                autoEvaluate()
            }
        }

        // Basic operators
        setupOperators()

        // Scientific functions
        setupScientificFunctions()

        // Control buttons
        setupControlButtons()

        // Special functions
        setupSpecialFunctions()

        // Back to main calculator
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupOperators() {
        val operators = mapOf(
            binding.addBtn to "+",
            binding.subBtn to "-",
            binding.mulBtn to "*",
            binding.divBtn to "/"
        )

        operators.forEach { (btn, op) ->
            btn.setOnClickListener {
                if (expression.isEmpty() && op == "-") {
                    expression = op
                } else if (expression.isNotEmpty()) {
                    if (isLastCharOperator()) {
                        expression = expression.dropLast(1) + op
                    } else {
                        expression += op
                    }
                }
                updateExpression()
            }
        }

        // Brackets
        binding.openBracket.setOnClickListener {
            expression += "("
            updateExpression()
        }

        binding.closeBracket.setOnClickListener {
            expression += ")"
            updateExpression()
        }
    }

    private fun setupScientificFunctions() {
        // Trigonometric functions
        binding.sinBtn.setOnClickListener {
            if (isInverse) {
                expression += "asin("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "sin("
            }
            updateExpression()
        }

        binding.cosBtn.setOnClickListener {
            if (isInverse) {
                expression += "acos("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "cos("
            }
            updateExpression()
        }

        binding.tanBtn.setOnClickListener {
            if (isInverse) {
                expression += "atan("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "tan("
            }
            updateExpression()
        }

        // Logarithms
        binding.lnBtn.setOnClickListener {
            expression += "ln("
            updateExpression()
        }

        binding.logBtn.setOnClickListener {
            expression += "log("
            updateExpression()
        }

        // Square root
        binding.sqrtBtn.setOnClickListener {
            expression += "√("
            updateExpression()
        }

        // Power
        binding.powBtn.setOnClickListener {
            expression += "^"
            updateExpression()
        }

        // Constants
        binding.piBtn.setOnClickListener {
            expression += "π"
            updateExpression()
            autoEvaluate()
        }

        binding.eBtn.setOnClickListener {
            expression += "e"
            updateExpression()
            autoEvaluate()
        }
    }

    private fun setupSpecialFunctions() {
        // Modulo
        binding.modBtn.setOnClickListener {
            expression += "%"
            updateExpression()
        }

        // Factorial
        binding.factorialBtn.setOnClickListener {
            expression += "!"
            updateExpression()
            autoEvaluate()
        }

        // Answer (Ans)
        binding.ansBtn.setOnClickListener {
            expression += lastAnswer
            updateExpression()
            autoEvaluate()
        }

        // EXP (scientific notation)
        binding.expBtn.setOnClickListener {
            expression += "E"
            updateExpression()
        }

        // Inverse toggle
        binding.invBtn.setOnClickListener {
            isInverse = !isInverse
            if (isInverse) {
                binding.invBtn.alpha = 0.5f
                showToast("Inverse mode ON")
            } else {
                binding.invBtn.alpha = 1.0f
                showToast("Inverse mode OFF")
            }
        }
    }

    private fun setupControlButtons() {
        // Equal button
        binding.equal.setOnClickListener {
            try {
                val result = evaluateExpression(expression)

                if (result.isInfinite()) {
                    binding.output.text = "∞"
                    showToast("Division by zero")
                } else if (result.isNaN()) {
                    binding.output.text = "Error"
                    showToast("Invalid operation")
                } else {
                    val resultString = formatResult(result)
                    lastAnswer = resultString

                    // Animate result
                    binding.output.animate()
                        .alpha(0f)
                        .translationY(-50f)
                        .setDuration(250)
                        .withEndAction {
                            binding.expressiontxt.text = resultString
                            binding.expressiontxt.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                            scrollToRight(R.id.expressionScrollView)
                            binding.output.text = ""
                            binding.output.alpha = 1f
                            binding.output.translationY = 0f
                            expression = resultString
                            resultShown = true
                            binding.delBtn.text = "AC"
                        }
                        .start()
                }
            } catch (e: Exception) {
                binding.output.text = "Error"
                showToast(e.message ?: "Invalid expression")
            }
        }

        // DEL / CE button
        binding.delBtn.setOnClickListener {
            if (binding.delBtn.text == "AC") {
                // Clear everything
                expression = ""
                binding.expressiontxt.text = ""
                binding.output.text = ""
                binding.expressiontxt.setTextColor(resources.getColor(android.R.color.white, null))
                resultShown = false
                binding.delBtn.text = "DEL"
            } else {
                // Delete last character
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    updateExpression()
                    autoEvaluate()
                }
            }
        }
    }

    private fun updateExpression() {
        binding.expressiontxt.text = expression
        scrollToRight(R.id.expressionScrollView)
    }

    private fun autoEvaluate() {
        try {
            if (expression.isNotEmpty() && !isLastCharOperator()) {
                val result = evaluateExpression(expression)
                if (!result.isNaN() && !result.isInfinite()) {
                    binding.output.text = formatResult(result)
                    scrollToRight(R.id.outputScrollView)
                }
            }
        } catch (e: Exception) {
            binding.output.text = ""
        }
    }

    private fun evaluateExpression(expr: String): Double {
        if (expr.isEmpty()) return 0.0

        var processedExpr = expr
            // Replace symbols with exp4j compatible ones
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", PI.toString())
            .replace("e", E.toString())
            .replace("√", "sqrt")
            .replace("E", "*10^") // Scientific notation

        // Handle factorial
        processedExpr = handleFactorial(processedExpr)

        // Convert trig functions based on angle mode
        if (angleMode == "DEG") {
            processedExpr = convertTrigToDegrees(processedExpr)
        }

        // Build and evaluate expression
        val builder = ExpressionBuilder(processedExpr)
            .function(object : net.objecthunter.exp4j.function.Function("sin", 1) {
                override fun apply(args: DoubleArray): Double {
                    return if (angleMode == "RAD") sin(args[0]) else sin(Math.toRadians(args[0]))
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("cos", 1) {
                override fun apply(args: DoubleArray): Double {
                    return if (angleMode == "RAD") cos(args[0]) else cos(Math.toRadians(args[0]))
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("tan", 1) {
                override fun apply(args: DoubleArray): Double {
                    return if (angleMode == "RAD") tan(args[0]) else tan(Math.toRadians(args[0]))
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("asin", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = asin(args[0])
                    return if (angleMode == "RAD") result else Math.toDegrees(result)
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("acos", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = acos(args[0])
                    return if (angleMode == "RAD") result else Math.toDegrees(result)
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("atan", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = atan(args[0])
                    return if (angleMode == "RAD") result else Math.toDegrees(result)
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("ln", 1) {
                override fun apply(args: DoubleArray): Double {
                    return ln(args[0])
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("log", 1) {
                override fun apply(args: DoubleArray): Double {
                    return log10(args[0])
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("sqrt", 1) {
                override fun apply(args: DoubleArray): Double {
                    return sqrt(args[0])
                }
            })
            .build()

        return builder.evaluate()
    }

    private fun handleFactorial(expr: String): String {
        var result = expr
        val factorialPattern = "(\\d+)!".toRegex()

        factorialPattern.findAll(result).forEach { match ->
            val number = match.groupValues[1].toInt()
            val factorial = calculateFactorial(number)
            result = result.replace(match.value, factorial.toString())
        }

        return result
    }

    private fun calculateFactorial(n: Int): Long {
        if (n < 0) throw IllegalArgumentException("Negative factorial")
        if (n > 20) throw IllegalArgumentException("Factorial too large")
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    private fun convertTrigToDegrees(expr: String): String {
        // This is handled in the custom functions
        return expr
    }

    private fun formatResult(result: Double): String {
        return when {
            result == result.toLong().toDouble() -> result.toLong().toString()
            abs(result) < 0.000001 -> String.format("%.10f", result).trimEnd('0').trimEnd('.')
            abs(result) > 1E10 -> String.format("%.4E", result)
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

    private fun isLastCharOperator(): Boolean {
        if (expression.isEmpty()) return false
        val lastChar = expression.last()
        return lastChar in listOf('+', '-', '*', '/', '^', '%')
    }

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