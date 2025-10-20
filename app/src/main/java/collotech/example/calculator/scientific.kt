package collotech.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import collotech.example.calculator.databinding.ActivityScientificBinding
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.*


class scientific : AppCompatActivity() {

    private lateinit var binding: ActivityScientificBinding
    private var expression = ""
    private var justEvaluated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scientific)

        binding = ActivityScientificBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        // 🔙 Back to Main Calculator
        binding.backBtn.setOnClickListener { finish() }

        // Numbers & dot
        setNumberListeners()

        // Operators
        setOperatorListeners()

        // Scientific functions
        setScientificListeners()

        // Equal, DEL, CLR
        setControlListeners()

    }
    private fun setNumberListeners() {
        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btnDot
        )

        buttons.forEach { btn ->
            btn.setOnClickListener {
                if (justEvaluated) {
                    expression = ""
                    justEvaluated = false
                }
                expression += btn.text
                binding.expressiontxt.text = expression
                autoEvaluate()
            }
        }
    }

    private fun setOperatorListeners() {
        val ops = listOf(
            binding.addBtn, binding.subBtn, binding.mulBtn, binding.divBtn,
            binding.openBracket, binding.closeBracket, binding.modBtn, binding.powBtn
        )

        ops.forEach { op ->
            op.setOnClickListener {
                val symbol = when (op.text) {
                    "×" -> "*"
                    "÷" -> "/"
                    else -> op.text.toString()
                }
                expression += symbol
                binding.expressiontxt.text = expression
            }
        }
    }

    private fun setScientificListeners() {
        val sciButtons = mapOf(
            binding.sinBtn to "sin(",
            binding.cosBtn to "cos(",
            binding.tanBtn to "tan(",
            binding.logBtn to "log10(",
            binding.lnBtn to "ln(",
            binding.sqrtBtn to "sqrt(",
            binding.piBtn to PI.toString(),
            binding.eBtn to E.toString()
        )

        sciButtons.forEach { (btn, value) ->
            btn.setOnClickListener {
                expression += value
                binding.expressiontxt.text = expression
            }
        }
    }

    private fun setControlListeners() {
        // Equal
        binding.equal.setOnClickListener {
            try {
                val result = evaluateExpression(expression)
                animateResult(result)
                expression = result.toString()
                justEvaluated = true
                binding.delBtn.text = "CLR"
            } catch (e: Exception) {
                binding.output.text = "Error"
            }
        }

        // Delete / CLR
        binding.delBtn.setOnClickListener {
            if (binding.delBtn.text == "CLR") {
                expression = ""
                binding.expressiontxt.text = ""
                binding.output.text = ""
                binding.delBtn.text = "DEL"
            } else {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    binding.expressiontxt.text = expression
                }
            }
        }

    }

    // 🔢 Auto-evaluate while typing
    private fun autoEvaluate() {
        try {
            val result = evaluateExpression(expression)
            binding.output.text = result.toString()
        } catch (_: Exception) {
        }
    }

    // 🧮 Evaluate full expression (supports sin, cos, etc.)
    private fun evaluateExpression(expr: String): Double {
        var modExpr = expr
            .replace("sin", "sinr")
            .replace("cos", "cosr")
            .replace("tan", "tanr")
            .replace("ln", "log")
            .replace("√", "sqrt")

        val builder = ExpressionBuilder(modExpr)
            .function(object : net.objecthunter.exp4j.function.Function("sinr", 1) {
                override fun apply(args: DoubleArray): Double {
                    return sin(Math.toRadians(args[0]))
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("cosr", 1) {
                override fun apply(args: DoubleArray): Double {
                    return cos(Math.toRadians(args[0]))
                }
            })
            .function(object : net.objecthunter.exp4j.function.Function("tanr", 1) {
                override fun apply(args: DoubleArray): Double {
                    return tan(Math.toRadians(args[0]))
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

    // ✨ Smooth animation when result updates
    private fun animateResult(result: Double) {
        binding.output.animate().alpha(0f).setDuration(200).withEndAction {
            binding.output.text = result.toString()
            ObjectAnimator.ofFloat(binding.output, "alpha", 0f, 1f).setDuration(300).start()
        }.start()
    }

}