package collotech.example.calculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import collotech.example.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import android.widget.ImageView
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

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

        // ensure both TextViews support scrolling
        setupScrollableTextView(binding.expressiontxt)
        setupScrollableTextView(binding.output)

        setNumberClickListeners()
        setOperatorClickListeners()
        setEqualAndDeleteListeners()

        val arrow = findViewById<ImageView>(R.id.arrow)
        arrow.setOnClickListener {
            val intent = Intent(this, scientific::class.java)
            startActivity(intent)
        }
    }

    private fun setupScrollableTextView(tv: TextView) {
        tv.isHorizontalScrollBarEnabled = true
        tv.movementMethod = ScrollingMovementMethod()
        tv.setHorizontallyScrolling(true)
        tv.isSingleLine = true
        tv.isSelected = true
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
                    resetDelButton()
                }

                expression += btn.text.toString()
                updateExpressionView()
            }
        }
    }

    private fun setOperatorClickListeners() {
        val operatorButtons = listOf(
            binding.addition, binding.multiple, binding.divide
        )

        operatorButtons.forEach { btn ->
            btn.setOnClickListener {
                if (expression.isNotEmpty() && !isLastCharOperator()) {
                    resultShown = false
                    resetDelButton()
                    expression += btn.text.toString()
                    updateExpressionView()
                }
            }
        }

        // subtraction with negative support
        binding.subtract.setOnClickListener {
            if (expression.isEmpty() || isLastCharOperator()) {
                expression += "-" // allow negative number
            } else {
                expression += "-" // subtraction
            }
            updateExpressionView()
        }
    }

    private fun setEqualAndDeleteListeners() {
        binding.equal.setOnClickListener {
            try {
                val sanitized = expression.replace("--", "+")
                val result = ExpressionBuilder(sanitized).build().evaluate()
                val resultString = result.toString()

                binding.output.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(250)
                    .withEndAction {
                        expression = resultString
                        binding.expressiontxt.text = resultString
                        adjustTextSize(binding.expressiontxt)
                        scrollToEnd(binding.expressiontxt)
                        binding.output.text = ""
                        binding.output.alpha = 1f
                        binding.output.translationY = 0f
                        resultShown = true
                        binding.del.text = "CLR"
                        isClearMode = true
                    }
                    .start()
            } catch (e: Exception) {
                binding.output.text = "Error"
            }
        }

        binding.del.setOnClickListener {
            if (isClearMode) {
                expression = ""
                binding.expressiontxt.text = ""
                binding.output.text = ""
                resultShown = false
                resetDelButton()
            } else {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    updateExpressionView()
                }
            }
        }
    }

    private fun updateExpressionView() {
        binding.expressiontxt.text = expression
        adjustTextSize(binding.expressiontxt)
        scrollToEnd(binding.expressiontxt)

        try {
            val result = ExpressionBuilder(expression).build().evaluate()
            binding.output.text = result.toString()
            adjustTextSize(binding.output)
            scrollToEnd(binding.output)
        } catch (e: Exception) {
            binding.output.text = ""
        }
    }

    private fun resetDelButton() {
        binding.del.text = "DEL"
        isClearMode = false
    }

    private fun isLastCharOperator(): Boolean {
        if (expression.isEmpty()) return false
        val last = expression.last()
        return last == '+' || last == '-' || last == '*' || last == '/'
    }

    private fun adjustTextSize(tv: TextView) {
        val length = tv.text.length
        val scale = when {
            length < 10 -> 1.0f
            length < 15 -> 0.9f
            length < 20 -> 0.8f
            length < 25 -> 0.7f
            length < 30 -> 0.6f
            else -> 0.5f
        }
        tv.textScaleX = scale
    }

    private fun scrollToEnd(tv: TextView) {
        tv.post {
            val scrollX = (tv.layout?.getLineWidth(0) ?: 0f).toInt()
            tv.scrollTo(scrollX, 0)
        }
    }
}
