package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RedeemRequestAdapter;
import com.app.bemyrider.Adapter.User.DepositHistoryAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.WalletViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.databinding.FragmentWalletBinding;
import com.app.bemyrider.model.DepositHistoryItem;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.RedeemHistoryPojoItem;
import com.app.bemyrider.model.WalletDetailsPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/*
 * Created by nct121 on 5/12/16.
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class WalletActivity extends AppCompatActivity {

    private FragmentWalletBinding binding;
    private ArrayList<DepositHistoryItem> depositHistoryItems;
    private ArrayList<RedeemHistoryPojoItem> redeemHistoryPojoItems;
    private DepositHistoryAdapter depositHistoryAdapter;
    private RedeemRequestAdapter redeemRequestAdapter;
    private WalletViewModel viewModel;
    private Context context;
    private Activity activity;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = WalletActivity.this;
        activity = WalletActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.fragment_wallet, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);
        observeViewModel();

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetWalletDetails();
            serviceCallGetDepositHistory();
            serviceCallGetRedeemHistory();
        } else {
            getOfflineDetails();
        }

        binding.btnReedem.setOnClickListener(view -> {
            binding.btnReedem.setClickable(false);
            serviceCallRedeem();
        });

        binding.btnDeposite.setOnClickListener(view -> {
            Intent intent = new Intent(activity, DepositFundActivity.class);
            intent.putExtra("actual_amount", binding.TxtShowCurBal.getText().toString().trim());
            myActivityResultLauncher.launch(intent);
        });
    }



    private void initViews() {
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        depositHistoryItems = new ArrayList<>();
        depositHistoryAdapter = new DepositHistoryAdapter(depositHistoryItems, context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.recyclerDepositeHistory.setLayoutManager(layoutManager);
        binding.recyclerDepositeHistory.setAdapter(depositHistoryAdapter);

        redeemHistoryPojoItems = new ArrayList<>();
        redeemRequestAdapter = new RedeemRequestAdapter(redeemHistoryPojoItems, context);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.recyclerRedeemHistory.setLayoutManager(layoutManager1);
        binding.recyclerRedeemHistory.setAdapter(redeemRequestAdapter);

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myActivityResult();
    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    serviceCallGetWalletDetails();
                    serviceCallGetDepositHistory();
                }
            }
        });
    }

    private void observeViewModel() {
        String sign = PrefsUtil.with(activity).readString("CurrencySign");

        viewModel.getWalletDetails().observe(this, pojo -> {
            binding.pgShowCurBal.setVisibility(View.GONE);
            binding.pgShowFund.setVisibility(View.GONE);
            binding.pgShowRedeemReq.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                binding.TxtShowCurBal.setText(String.format("%s%s", sign, pojo.getData().getWalletAmount()));
                binding.TxtShowFund.setText(String.format("%s%s", sign, pojo.getData().getHoldAmount()));
                binding.TxtShowReedemRequest.setText(String.format("%s%s", sign, pojo.getData().getRedeemRequestedAmount()));
                binding.btnDeposite.setClickable(true);
            }
        });

        viewModel.getDepositHistory().observe(this, pojo -> {
            binding.llProgress.setVisibility(View.GONE);
            binding.layoutDeposit.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                depositHistoryItems.clear();
                depositHistoryItems.addAll(pojo.getData());
                if (depositHistoryItems.isEmpty()) binding.layoutDeposit.setVisibility(View.GONE);
                depositHistoryAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getRedeemHistory().observe(this, pojo -> {
            binding.llProgress.setVisibility(View.GONE);
            binding.layoutRedeemRequest.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                redeemHistoryPojoItems.addAll(pojo.getData());
                if (redeemHistoryPojoItems.isEmpty()) binding.layoutRedeemRequest.setVisibility(View.GONE);
                redeemRequestAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getRedeemResult().observe(this, result -> {
            binding.pgRedeem.setVisibility(View.GONE);
            binding.btnReedem.setClickable(true);
            if (result != null) {
                Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (result.isStatus()) onBackPressed();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
        });
    }

    private void serviceCallGetRedeemHistory() {
        binding.layoutRedeemRequest.setVisibility(View.GONE);
        binding.llProgress.setVisibility(View.VISIBLE);
        viewModel.loadRedeemHistory(PrefsUtil.with(activity).readString("UserId"));
    }

    private void serviceCallGetDepositHistory() {
        binding.layoutDeposit.setVisibility(View.GONE);
        binding.llProgress.setVisibility(View.VISIBLE);
        viewModel.loadDepositHistory(PrefsUtil.with(activity).readString("UserId"));
    }

    private void serviceCallRedeem() {
        binding.pgRedeem.setVisibility(View.VISIBLE);
        viewModel.sendRedeemRequest(PrefsUtil.with(activity).readString("UserId"));
    }

    private void serviceCallGetWalletDetails() {
        binding.btnDeposite.setClickable(false);
        binding.pgShowCurBal.setVisibility(View.VISIBLE);
        binding.pgShowFund.setVisibility(View.VISIBLE);
        binding.pgShowRedeemReq.setVisibility(View.VISIBLE);
        viewModel.loadWalletDetails(PrefsUtil.with(activity).readString("UserId"));
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    getOfflineDetails();
                    binding.btnDeposite.setVisibility(View.GONE);
                    binding.layoutRedeemRequest.setVisibility(View.GONE);
                    binding.llBtnRedeem.setVisibility(View.GONE);
                   // binding.viewRedeem.setVisibility(View.GONE);
                    binding.layoutDeposit.setVisibility(View.GONE);
                   // binding.viewSep.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOfflineDetails() {
        try {
            binding.llProgress.setVisibility(View.GONE);
            binding.pgShowCurBal.setVisibility(View.GONE);
            binding.pgShowFund.setVisibility(View.GONE);
            binding.pgShowRedeemReq.setVisibility(View.GONE);
            Log.e("Offline", "onMessageEvent: My Resolution");
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONObject serviceList = dataObj.getJSONObject("walletDetails");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            WalletDetailsPojoItem walletDetails = gson.fromJson(serviceList.toString(), WalletDetailsPojoItem.class);
            WalletDetailsPojo walletDetailsPojo = new WalletDetailsPojo();
            walletDetailsPojo.setData(walletDetails);
            binding.TxtShowCurBal.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getWalletAmount()));
            binding.TxtShowFund.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getHoldAmount()));
            binding.TxtShowReedemRequest.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getRedeemRequestedAmount()));

//            new ConnectionCheck().showDialogWithMessage(activity, getString(R.string.sync_data_message)).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

}
