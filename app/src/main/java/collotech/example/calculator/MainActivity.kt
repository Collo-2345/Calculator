package collotech.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import collotech.example.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var expression: String = ""
    private var resultShown: Boolean = false
    private var isClearMode: Boolean = false // Tracks if DEL is now CLR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNumberClickListeners()
        setOperatorClickListeners()
        setEqualAndDeleteListeners()
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
                binding.expressiontxt.setText(expression)

                try {
                    val result = ExpressionBuilder(expression).build().evaluate()
                    binding.output.text = result.toString()
                } catch (e: Exception) {
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
                if (expression.isNotEmpty() && !isLastCharOperator()) {
                    resultShown = false
                    resetDelButton()
                    expression += btn.text.toString()
                    binding.expressiontxt.setText(expression)
                }
            }
        }
    }

    private fun setEqualAndDeleteListeners() {
        binding.equal.setOnClickListener {
            try {
                val result = ExpressionBuilder(expression).build().evaluate()
                val resultString = result.toString()

                // Animate result upward
                binding.output.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(250)
                    .withEndAction {
                        binding.expressiontxt.setText(resultString)
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

            } catch (e: Exception) {
                binding.output.text = "Error"
            }
        }

        binding.del.setOnClickListener {
            if (isClearMode) {
                // 🧹 Clear everything
                expression = ""
                binding.expressiontxt.setText("")
                binding.output.text = ""
                resultShown = false
                resetDelButton()
            } else {
                //  Normal DEL behavior
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    binding.expressiontxt.setText(expression)

                    try {
                        val result = ExpressionBuilder(expression).build().evaluate()
                        binding.output.text = result.toString()
                    } catch (e: Exception) {
                        binding.output.text = ""
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
}
