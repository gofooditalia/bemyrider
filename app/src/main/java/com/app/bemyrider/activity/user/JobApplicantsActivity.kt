package com.app.bemyrider.activity.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.bemyrider.Adapter.User.JobApplicantsAdapter
import com.app.bemyrider.R
import com.app.bemyrider.databinding.ActivityJobApplicantsBinding
import com.app.bemyrider.model.JobApplicantPojoItem
import com.app.bemyrider.viewmodel.JobViewModel

/**
 * Activity for Customers to see and hire riders who applied to their job.
 * Created by Gemini on 2024.
 */
class JobApplicantsActivity : AppCompatActivity(), JobApplicantsAdapter.ApplicantInteractionListener {

    private lateinit var binding: ActivityJobApplicantsBinding
    private val viewModel: JobViewModel by viewModels()
    private lateinit var adapter: JobApplicantsAdapter
    
    private var jobId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobApplicantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        jobId = intent.getStringExtra("job_id")
        if (jobId == null) {
            Toast.makeText(this, "ID Annuncio non valido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupObservers()

        binding.swipeRefresh.setOnRefreshListener {
            loadApplicants()
        }

        loadApplicants()
    }

    private fun setupRecyclerView() {
        adapter = JobApplicantsAdapter(this, emptyList(), this)
        binding.rvApplicants.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.applicantResponse.observe(this) { response ->
            binding.swipeRefresh.isRefreshing = false
            if (response != null && response.status && response.data?.applicantList != null) {
                val list = response.data!!.applicantList!!
                if (list.isEmpty()) {
                    binding.layoutNoData.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                } else {
                    binding.layoutNoData.visibility = View.GONE
                    adapter.updateData(list)
                }
            } else {
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        viewModel.actionResponse.observe(this) { response ->
            if (response != null && response.status) {
                val serviceRequestId = response.data?.serviceRequestId
                if (serviceRequestId != null) {
                    Toast.makeText(this, "Rider ingaggiato! Procedi al pagamento.", Toast.LENGTH_LONG).show()
                    // Reindirizza al flusso di pagamento esistente
                    val intent = Intent(this, BookedServiceDetailActivity::class.java)
                    intent.putExtra("serviceRequestId", serviceRequestId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Ingaggio confermato.", Toast.LENGTH_SHORT).show()
                    loadApplicants()
                }
            } else {
                Toast.makeText(this, response?.message ?: "Errore nell'ingaggio", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadApplicants() {
        jobId?.let { viewModel.getJobApplicants(it) }
    }

    override fun onRiderProfileClicked(applicant: JobApplicantPojoItem) {
        // Apri profilo rider per vedere feedback dettagliati
        val intent = Intent(this, UserServicesActivity::class.java)
        intent.putExtra("providerId", applicant.riderId)
        startActivity(intent)
    }

    override fun onHireClicked(applicant: JobApplicantPojoItem) {
        jobId?.let { jId ->
            applicant.riderId?.let { rId ->
                viewModel.hireRider(jId, rId)
            }
        }
    }
}
