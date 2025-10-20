package collotech.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import collotech.example.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {

    // ViewBinding variable
    private lateinit var binding: ActivityMainBinding

    // To store input expression
    private var expression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize all listeners
        setNumberClickListeners()
        setOperatorClickListeners()
        setEqualAndDeleteListeners()
    }

    /** -------------------------------
     *  HANDLE NUMBERS AND DOT BUTTONS
     *  ------------------------------- */
    private fun setNumberClickListeners() {
        val numberButtons = listOf(
            binding.num0, binding.num1, binding.num2, binding.num3,
            binding.num4, binding.num5, binding.num6,
            binding.num7, binding.num8, binding.num9, binding.dot
        )

        numberButtons.forEach { textView ->
            textView.setOnClickListener {
                expression += textView.text.toString()
                binding.expressiontxt.setText(expression)
                evaluateExpressionLive()
            }
        }
    }

    /** -------------------------------
     *  HANDLE OPERATORS (+, -, *, /)
     *  ------------------------------- */
    private fun setOperatorClickListeners() {
        val operatorButtons = listOf(
            binding.addition, binding.subtract,
            binding.multiple, binding.divide
        )

        operatorButtons.forEach { textView ->
            textView.setOnClickListener {
                if (expression.isNotEmpty() && !isLastCharOperator()) {
                    expression += textView.text.toString()
                    binding.expressiontxt.setText(expression)
                }
            }
        }
    }

    /** -------------------------------
     *  HANDLE = AND DEL BUTTONS
     *  ------------------------------- */
    private fun setEqualAndDeleteListeners() {
        // Equals (=)
        binding.equal.setOnClickListener {
            evaluateExpressionLive(final = true)
        }

        // Delete (DEL)
        binding.del.setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                binding.expressiontxt.setText(expression)
                evaluateExpressionLive()
            }
        }
    }

    /** -------------------------------
     *  LIVE EVALUATION FUNCTION
     *  ------------------------------- */
    private fun evaluateExpressionLive(final: Boolean = false) {
        try {
            // Avoid evaluation on incomplete expressions
            if (expression.isNotEmpty() && !isLastCharOperator()) {
                val result = ExpressionBuilder(expression).build().evaluate()

                // Show as real-time result or final output
                if (final) {
                    binding.output.text = result.toString()
                } else {
                    // Real-time preview (without pressing =)
                    binding.output.text = result.toString()
                }
            } else if (final) {
                // Pressed "=" on invalid expression
                binding.output.text = "Error"
            }
        } catch (e: Exception) {
            if (final) binding.output.text = "Error"
            else binding.output.text = ""
        }
    }

    /** -------------------------------
     *  HELPER FUNCTION
     *  ------------------------------- */
    private fun isLastCharOperator(): Boolean {
        if (expression.isEmpty()) return false
        val lastChar = expression.last()
        return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/'
    }
}
