package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.app.bemyrider.Adapter.User.PopularServiceAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentServiceListBinding;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.ServiceListViewModel;

import java.util.ArrayList;

public class ServiceListFragment extends Fragment {

    private FragmentServiceListBinding binding;
    private PopularServiceAdapter populerServiceAdapter;
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();
    private ServiceListViewModel viewModel;
    private String providerId = "";
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_list, container, false);
        if (getArguments() != null) {
            providerId = getArguments().getString(Utils.PROVIDER_ID, "");
        }
        context = getActivity();

        populerServiceAdapter = new PopularServiceAdapter(context, serviceDataItems, providerId);
        binding.rvPopularCategories.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        binding.rvPopularCategories.setItemAnimator(new DefaultItemAnimator());
        binding.rvPopularCategories.setAdapter(populerServiceAdapter);

        viewModel = new ViewModelProvider(this).get(ServiceListViewModel.class);
        observeViewModel();

        binding.progress.setVisibility(View.VISIBLE);
        binding.rvPopularCategories.setVisibility(View.GONE);
        viewModel.loadPopularServices(providerId);

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getPopularServices().observe(getViewLifecycleOwner(), pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.rvPopularCategories.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                serviceDataItems.clear();
                serviceDataItems.addAll(pojo.getData());
                populerServiceAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                if (getActivity() != null) getActivity().finish();
            }
        });
    }
}
