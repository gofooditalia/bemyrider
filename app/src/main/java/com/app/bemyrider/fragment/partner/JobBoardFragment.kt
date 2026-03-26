package com.app.bemyrider.fragment.partner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.bemyrider.Adapter.User.JobBoardAdapter
import com.app.bemyrider.R
import com.app.bemyrider.model.JobPojoItem
import com.app.bemyrider.utils.PrefsUtil
import com.app.bemyrider.viewmodel.JobViewModel

/**
 * Job Board Fragment for Partners (Riders).
 * Created by Gemini on 2024.
 */
class JobBoardFragment : Fragment(), JobBoardAdapter.JobInteractionListener {

    private val viewModel: JobViewModel by viewModels()
    private lateinit var adapter: JobBoardAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: View
    private lateinit var layoutNoData: View

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_job_board, container, false)
        rvJobs = root.findViewById(R.id.rv_jobs)
        swipeRefresh = root.findViewById(R.id.swipe_refresh)
        progressBar = root.findViewById(R.id.progress_bar)
        layoutNoData = root.findViewById(R.id.layout_no_data)

        userId = PrefsUtil.with(requireContext()).readString("UserId") ?: ""

        setupRecyclerView()
        setupObservers()

        swipeRefresh.setOnRefreshListener {
            loadJobs()
        }

        loadJobs()

        return root
    }

    private fun setupRecyclerView() {
        adapter = JobBoardAdapter(requireContext(), emptyList(), this, false)
        rvJobs.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.jobListResponse.observe(viewLifecycleOwner) { response ->
            swipeRefresh.isRefreshing = false
            if (response != null && response.status && response.data?.jobList != null) {
                val list = response.data!!.jobList!!
                if (list.isEmpty()) {
                    showNoData(true)
                } else {
                    showNoData(false)
                    adapter.updateData(list)
                }
            } else {
                showNoData(true)
            }
        }

        viewModel.actionResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.status) {
                    Toast.makeText(requireContext(), "Candidatura inviata con successo!", Toast.LENGTH_SHORT).show()
                    loadJobs() 
                } else {
                    Toast.makeText(requireContext(), response.message ?: "Errore nella candidatura", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadJobs() {
        viewModel.getAvailableJobs(userId)
    }

    private fun showNoData(show: Boolean) {
        layoutNoData.visibility = if (show) View.VISIBLE else View.GONE
        rvJobs.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onJobClicked(job: JobPojoItem) {
        // Opzionale: apri dettaglio annuncio
    }

    override fun onActionClicked(job: JobPojoItem) {
        job.jobId?.let {
            viewModel.applyToJob(it, userId)
        }
    }
}
