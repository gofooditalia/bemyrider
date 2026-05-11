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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.MessageListAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityMessagesBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.MessageListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.MessageListViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class CustomerMessagesFragment extends Fragment {

    private ActivityMessagesBinding binding;
    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private MessageListViewModel viewModel;
    private Context context;
    private AppCompatActivity activity;
    private ConnectionManager connectionManager;

    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    private boolean pendingClear = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_messages, container, false);
        context = getContext();
        activity = (AppCompatActivity) getActivity();

        initViews();

        viewModel = new ViewModelProvider(this).get(MessageListViewModel.class);
        observeViewModel();
        loadMessages(true);

        binding.recyclerMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        if (messageListPojoItems.size() < total_records) {
                            page++;
                            loadMessages(false);
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { messageListPojoItems.clear(); pendingClear = false; }
                messageListPojoItems.addAll(pojo.getData().getMessageList());
                messageListAdapter.notifyDataSetChanged();
                loading = true;
                boolean hasItems = !messageListPojoItems.isEmpty();
                binding.txtNoRecordMes.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.recyclerMessageList.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        if (activity != null) activity.setSupportActionBar(binding.toolbar);
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        messageListPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.recyclerMessageList.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(messageListPojoItems, context);
        binding.recyclerMessageList.setAdapter(messageListAdapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); loadMessages(true); });
    }

    private void loadMessages(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecordMes.setVisibility(View.GONE); binding.recyclerMessageList.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadMessages(PrefsUtil.with(context).readString("UserId"), page);
    }

    @Override public void onStart() { super.onStart(); if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try { if (event.getType().equalsIgnoreCase("msg")) loadMessages(true); } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        EventBus.getDefault().unregister(this);
    }
}
