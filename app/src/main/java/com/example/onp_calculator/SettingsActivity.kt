package com.example.onp_calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onp_calculator.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.numberPicker.minValue = 0
        binding.numberPicker.maxValue = 8
        binding.numberPicker.value = MainActivity.decimalPoints

        binding.numberPicker.setOnValueChangedListener { _, _, new ->
            run {
                MainActivity.decimalPoints = new
            }
        }

        binding.returnButton.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }

}