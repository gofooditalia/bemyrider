package com.app.bemyrider.activity.user;

import android.content.Context;
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
import com.app.bemyrider.databinding.ActivityMessagesBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.MessageListPojo;
import com.app.bemyrider.model.MessageListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */


public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private WebServiceCall messageListAsync;
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
        binding = DataBindingUtil.setContentView(MessagesActivity.this, R.layout.activity_messages, null);

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
        context = MessagesActivity.this;

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

        messageListPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(MessagesActivity.this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerMessageList.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(messageListPojoItems, MessagesActivity.this);
        binding.recyclerMessageList.setAdapter(messageListAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetMessageList(true);
        });
    }

    /*-------------- Get Message List Api Call -----------------*/
    private void serviceCallGetMessageList(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordMes.setVisibility(View.GONE);
            binding.recyclerMessageList.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(MessagesActivity.this).readString("UserId"));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(MessagesActivity.this, WebServiceUrl.URL_GET_MESSAGE_LIST,
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
                        binding.txtNoRecordMes.setVisibility(View.GONE);
                        binding.recyclerMessageList.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerMessageList.setVisibility(View.GONE);
                        binding.txtNoRecordMes.setVisibility(View.VISIBLE);
                    }
                    try {
                        total_records = messageListPojo.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                messageListAsync = (WebServiceCall) asyncTask;
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            //{customer_id=2, service_id=19, notification_type=m, user_id=2, title=Robert Brown sent you message on Room Lighting service, user_type=c, service_request_id=233, provider_id=1}
            //JSONObject object = new JSONObject(event.getData());
            if (event.getType().equalsIgnoreCase("msg")) {
                serviceCallGetMessageList(true);
            }
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
        Utils.cancelAsyncTask(messageListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
