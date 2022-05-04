package com.example.onp_calculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onp_calculator.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val SWIPE_THRESHOLD = 100
const val SWIPE_VELOCITY_THRESHOLD = 100

class MainActivity : AppCompatActivity(), View.OnClickListener, GestureDetector.OnGestureListener {

    companion object {
        var decimalPoints = 3
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var gDetector: GestureDetector

    private var displayColor: DisplayColors = DisplayColors.WHITE

    private var stack: MutableList<Float> = emptyList<Float>().toMutableList()

    private var stackStates: ArrayDeque<MutableList<Float>> = ArrayDeque()

    private var isInput: Boolean = false

    private var input: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        gDetector = GestureDetector(this, this)
        setContentView(binding.root)
        this.addOnClickListeners()
        this.updateStackView()
    }

    override fun onResume() {
        super.onResume()
        this.updateStackView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.decimalPointsMenu -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                onPause()
                this.updateStackView()
            }
            R.id.bgColorMenu -> {
                this.displayColor = this.displayColor.next()
                binding.displayTable.setBackgroundColor(this.displayColor.color)
                binding.display1.setBackgroundColor(this.displayColor.color)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        val button: Button = view as Button
        val num = button.text.toString().toIntOrNull()
        if (num != null) {
            if (this.input.length >= 5) {
                Toast.makeText(this, "Provided number exceeded the max length!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                this.isInput = true
                this.input = when (this.input) {
                    "-0" -> "-$num"
                    "0" -> "$num"
                    else -> "${this.input}$num"
                }
                this.updateStackView()
            }
        } else {
            when (button) {
                binding.enterButton -> {
                    this.saveState()
                    if (isInput) {
                        var value = this.input.toFloat()
                        value = if (value != -0.0f) value else 0f
                        stack.add(value)
                        this.clearInput()
                    } else {
                        if (stack.isNotEmpty()) {
                            stack.add(stack.last())
                            this.updateStackView()
                        }
                    }
                    return
                }
                binding.ACButton -> {
                    stack.clear()
                    stackStates.clear()
                    this.updateStackView()
                    this.clearInput()
                    return
                }
                binding.clearButton -> {
                    this.clearInput()
                    return
                }
                binding.dropButton -> {
                    this.saveState()
                    try {
                        stack.removeLast()
                        this.updateStackView()
                    } catch (e: NoSuchElementException) {
                        Toast.makeText(
                            this,
                            "An element on stack needed to perform this operation!",
                            Toast.LENGTH_SHORT
                        ).show()
                        this.stackStates.removeLast()
                    }
                    return
                }
                binding.shiftButton -> {
                    if (stack.size >= 2) {
                        this.saveState()
                        stack[stack.lastIndex - 1] = stack.last()
                            .also { stack[stack.lastIndex] = stack[stack.lastIndex - 1] }
                        this.updateStackView()
                    } else {
                        Toast.makeText(
                            this,
                            "2 elements on stack needed to perform this operation!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return
                }
                binding.signButton -> {
                    if (this.isInput) {
                        this.input =
                            if (this.input.startsWith('-')) this.input.substring(1)
                            else "-${this.input}"
                        this.updateStackView()
                        return
                    } else {
                        this.saveState()
                        try {
                            stack[stack.lastIndex] = -stack.last()
                            this.updateStackView()
                        } catch (e: NoSuchElementException) {
                            Toast.makeText(
                                this,
                                "An element on stack needed to perform this operation!",
                                Toast.LENGTH_SHORT
                            ).show()
                            this.stackStates.removeLast()
                        }
                    }
                }
                binding.dotButton -> {
                    if (this.input.isEmpty()) {
                        this.isInput = true
                        this.input = "0."
                        this.updateStackView()
                        return
                    }
                    if (this.input.contains('.')) {
                        Toast.makeText(this, "Cannot use 2 dots in the number!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        this.input = "${this.input}."
                        this.updateStackView()
                    }
                    return
                }
                else -> {
                    this.saveState()
                    if (button.text == "√") {
                        if (stack.size >= 1) {
                            stack.add(sqrt(stack.removeLast()))
                            this.updateStackView()
                        } else {
                            Toast.makeText(
                                this,
                                "An element on stack needed to perform this operation!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    } else {
                        if (stack.size >= 2) {
                            val a = stack.removeLast()
                            val b = stack.removeLast()
                            stack.add(
                                when (button.text) {
                                    "+" -> b + a
                                    "-" -> b - a
                                    "x" -> b * a
                                    "/" -> b / a
                                    "^" -> b.pow(a)
                                    else -> 0f
                                }
                            )
                            this.updateStackView()
                        } else {
                            Toast.makeText(
                                this,
                                "2 elements on stack needed to perform this operation!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun clearInput() {
        this.isInput = false
        this.input = ""
        this.updateStackView()
    }

    private fun updateStackView() {
        if (this.isInput) {
            binding.display1.text = this.input
            binding.display1.setTextColor(Color.RED)

            binding.display2.text = adjustDecimalPoints(stack.lastOrNull()?.toString() ?: "-")

            binding.display3.text =
                adjustDecimalPoints(stack.getOrNull(stack.lastIndex - 1)?.toString() ?: "-")

            binding.display4.text =
                adjustDecimalPoints(stack.getOrNull(stack.lastIndex - 2)?.toString() ?: "-")
        } else {
            binding.display1.setTextColor(Color.BLACK)
            binding.display1.text = adjustDecimalPoints(stack.lastOrNull()?.toString() ?: "-")

            binding.display2.text =
                adjustDecimalPoints(stack.getOrNull(stack.lastIndex - 1)?.toString() ?: "-")

            binding.display3.text =
                adjustDecimalPoints(stack.getOrNull(stack.lastIndex - 2)?.toString() ?: "-")
            binding.display4.text =
                adjustDecimalPoints(stack.getOrNull(stack.lastIndex - 3)?.toString() ?: "-")
        }

    }

    private fun saveState() {
        this.stackStates.add(this.stack.toMutableList())
    }


    private fun addOnClickListeners() {
        binding.enterButton.setOnClickListener(this)
        binding.ACButton.setOnClickListener(this)
        binding.dropButton.setOnClickListener(this)
        binding.shiftButton.setOnClickListener(this)
        binding.clearButton.setOnClickListener(this)

        binding.button0.setOnClickListener(this)
        binding.button1.setOnClickListener(this)
        binding.button2.setOnClickListener(this)
        binding.button3.setOnClickListener(this)
        binding.button4.setOnClickListener(this)
        binding.button5.setOnClickListener(this)
        binding.button6.setOnClickListener(this)
        binding.button7.setOnClickListener(this)
        binding.button8.setOnClickListener(this)
        binding.button9.setOnClickListener(this)

        binding.dotButton.setOnClickListener(this)
        binding.signButton.setOnClickListener(this)
        binding.addButton.setOnClickListener(this)
        binding.subtractButton.setOnClickListener(this)
        binding.multiplicateButton.setOnClickListener(this)
        binding.divideButton.setOnClickListener(this)
        binding.powerButton.setOnClickListener(this)
        binding.rootButton.setOnClickListener(this)
    }

    private fun adjustDecimalPoints(text: String): String {
        return if (text != "-") {
            if (text.indexOf('E') == -1) {
                if (decimalPoints != 0)
                    text.substring(
                        0,
                        min(text.length, text.indexOf('.') + decimalPoints + 1)
                    )
                else
                    text.substring(
                        0,
                        min(text.length, text.indexOf('.') + decimalPoints)
                    )
            } else {
                if (decimalPoints != 0)
                    text.substring(
                        0,
                        min(text.length, text.indexOf('.') + decimalPoints + 1)
                    ) + text.substring(text.indexOf('E'))
                else
                    text.substring(
                        0,
                        min(text.length, text.indexOf('.') + decimalPoints)
                    ) + text.substring(text.indexOf('E'))
            }
        } else {
            "-"
        }
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
    }

    override fun onFling(
        p1: MotionEvent,
        p2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            val diffY = p2.y - p1.y
            val diffX = p2.x - p1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight()
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }

    private fun onSwipeRight() {
        if (this.stackStates.isNotEmpty()) {
            this.stack = this.stackStates.removeLast()
            this.updateStackView()
        } else {
            Toast.makeText(this, "No more actions to be undone!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

}

