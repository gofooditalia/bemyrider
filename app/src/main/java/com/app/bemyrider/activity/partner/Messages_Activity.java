package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.MessageListAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityMessagesBinding;
import com.app.bemyrider.model.MessageListPojo;
import com.app.bemyrider.model.MessageListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class Messages_Activity extends AppCompatActivity {

    private PartnerActivityMessagesBinding binding;
    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private SharedPreferences preferences;
    private AsyncTask messageListAsync;
    private Context context;
    private ConnectionManager connectionManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(Messages_Activity.this, R.layout.partner_activity_messages, null);

        initViews();

        serviceCallGetMessageList(true);

        binding.recyclerMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            if (messageListPojoItems.size() < total_records) {
                                page++;
                                serviceCallGetMessageList(false);
                            }
                        }
                    }
                }
            }
        });
    }

    private void initViews() {
        context = Messages_Activity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.message),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        preferences = getSharedPreferences("Unique", MODE_PRIVATE);

        messageListPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(Messages_Activity.this, RecyclerView.VERTICAL, false);
        binding.recyclerMessageList.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(messageListPojoItems, Messages_Activity.this);
        binding.recyclerMessageList.setAdapter(messageListAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetMessageList(true);
        });
    }

    /*------------- Message List Api Call --------------*/
    private void serviceCallGetMessageList(boolean isClear) {

        if (isClear) {
            page = 1;
            binding.txtNoRecordMesP.setVisibility(View.GONE);
            binding.recyclerMessageList.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(Messages_Activity.this).readString("UserId"));
        textParams.put("page", String.valueOf(page));
        textParams.put("lId", preferences.getString("lanId", "1"));

        new WebServiceCall(Messages_Activity.this, WebServiceUrl.URL_GET_MESSAGE_LIST,
                textParams, MessageListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    MessageListPojo messageListPojo = (MessageListPojo) obj;
                    if (isClear) {
                        messageListPojoItems.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.recyclerMessageList.setVisibility(View.VISIBLE);

                    messageListPojoItems.addAll(messageListPojo.getData().getMessageList());
                    messageListAdapter.notifyDataSetChanged();
                    loading = true;
                    if (messageListPojoItems.size() > 0) {
                        binding.txtNoRecordMesP.setVisibility(View.GONE);
                        binding.recyclerMessageList.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerMessageList.setVisibility(View.GONE);
                        binding.txtNoRecordMesP.setVisibility(View.VISIBLE);
                    }
                    try {
                        total_records = messageListPojo.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(Messages_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                messageListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                messageListAsync = null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(messageListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
