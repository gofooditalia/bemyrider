package com.app.bemyrider.activity.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.bemyrider.databinding.ActivityCreateJobBinding
import com.app.bemyrider.utils.PrefsUtil
import com.app.bemyrider.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for Customers (Esercenti) to create a new job post.
 * Created by Gemini on 2024.
 */
class CreateJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateJobBinding
    private val viewModel: JobViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.etStartTime.setOnClickListener { showDateTimePicker { date -> 
            selectedStartTime = date
            binding.etStartTime.setText(date)
        } }

        binding.etEndTime.setOnClickListener { showTimePicker { time -> 
            selectedEndTime = time
            binding.etEndTime.setText(time)
        } }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSubmit.isEnabled = !isLoading
            binding.btnSubmit.text = if (isLoading) "Pubblicazione..." else "Pubblica Annuncio"
        }

        viewModel.actionResponse.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Toast.makeText(this, "Annuncio pubblicato con successo!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, response.message ?: "Errore nella pubblicazione", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSubmit() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val compensation = binding.etCompensation.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        
        val selectedId = binding.rgVehicle.checkedRadioButtonId
        val vehicle = findViewById<RadioButton>(selectedId)?.text?.toString()?.lowercase() ?: "moto"

        if (title.isEmpty() || description.isEmpty() || compensation.isEmpty() || address.isEmpty() || selectedStartTime.isEmpty()) {
            Toast.makeText(this, "Per favore, compila tutti i campi", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = PrefsUtil.with(this).readString("UserId") ?: ""

        viewModel.createJob(
            title = title,
            description = description,
            vehicleRequired = vehicle,
            startAt = selectedStartTime,
            endAt = selectedEndTime,
            compensation = compensation,
            compensationType = "fisso",
            address = address,
            lat = 0.0,
            lng = 0.0,
            userId = userId
        )
    }

    private fun showDateTimePicker(onSelected: (String) -> Unit) {
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            
            showTimePicker { time ->
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                onSelected(format.format(calendar.time))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(onSelected: (String) -> Unit) {
        TimePickerDialog(this, { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            onSelected(format.format(calendar.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }
}
