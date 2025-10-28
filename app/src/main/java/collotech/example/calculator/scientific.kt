package collotech.example.calculator


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
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
    private var angleMode = "NONE" // Start with NONE (basic mode), then RAD, then DEG
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

        // Initialize mode display
        updateModeDisplay()

        setupListeners()

        // Start back button animation
        startBackButtonAnimation()
    }

    private fun startBackButtonAnimation() {
        // Color splash animation - rainbow colors cycling (only text color)
        val colorAnimation = ValueAnimator.ofArgb(
            0xFF4A90E2.toInt(), // Blue
            0xFF50C878.toInt(), // Green
            0xFFFFAA00.toInt(), // Orange
            0xFFFF6B6B.toInt(), // Red
            0xFFAA66CC.toInt(), // Purple
            0xFFFF1493.toInt(), // Deep Pink
            0xFF4A90E2.toInt()  // Back to Blue
        )
        colorAnimation.duration = 4000
        colorAnimation.repeatCount = ValueAnimator.INFINITE
        colorAnimation.addUpdateListener { animator ->
            binding.backBtn.setTextColor(animator.animatedValue as Int)
        }

        // Start color animation
        colorAnimation.start()
    }

    @SuppressLint("SetTextI18n")
    private fun setupListeners() {
        // Numbers and dot
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btnDot
        )

        numberButtons.forEach { btn ->
            btn.setOnClickListener {
                if (resultShown) {
                    // Clear and start fresh when entering a number after result
                    expression = ""
                    binding.output.text = ""
                    resultShown = false
                    binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                    binding.delBtn.text = "DEL"
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

        // Back to main calculator with smooth transition
        binding.backBtn.setOnClickListener {
            finish()
            // Add smooth slide transition animation
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Mode toggle (RAD/DEG)
        binding.mode.setOnClickListener {
            toggleAngleMode()
        }
    }

    private fun toggleAngleMode() {
        angleMode = when (angleMode) {
            "NONE" -> "RAD"
            "RAD" -> "DEG"
            "DEG" -> "NONE"
            else -> "NONE"
        }
        updateModeDisplay()

        val modeMessage = when (angleMode) {
            "NONE" -> "Mode: Basic (No angle conversion)"
            "RAD" -> "Mode: Radians"
            "DEG" -> "Mode: Degrees"
            else -> "Mode: Basic"
        }
        showToast(modeMessage)
    }

    @SuppressLint("SetTextI18n")
    private fun updateModeDisplay() {
        when (angleMode) {
            "NONE" -> {
                binding.mode.text = "Mode"
                binding.mode.setTextColor(resources.getColor(android.R.color.white, null))
            }
            "RAD" -> {
                binding.mode.text = "RAD"
                binding.mode.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            "DEG" -> {
                binding.mode.text = "DEG"
                binding.mode.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
                    // Allow continuing with result after equals
                    if (resultShown) {
                        resultShown = false
                        binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                        binding.delBtn.text = "DEL"
                    }

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
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "("
            updateExpression()
        }

        binding.closeBracket.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += ")"
            updateExpression()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupScientificFunctions() {
        // Trigonometric functions
        binding.sinBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
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
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
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
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            if (isInverse) {
                expression += "atan("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "tan("
            }
            updateExpression()
        }

        // Logarithms with INVERSE support
        binding.lnBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            if (isInverse) {
                // Inverse of ln is e^x
                expression += "exp("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "ln("
            }
            updateExpression()
        }

        binding.logBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            if (isInverse) {
                // Inverse of log is 10^x
                expression += "10^("
                isInverse = false
                binding.invBtn.alpha = 1.0f
            } else {
                expression += "log("
            }
            updateExpression()
        }

        // Square root
        binding.sqrtBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "√("
            updateExpression()
            autoEvaluate() // Enable auto-complete for sqrt
        }

        // Power
        binding.powBtn.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "^"
            updateExpression()
        }

        // Square (X²)
        binding.squareBtn.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "^2"
            updateExpression()
            autoEvaluate()
        }

        // Constants
        binding.piBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "π"
            updateExpression()
            autoEvaluate()
        }

        binding.eBtn.setOnClickListener {
            if (resultShown) {
                expression = ""
                binding.output.text = ""
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "e"
            updateExpression()
            autoEvaluate()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSpecialFunctions() {
        // Modulo
        binding.modBtn.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "%"
            updateExpression()
        }

        // Factorial
        binding.factorialBtn.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "!"
            updateExpression()
            autoEvaluate()
        }

        // Answer (Ans) - Fixed to prevent multiple consecutive additions
        binding.ansBtn.setOnClickListener {
            // Check if the last added text was already "Ans" or lastAnswer
            val lastAddedWasAns = expression.endsWith(lastAnswer) &&
                    expression.length >= lastAnswer.length

            if (!lastAddedWasAns) {
                if (resultShown) {
                    // If showing result, start fresh with the answer
                    expression = lastAnswer
                    resultShown = false
                    binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                    binding.delBtn.text = "DEL"
                } else {
                    expression += lastAnswer
                }
                updateExpression()
                autoEvaluate()
            } else {
                showToast("Answer already added")
            }
        }

        // EXP (scientific notation) - FIXED to enable auto-evaluate
        binding.expBtn.setOnClickListener {
            if (resultShown) {
                resultShown = false
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                binding.delBtn.text = "DEL"
            }
            expression += "E"
            updateExpression()
            autoEvaluate() // Enable auto-evaluate for EXP
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

    @SuppressLint("SetTextI18n")
    private fun setupControlButtons() {
        // Equal button - FIXED to auto-complete brackets
        binding.equal.setOnClickListener {
            try {
                // Auto-complete brackets before evaluation
                val completedExpression = autoCompleteBrackets(expression)
                val result = evaluateExpression(completedExpression)

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
                            binding.expressionText.text = resultString
                            binding.expressionText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
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
                binding.expressionText.text = ""
                binding.output.text = ""
                binding.expressionText.setTextColor(resources.getColor(android.R.color.white, null))
                resultShown = false
                binding.delBtn.text = "DEL"
            } else {
                // Delete last character
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    updateExpression()
                    autoEvaluate()
                } else {
                    // If expression is empty, also clear output
                    binding.output.text = ""
                }
            }
        }
    }

    private fun updateExpression() {
        binding.expressionText.text = expression
        scrollToRight(R.id.expressionScrollView)
    }

    private fun autoEvaluate() {
        try {
            if (expression.isNotEmpty() && !isLastCharOperator()) {
                // Auto-complete unclosed brackets for preview
                val completedExpression = autoCompleteBrackets(expression)

                // Check if expression is complete enough to evaluate
                if (isEvaluableExpression(completedExpression)) {
                    val result = evaluateExpression(completedExpression)
                    if (!result.isNaN() && !result.isInfinite()) {
                        binding.output.text = formatResult(result)
                        scrollToRight(R.id.outputScrollView)
                    } else {
                        binding.output.text = ""
                    }
                } else {
                    // Expression is incomplete or invalid, clear output
                    binding.output.text = ""
                }
            } else {
                // Last char is operator or expression is empty, clear output
                binding.output.text = ""
            }
        } catch (_: Exception) {
            binding.output.text = ""
        }
    }

    private fun autoCompleteBrackets(expr: String): String {
        var result = expr
        var openCount = 0
        var closeCount = 0

        // Count open and close brackets
        for (char in expr) {
            when (char) {
                '(' -> openCount++
                ')' -> closeCount++
            }
        }

        // Add missing closing brackets
        val missingBrackets = openCount - closeCount
        if (missingBrackets > 0) {
            result += ")".repeat(missingBrackets)
        }

        return result
    }

    private fun isEvaluableExpression(expr: String): Boolean {
        // Check if expression is just a single number
        val numberPattern = "^-?\\d+(\\.\\d+)?$".toRegex()
        if (numberPattern.matches(expr)) {
            return false // Don't show single numbers in output
        }

        // Check if expression contains at least one operator or function
        val hasOperator = expr.contains(Regex("[+\\-*/^%]"))
        val hasFunction = expr.contains(Regex("(sin|cos|tan|asin|acos|atan|ln|log|sqrt|exp)\\("))
        val hasConstant = expr.contains("π") || expr.contains("e")
        val hasFactorial = expr.contains("!")
        val hasScientificNotation = expr.contains("E")

        // Expression is evaluable if it has operators, functions, constants, or factorials
        return hasOperator || hasFunction || hasConstant || hasFactorial || hasScientificNotation
    }

    private fun evaluateExpression(expr: String): Double {
        if (expr.isEmpty()) return 0.0

        var processedExpr = expr
            // Replace symbols with exp4j compatible ones
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", PI.toString())
            .replace("√", "sqrt")

        // Handle scientific notation PROPERLY
        // Replace E with *10^ but be careful with 'e' constant
        processedExpr = processedExpr.replace(Regex("(\\d)E([+-]?\\d)"), "$1*10^($2)")

        // Now replace 'e' constant
        processedExpr = processedExpr.replace(Regex("(?<!\\d)e(?!xp)"), E.toString())

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
                    return when (angleMode) {
                        "RAD" -> sin(args[0])
                        "DEG" -> sin(Math.toRadians(args[0]))
                        else -> sin(args[0]) // NONE mode treats as radians by default
                    }
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("cos", 1) {
                override fun apply(args: DoubleArray): Double {
                    return when (angleMode) {
                        "RAD" -> cos(args[0])
                        "DEG" -> cos(Math.toRadians(args[0]))
                        else -> cos(args[0])
                    }
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("tan", 1) {
                override fun apply(args: DoubleArray): Double {
                    return when (angleMode) {
                        "RAD" -> tan(args[0])
                        "DEG" -> tan(Math.toRadians(args[0]))
                        else -> tan(args[0])
                    }
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("asin", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = asin(args[0])
                    return when (angleMode) {
                        "RAD" -> result
                        "DEG" -> Math.toDegrees(result)
                        else -> result
                    }
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("acos", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = acos(args[0])
                    return when (angleMode) {
                        "RAD" -> result
                        "DEG" -> Math.toDegrees(result)
                        else -> result
                    }
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("atan", 1) {
                override fun apply(args: DoubleArray): Double {
                    val result = atan(args[0])
                    return when (angleMode) {
                        "RAD" -> result
                        "DEG" -> Math.toDegrees(result)
                        else -> result
                    }
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
            .function(object : net.objecthunter.exp4j.function.Function("exp", 1) {
                override fun apply(args: DoubleArray): Double {
                    return exp(args[0]) // e^x
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

    @SuppressLint("DefaultLocale")
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