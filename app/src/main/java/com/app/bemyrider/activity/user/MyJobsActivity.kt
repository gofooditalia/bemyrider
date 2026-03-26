package com.app.bemyrider.activity.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.bemyrider.Adapter.User.JobBoardAdapter
import com.app.bemyrider.databinding.ActivityMyJobsBinding
import com.app.bemyrider.model.JobPojoItem
import com.app.bemyrider.utils.PrefsUtil
import com.app.bemyrider.viewmodel.JobViewModel

/**
 * Activity for Customers to see and manage their job posts.
 * Created by Gemini on 2024.
 */
class MyJobsActivity : AppCompatActivity(), JobBoardAdapter.JobInteractionListener {

    private lateinit var binding: ActivityMyJobsBinding
    private val viewModel: JobViewModel by viewModels()
    private lateinit var adapter: JobBoardAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        userId = PrefsUtil.with(this).readString("UserId") ?: ""

        setupRecyclerView()
        setupObservers()
        setupListeners()

        binding.swipeRefresh.setOnRefreshListener {
            loadMyJobs()
        }
    }

    private fun setupListeners() {
        binding.fabAddJob.setOnClickListener {
            startActivity(Intent(this, CreateJobActivity::class.java))
        }

        binding.btnCreateFirst.setOnClickListener {
            startActivity(Intent(this, CreateJobActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyJobs()
    }

    private fun setupRecyclerView() {
        adapter = JobBoardAdapter(this, emptyList(), this, true)
        binding.rvMyJobs.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.jobListResponse.observe(this) { response ->
            binding.swipeRefresh.isRefreshing = false
            if (response != null && response.status && response.data?.jobList != null) {
                val list = response.data!!.jobList!!
                if (list.isEmpty()) {
                    showEmptyState(true)
                    adapter.updateData(emptyList())
                } else {
                    showEmptyState(false)
                    adapter.updateData(list)
                }
            } else {
                showEmptyState(true)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutNoData.visibility = View.VISIBLE
            binding.rvMyJobs.visibility = View.GONE
        } else {
            binding.layoutNoData.visibility = View.GONE
            binding.rvMyJobs.visibility = View.VISIBLE
        }
    }

    private fun loadMyJobs() {
        viewModel.getAvailableJobs(userId) // Qui andrebbe getMyJobPosts, ma usiamo questo per ora come mockup
    }

    override fun onJobClicked(job: JobPojoItem) {
        openApplicants(job)
    }

    override fun onActionClicked(job: JobPojoItem) {
        openApplicants(job)
    }

    private fun openApplicants(job: JobPojoItem) {
        val intent = Intent(this, JobApplicantsActivity::class.java)
        intent.putExtra("job_id", job.jobId)
        startActivity(intent)
    }
}
