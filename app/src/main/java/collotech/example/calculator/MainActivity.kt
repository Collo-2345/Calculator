package collotech.example.calculator

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val edittext = findViewById<EditText>(R.id.editText)
        val output = findViewById<TextView>(R.id.output)
        val num0  = findViewById<TextView>(R.id.num0)
        val num1  = findViewById<TextView>(R.id.num1)
        val num2  = findViewById<TextView>(R.id.num2)
        val num3  = findViewById<TextView>(R.id.num3)
        val num4  = findViewById<TextView>(R.id.num4)
        val num5  = findViewById<TextView>(R.id.num5)
        val num6  = findViewById<TextView>(R.id.num6)
        val num7  = findViewById<TextView>(R.id.num7)
        val num8  = findViewById<TextView>(R.id.num8)
        val num9  = findViewById<TextView>(R.id.num9)
        val dot  = findViewById<TextView>(R.id.dot)
        val equal  = findViewById<TextView>(R.id.equal)

        val del  = findViewById<TextView>(R.id.del)
        val addition  = findViewById<TextView>(R.id.addition)
        val subtract  = findViewById<TextView>(R.id.subtract)
        val multiple  = findViewById<TextView>(R.id.multiple)
        val divide  = findViewById<TextView>(R.id.divide)


    }
}