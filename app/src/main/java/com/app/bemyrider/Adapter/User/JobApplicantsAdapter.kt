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
import com.app.bemyrider.model.JobApplicantPojoItem
import com.google.android.material.button.MaterialButton

/**
 * Adapter for Job Applicants list.
 * Optimized with Native Placeholders by Gemini - 2024.
 */
class JobApplicantsAdapter(
    private val context: Context,
    private var applicantList: List<JobApplicantPojoItem>,
    private val listener: ApplicantInteractionListener
) : RecyclerView.Adapter<JobApplicantsAdapter.ApplicantViewHolder>() {

    interface ApplicantInteractionListener {
        fun onRiderProfileClicked(applicant: JobApplicantPojoItem)
        fun onHireClicked(applicant: JobApplicantPojoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_job_applicant, parent, false)
        return ApplicantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        val applicant = applicantList[position]

        val name = applicant.riderName ?: "Rider"
        holder.txtRiderName.text = name
        holder.txtRating.text = applicant.rating ?: "0.0"
        holder.txtDeliveries.text = "(${applicant.totalDeliveries ?: "0"} consegne)"
        holder.txtMargin.text = "Veicolo: ${applicant.vehicleType?.uppercase() ?: "INDIFF."}"

        // --- FINAL IMAGE LOGIC (No POST, No Thread issues) ---
        val imageUrl = applicant.riderImage
        val isExplicitlyEmpty = imageUrl.isNullOrEmpty() || 
                               imageUrl.lowercase().contains("no_user_image.png")

        if (!isExplicitlyEmpty) {
            holder.ivRider.load(imageUrl) {
                placeholder(R.drawable.account_circle_24)
                error(R.drawable.account_circle_24)
                // Do not use crossfade with custom CircleImageView
            }
        } else {
            holder.ivRider.load(R.drawable.account_circle_24)
        }
        // --------------------------------------------------------

        holder.itemView.setOnClickListener { listener.onRiderProfileClicked(applicant) }
        holder.btnHire.setOnClickListener { listener.onHireClicked(applicant) }
    }

    override fun getItemCount(): Int = applicantList.size

    fun updateData(newList: List<JobApplicantPojoItem>) {
        this.applicantList = newList
        notifyDataSetChanged()
    }

    class ApplicantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRider: ImageView = itemView.findViewById(R.id.iv_rider)
        val txtRiderName: TextView = itemView.findViewById(R.id.txt_rider_name)
        val txtRating: TextView = itemView.findViewById(R.id.txt_rating)
        val txtDeliveries: TextView = itemView.findViewById(R.id.txt_deliveries)
        val txtMargin: TextView = itemView.findViewById(R.id.txt_vehicle)
        val btnHire: MaterialButton = itemView.findViewById(R.id.btn_hire)
    }
}
