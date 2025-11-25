package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.app.bemyrider.databinding.FragmentWalletBinding;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

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
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.DepositHistoryItem;
import com.app.bemyrider.model.DepositHistoryPojo;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.RedeemHistoryPojo;
import com.app.bemyrider.model.RedeemHistoryPojoItem;
import com.app.bemyrider.model.WalletDetailsPojo;
import com.app.bemyrider.model.WalletDetailsPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
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
import java.util.LinkedHashMap;

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
    private AsyncTask redeemHistoryAsync, depositHistoryAsync, requestRedeemAsync,
            walletDetailAsync;
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

        if (PrefsUtil.with(activity).readString("UserType").equals("c")) {
            binding.layoutRedeem.setVisibility(View.GONE);
            binding.llBtnRedeem.setVisibility(View.GONE);
            //binding.viewRedeem.setVisibility(View.GONE);
            serviceCallGetDepositHistory();
        } else {
            binding.btnDeposite.setVisibility(View.GONE);
            serviceCallGetRedeemHistory();
        }

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetWalletDetails();
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

    /*-------------- Get Redeem History Api Call ----------------*/
    private void serviceCallGetRedeemHistory() {
        binding.layoutRedeemRequest.setVisibility(View.GONE);
        binding.llProgress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_REDEEM_HISTORY,
                textParams, RedeemHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.llProgress.setVisibility(View.GONE);
                binding.layoutRedeemRequest.setVisibility(View.VISIBLE);
                if (status) {
                    RedeemHistoryPojo redeemHistoryPojo = (RedeemHistoryPojo) obj;
                    redeemHistoryPojoItems.addAll(redeemHistoryPojo.getData());
                    if (redeemHistoryPojoItems.size() == 0) {
                        binding.layoutRedeemRequest.setVisibility(View.GONE);
                       // binding.viewSep.setVisibility(View.GONE);
                    }
                    redeemRequestAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                redeemHistoryAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                redeemHistoryAsync = null;
            }
        });
    }

    /*------------- Get Deposit History Api Call ---------------*/
    private void serviceCallGetDepositHistory() {
        binding.layoutDeposit.setVisibility(View.GONE);
        binding.llProgress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_DEPOSITE_HISTORY,
                textParams, DepositHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.llProgress.setVisibility(View.GONE);
                binding.layoutDeposit.setVisibility(View.VISIBLE);
                if (status) {
                    depositHistoryItems.clear();
                    DepositHistoryPojo depositHistoryPojo = (DepositHistoryPojo) obj;
                    depositHistoryItems.addAll(depositHistoryPojo.getData());
                    if (depositHistoryItems.size() == 0) {
                        binding.layoutDeposit.setVisibility(View.GONE);
                       // binding.viewSep.setVisibility(View.GONE);
                    }
                    depositHistoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                depositHistoryAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                depositHistoryAsync = null;
            }
        });
    }

    /*---------------- Redeem Request Api Call -------------------*/
    private void serviceCallRedeem() {
        binding.pgRedeem.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_REDDEMRE_REQUEST,
                textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgRedeem.setVisibility(View.GONE);
                binding.btnReedem.setClickable(true);
                if (status) {
                    Toast.makeText(context, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                requestRedeemAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                requestRedeemAsync = null;
            }
        });
    }

    /*--------------- Get Wallet Detail Api Call -----------------*/
    private void serviceCallGetWalletDetails() {
        binding.btnDeposite.setClickable(false);
        binding.pgShowCurBal.setVisibility(View.VISIBLE);
        binding.pgShowFund.setVisibility(View.VISIBLE);
        binding.pgShowRedeemReq.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_GET_WALLET_DETAILS,
                textParams, WalletDetailsPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgShowCurBal.setVisibility(View.GONE);
                binding.pgShowFund.setVisibility(View.GONE);
                binding.pgShowRedeemReq.setVisibility(View.GONE);
                if (status) {
                    WalletDetailsPojo walletDetailsPojo = (WalletDetailsPojo) obj;
                    binding.TxtShowCurBal.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getWalletAmount()));
                    binding.TxtShowFund.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getHoldAmount()));
                    binding.TxtShowReedemRequest.setText(String.format("%s%s", PrefsUtil.with(activity).readString("CurrencySign"), walletDetailsPojo.getData().getRedeemRequestedAmount()));
                    binding.btnDeposite.setClickable(true);
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                walletDetailAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                walletDetailAsync = null;
            }
        });
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
        Utils.cancelAsyncTask(redeemHistoryAsync);
        Utils.cancelAsyncTask(depositHistoryAsync);
        Utils.cancelAsyncTask(requestRedeemAsync);
        Utils.cancelAsyncTask(walletDetailAsync);
        super.onDestroy();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

}
