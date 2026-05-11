package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.MessageListAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityMessagesBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.MessageListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.MessageListViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private MessageListViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;

    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    private boolean pendingClear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(MessagesActivity.this, R.layout.activity_messages, null);

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
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, pojo -> {
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

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        context = MessagesActivity.this;
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.message), HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setHomeButtonEnabled(true); actionBar.setDisplayHomeAsUpEnabled(true); }
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        messageListPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerMessageList.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(messageListPojoItems, this);
        binding.recyclerMessageList.setAdapter(messageListAdapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); loadMessages(true); });
    }

    private void loadMessages(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecordMes.setVisibility(View.GONE); binding.recyclerMessageList.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadMessages(PrefsUtil.with(this).readString("UserId"), page);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); EventBus.getDefault().register(this); }
    @Override public void onStop() { super.onStop(); EventBus.getDefault().unregister(this); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try { if (event.getType().equalsIgnoreCase("msg")) loadMessages(true); } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
