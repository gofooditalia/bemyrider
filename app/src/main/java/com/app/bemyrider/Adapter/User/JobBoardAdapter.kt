package com.app.bemyrider.Adapter.User

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.app.bemyrider.R
import com.app.bemyrider.model.JobPojoItem
import com.google.android.material.button.MaterialButton

/**
 * Adapter for Job Board (Bacheca) list.
 * Optimized for stable image loading by Gemini - 2024.
 */
class JobBoardAdapter(
    private val context: Context,
    private var jobList: List<JobPojoItem>,
    private val listener: JobInteractionListener,
    private val isCustomerView: Boolean = false
) : RecyclerView.Adapter<JobBoardAdapter.JobViewHolder>() {

    interface JobInteractionListener {
        fun onJobClicked(job: JobPojoItem)
        fun onActionClicked(job: JobPojoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_job_board, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]

        holder.txtTitle.text = job.title ?: ""
        holder.txtCustomerName.text = job.customerName ?: "Bemyrider User"
        holder.txtDesc.text = job.description ?: ""
        holder.txtAddress.text = job.address ?: ""
        holder.txtCompensation.text = "€ ${job.compensation ?: "0"}"
        holder.txtDateTime.text = "${job.startAt ?: ""} - ${job.endAt ?: ""}"
        holder.txtVehicle.text = job.vehicleRequired?.uppercase() ?: "INDIFF."

        if (isCustomerView) {
            holder.btnAction.text = "Vedi Candidati"
        } else {
            holder.btnAction.text = "Candidati"
        }

        // --- FINAL IMAGE LOGIC (No POST, No Thread issues) ---
        val imageUrl = job.customerImage
        val isExplicitlyEmpty = imageUrl.isNullOrEmpty() || 
                               imageUrl.lowercase().contains("no_user_image.png")

        if (!isExplicitlyEmpty) {
            holder.ivCustomer.load(imageUrl) {
                placeholder(R.drawable.account_circle_24)
                error(R.drawable.account_circle_24)
                // Do not use crossfade with custom CircleImageView
            }
        } else {
            holder.ivCustomer.load(R.drawable.account_circle_24)
        }
        // --------------------------------------------------------

        holder.itemView.setOnClickListener { listener.onJobClicked(job) }
        holder.btnAction.setOnClickListener { listener.onActionClicked(job) }
    }

    override fun getItemCount(): Int = jobList.size

    fun updateData(newList: List<JobPojoItem>) {
        this.jobList = newList
        notifyDataSetChanged()
    }

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCustomer: ImageView = itemView.findViewById(R.id.iv_customer)
        val txtTitle: TextView = itemView.findViewById(R.id.txt_job_title)
        val txtCustomerName: TextView = itemView.findViewById(R.id.txt_customer_name)
        val txtDesc: TextView = itemView.findViewById(R.id.txt_job_desc)
        val txtAddress: TextView = itemView.findViewById(R.id.txt_address)
        val txtCompensation: TextView = itemView.findViewById(R.id.txt_compensation)
        val txtDateTime: TextView = itemView.findViewById(R.id.txt_date_time)
        val txtVehicle: TextView = itemView.findViewById(R.id.txt_vehicle)
        val btnAction: MaterialButton = itemView.findViewById(R.id.btn_apply)
    }
}
