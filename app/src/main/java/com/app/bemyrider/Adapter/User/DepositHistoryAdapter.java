package com.app.bemyrider.Adapter.User;

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

import com.app.bemyrider.model.DepositHistoryItem;
import com.app.bemyrider.R;

import java.util.ArrayList;

/**
 * Created by nct33 on 9/11/17.
 */

public class DepositHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<DepositHistoryItem> arrayList;
    private Context context;

    public DepositHistoryAdapter(ArrayList<DepositHistoryItem> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            // inflate your layout and pass it to view holder
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deposit_history_item_new, parent,
                    false);
            return new ViewHolderItem(view);
        } /*
           * else if (viewType == TYPE_HEADER) {
           * //inflate your layout and pass it to view holder
           * View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.
           * deposit_history_header,parent,false);
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
            ((ViewHolderItem) holder).txt_admin_fees_deposit.setText(arrayList.get(position).getAdminFees());
            ((ViewHolderItem) holder).txt_amount_deposit.setText(arrayList.get(position).getAmount());
            ((ViewHolderItem) holder).txt_date_deposit.setText(arrayList.get(position).getDate());
            ((ViewHolderItem) holder).txt_transaction_id_deposit.setText(arrayList.get(position).getTransactionId());

            ((ViewHolderItem) holder).imgInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION)
                        return;

                    final Dialog d = new Dialog(context);
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    d.setContentView(inflater.inflate(R.layout.dialog_payment_info, null));

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = d.getWindow();
                    lp.copyFrom(window.getAttributes());
                    // This makes the dialog take up the full width
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setAttributes(lp);

                    Button btnClose = (Button) d.findViewById(R.id.btnClose);
                    final TextView txtAmount = (TextView) d.findViewById(R.id.txtAmount);
                    final TextView txtAdminFees = (TextView) d.findViewById(R.id.txtAdminFees);
                    final TextView txtDate = (TextView) d.findViewById(R.id.txtDate);
                    final TextView txtTransactionId = (TextView) d.findViewById(R.id.txtTransactionId);

                    txtAmount.setText(arrayList.get(adapterPosition).getAmount());
                    txtAdminFees.setText(arrayList.get(adapterPosition).getAdminFees());
                    txtDate.setText(arrayList.get(adapterPosition).getDate());
                    txtTransactionId.setText(arrayList.get(adapterPosition).getTransactionId());

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
     * holder.txt_admin_fees_deposit.setText(arrayList.get(position).getAdminFees())
     * ;
     * holder.txt_amount_deposit.setText(arrayList.get(position).getAmount());
     * holder.txt_date_deposit.setText(arrayList.get(position).getDate());
     * holder.txt_transaction_id_deposit.setText(arrayList.get(position).
     * getTransactionId());
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

        private TextView txt_amount_deposit, txt_admin_fees_deposit, txt_date_deposit, txt_transaction_id_deposit;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            txt_amount_deposit = itemView.findViewById(R.id.txt_amount_deposit);
            txt_admin_fees_deposit = itemView.findViewById(R.id.txt_admin_fees_deposit);
            txt_date_deposit = itemView.findViewById(R.id.txt_date_deposit);
            txt_transaction_id_deposit = itemView.findViewById(R.id.txt_transaction_id_deposit);
        }
    }

    public class ViewHolderItem extends RecyclerView.ViewHolder {

        private TextView txt_amount_deposit, txt_admin_fees_deposit, txt_date_deposit, txt_transaction_id_deposit;
        private ImageView imgInfo;

        public ViewHolderItem(View itemView) {
            super(itemView);
            txt_amount_deposit = itemView.findViewById(R.id.txt_amount_deposit);
            txt_admin_fees_deposit = itemView.findViewById(R.id.txt_admin_fees_deposit);
            txt_date_deposit = itemView.findViewById(R.id.txt_date_deposit);
            txt_transaction_id_deposit = itemView.findViewById(R.id.txt_transaction_id_deposit);
            imgInfo = itemView.findViewById(R.id.imgInfo);
        }
    }
}
