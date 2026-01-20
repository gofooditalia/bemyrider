package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CustomerMessagesFragment extends Fragment {

    private ActivityMessagesBinding binding;
    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private WebServiceCall messageListAsync;
    private Context context;
    private AppCompatActivity activity;

    private ConnectionManager connectionManager;

    /* pagination vars start */
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_messages, container, false);
        context = getContext();
        activity = (AppCompatActivity) getActivity();

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

        return binding.getRoot();
    }

    private void initViews() {
        activity.setSupportActionBar(binding.toolbar);
        /* Init Internet Connection Class For No Internet Banner */
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        messageListPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.recyclerMessageList.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(messageListPojoItems, context);
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

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(context, WebServiceUrl.URL_GET_MESSAGE_LIST,
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
                        messageListAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        messageListAsync = null;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            // {customer_id=2, service_id=19, notification_type=m, user_id=2, title=Robert
            // Brown sent you message on Room Lighting service, user_type=c,
            // service_request_id=233, provider_id=1}
            // JSONObject object = new JSONObject(event.getData());
            if (event.getType().equalsIgnoreCase("msg")) {
                serviceCallGetMessageList(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(messageListAsync);
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
