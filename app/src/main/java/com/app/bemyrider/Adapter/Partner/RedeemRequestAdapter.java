package com.app.bemyrider.Adapter.Partner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.model.RedeemHistoryPojoItem;
import com.app.bemyrider.R;

import java.util.ArrayList;

/**
 * Created by nct33 on 9/11/17.
 */

public class RedeemRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<RedeemHistoryPojoItem> arrayList;
    private Context context;

    public RedeemRequestAdapter(ArrayList<RedeemHistoryPojoItem> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            // inflate your layout and pass it to view holder
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.redeem_request_history_new, parent,
                    false);
            return new ViewHolderItem(view);
        } /*
           * else if (viewType == TYPE_HEADER) {
           * //inflate your layout and pass it to view holder
           * View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.
           * redeem_history_header, parent, false);
           * return new ViewHolderHeader(view);
           * }
           */

        throw new RuntimeException(
                "there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderHeader) {
            // cast holder to VHItem and set data
        } else if (holder instanceof ViewHolderItem) {
            // cast holder to VHHeader and set data for header.
            ((ViewHolderItem) holder).txt_request_amount_redeem.setText(arrayList.get(position).getRedeemedAmount());
            ((ViewHolderItem) holder).txt_admin_fees_redeem.setText(arrayList.get(position).getAdminFees());
            ((ViewHolderItem) holder).txt_redeem_date_redeem.setText(arrayList.get(position).getRedeemedDate());
            ((ViewHolderItem) holder).txt_redeem_amount_redeem.setText(arrayList.get(position).getRequestedAmount());
            ((ViewHolderItem) holder).txt_requested_date_redeem.setText(arrayList.get(position).getRequestedDate());

            ((ViewHolderItem) holder).imgInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION)
                        return;

                    final Dialog d = new Dialog(context);
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    d.setContentView(inflater.inflate(R.layout.dialog_redeem_info, null));

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = d.getWindow();
                    lp.copyFrom(window.getAttributes());
                    // This makes the dialog take up the full width
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setAttributes(lp);

                    Button btnClose = (Button) d.findViewById(R.id.btnClose);
                    final TextView txtRequestAmount = (TextView) d.findViewById(R.id.txtRequestAmount);
                    final TextView txtAdminFees = (TextView) d.findViewById(R.id.txtAdminFees);
                    final TextView txtRequestDate = (TextView) d.findViewById(R.id.txtRequestDate);
                    final TextView txtRedeemDate = (TextView) d.findViewById(R.id.txtRedeemDate);
                    final TextView txtRedeemAmount = (TextView) d.findViewById(R.id.txtRedeemAmount);

                    txtRequestAmount.setText(arrayList.get(adapterPosition).getRequestedAmount());
                    txtAdminFees.setText(arrayList.get(adapterPosition).getAdminFees());
                    txtRequestDate.setText(arrayList.get(adapterPosition).getRequestedDate());
                    txtRedeemDate.setText(arrayList.get(adapterPosition).getRedeemedDate());
                    txtRedeemAmount.setText(arrayList.get(adapterPosition).getRedeemedAmount());

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                        }
                    });
                    d.show();

                }
            });
        }
    }

    /*
     * @Override
     * public void onBindViewHolder(ViewHolder holder, int position) {
     * holder.txt_request_amount_redeem.setText(arrayList.get(position).
     * getRedeemedAmount());
     * holder.txt_admin_fees_redeem.setText(arrayList.get(position).getAdminFees());
     * holder.txt_redeem_date_redeem.setText(arrayList.get(position).getRedeemedDate
     * ());
     * holder.txt_redeem_amount_redeem.setText(arrayList.get(position).
     * getRequestedAmount());
     * holder.txt_requested_date_redeem.setText(arrayList.get(position).
     * getRequestedDate());
     * }
     */

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        /*
         * if (position==0)
         * return TYPE_HEADER;
         */
        return TYPE_ITEM;
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder {

        private TextView txt_request_amount_redeem, txt_admin_fees_redeem, txt_requested_date_redeem,
                txt_redeem_amount_redeem, txt_redeem_date_redeem;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            txt_request_amount_redeem = itemView.findViewById(R.id.txt_request_amount_redeem);
            txt_admin_fees_redeem = itemView.findViewById(R.id.txt_admin_fees_redeem);
            txt_requested_date_redeem = itemView.findViewById(R.id.txt_requested_date_redeem);
            txt_redeem_amount_redeem = itemView.findViewById(R.id.txt_redeem_amount_redeem);
            txt_redeem_date_redeem = itemView.findViewById(R.id.txt_redeem_date_redeem);

        }
    }

    public class ViewHolderItem extends RecyclerView.ViewHolder {

        private TextView txt_request_amount_redeem, txt_admin_fees_redeem, txt_requested_date_redeem,
                txt_redeem_amount_redeem, txt_redeem_date_redeem;
        private ImageView imgInfo;

        public ViewHolderItem(View itemView) {
            super(itemView);
            txt_request_amount_redeem = itemView.findViewById(R.id.txt_request_amount_redeem);
            txt_admin_fees_redeem = itemView.findViewById(R.id.txt_admin_fees_redeem);
            txt_requested_date_redeem = itemView.findViewById(R.id.txt_requested_date_redeem);
            txt_redeem_amount_redeem = itemView.findViewById(R.id.txt_redeem_amount_redeem);
            txt_redeem_date_redeem = itemView.findViewById(R.id.txt_redeem_date_redeem);
            imgInfo = itemView.findViewById(R.id.imgInfo);

        }
    }

}
