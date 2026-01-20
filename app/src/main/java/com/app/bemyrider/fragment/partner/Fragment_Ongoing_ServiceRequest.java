package com.app.bemyrider.fragment.partner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvOngoingServiceRequestAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerServicehistoryOngoingListBinding;
import com.app.bemyrider.model.ProviderHistoryPojo;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class Fragment_Ongoing_ServiceRequest extends Fragment {

    private static final String TAG = "FragmentOngoingService";
    private PartnerServicehistoryOngoingListBinding binding;
    private RvOngoingServiceRequestAdapter adapter;
    private ArrayList<ProviderHistoryPojoItem> ongoingarrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private String keyWord = "";
    private WebServiceCall ongoingServiceAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicehistory_ongoing_list, container, false);
        page = 1;
        initView();

        serviceCallGetOngoingServices(true);

        binding.rvOnGoingServiceRequest.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            serviceCallGetOngoingServices(false);
                        }
                    }
                }
            }
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyWord = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(getActivity());
            binding.rvOnGoingServiceRequest.setVisibility(View.GONE);
            serviceCallGetOngoingServices(true);
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.imgSearch.performClick();
                return true;
            }
            return false;
        });

        return binding.getRoot();

    }

    /*------------------ On Going Service Api Call -------------------*/
    private void serviceCallGetOngoingServices(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.layoutNoRecord.setVisibility(View.GONE);
            binding.rvOnGoingServiceRequest.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(getActivity()).readString("UserId"));
        textParams.put("tab", "ongoing");
        textParams.put("keyword", Utils.encodeEmoji(keyWord));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(getActivity(), WebServiceUrl.URL_SERVICE_REQUEST_LIST, textParams,
                ProviderHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ProviderHistoryPojo pojo = (ProviderHistoryPojo) obj;
                    if (page == 1) {
                        ongoingarrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvOnGoingServiceRequest.setVisibility(View.VISIBLE);
                    ongoingarrayList.addAll(pojo.getData().getServiceList());
                    if (!(ongoingarrayList.size() > 0)) {
                        binding.layoutNoRecord.setVisibility(View.VISIBLE);
                        binding.rvOnGoingServiceRequest.setVisibility(View.GONE);
                    } else {
                        binding.layoutNoRecord.setVisibility(View.GONE);
                        binding.rvOnGoingServiceRequest.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();

                    total_page = pojo.getData().getPagination().getTotalPages();
                    page = pojo.getData().getPagination().getCurrentPage();
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            }

            @Override
            public void onAsync(Object asyncTask) {
                ongoingServiceAsync = null;
            }

            @Override
            public void onCancelled() {
                ongoingServiceAsync = null;
            }
        });
    }

    private void initView() {

        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvOnGoingServiceRequest.setLayoutManager(layoutManager);
        binding.rvOnGoingServiceRequest.setHasFixedSize(false);
        adapter = new RvOngoingServiceRequestAdapter(getActivity(), ongoingarrayList);
        binding.rvOnGoingServiceRequest.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetOngoingServices(true);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Registra EventBus per ascoltare le notifiche push
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ricarica i dati quando l'utente torna alla schermata
        // Solo se non è già in caricamento per evitare chiamate multiple
        if (!isLoading && page == 1) {
            serviceCallGetOngoingServices(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Deregistra EventBus quando il fragment non è più visibile
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * Ascolta le notifiche push tramite EventBus e aggiorna automaticamente la lista
     * quando arriva una nuova richiesta di servizio
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            // Aggiorna la lista quando arriva una notifica di tipo "s" (service)
            // Questo viene inviato da MyFirebaseMessagingService quando arriva una notifica push
            if (event.getType() != null && event.getType().equalsIgnoreCase("s")) {
                Log.d(TAG, "Service notification received via EventBus, refreshing list...");
                // Ricarica la lista delle richieste in corso
                serviceCallGetOngoingServices(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling EventBus message", e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(ongoingServiceAsync);
        // Deregistra EventBus se non già fatto
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
