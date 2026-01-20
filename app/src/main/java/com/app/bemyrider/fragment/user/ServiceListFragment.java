package com.app.bemyrider.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.PopularServiceAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentServiceListBinding;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.model.ServiceListPOJO;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class ServiceListFragment extends Fragment {

    private FragmentServiceListBinding binding;
    private PopularServiceAdapter populerServiceAdapter;
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();
    private WebServiceCall categoryListAsync, subCategoryAsync, popularServiceAsync;
    private String categoryId = "";
    private String subCategoryId = "";
    private String providerId = "";
    private Context context;
    private Activity activity;
    private ArrayList<CategoryDataItem> categoryDataItems = new ArrayList<>();
    private ArrayList<SubCategoryItem> subCategoryItems = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_list, container, false);
        if (getArguments() != null) {
            providerId = getArguments().getString(Utils.PROVIDER_ID);
        }
        init();
        getCategory();
        return binding.getRoot();
    }

    private void init() {
        context = getActivity();
        activity = getActivity();

        populerServiceAdapter = new PopularServiceAdapter(context, serviceDataItems, providerId);
        binding.rvPopularCategories.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        binding.rvPopularCategories.setItemAnimator(new DefaultItemAnimator());
        binding.rvPopularCategories.setAdapter(populerServiceAdapter);
    }

    /*----------------- Get Category Api Call -------------------*/
    private void getCategory() {
        binding.rvPopularCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_id", providerId);

        new WebServiceCall(context, WebServiceUrl.URL_CATEGORYLIST, textParams,
                CategoryListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                try {
                    if (status) {
                        CategoryListPOJO listPojo = (CategoryListPOJO) obj;
                        categoryDataItems.clear();
                        categoryDataItems.addAll(listPojo.getData());
                        if (categoryDataItems.size() > 0) {
                            categoryId = categoryDataItems.get(0).getCategoryId();
                            getSubCategory();
                        } else {
                            Toast.makeText(context,
                                    listPojo.getMessage(), Toast.LENGTH_LONG).show();
                            activity.finish();
                        }
                    } else {
                        Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                categoryListAsync = null;
            }

            @Override
            public void onCancelled() {
                categoryListAsync = null;
            }
        });
    }


    /*---------------- Get Sub Category Api Call -------------------*/
    private void getSubCategory() {
        binding.rvPopularCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("category_id", categoryId);
        textParams.put("provider_id", providerId);

        new WebServiceCall(context, WebServiceUrl.URL_SUBCATEGORYLIST, textParams,
                SubCategoryListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                try {
                    if (status) {
                        List<String> stringList = new ArrayList<>();
                        SubCategoryListPojo listPojo = (SubCategoryListPojo) obj;
                        subCategoryItems.clear();
                        subCategoryItems.addAll(listPojo.getData());
                        if (subCategoryItems.size() > 0) {
                            for (int i = 0; i < subCategoryItems.size(); i++) {
                                if (subCategoryItems.get(i).getCategoryId() != null) {
                                    stringList.add(String.valueOf(subCategoryItems.get(i).getCategoryId()));
                                    subCategoryId = TextUtils.join(",", stringList);
                                }
                            }
                            getPopularService();
                        } else {
                            Toast.makeText(context,
                                    listPojo.getMessage(), Toast.LENGTH_LONG).show();
                            activity.finish();
                        }
                    } else {
                        Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                subCategoryAsync = null;
            }

            @Override
            public void onCancelled() {
                subCategoryAsync = null;
            }
        });
    }


    /*------------------ Get Popular Service Api Call ---------------------*/
    private void getPopularService() {
        binding.rvPopularCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("sub_category_id", subCategoryId);
        textParams.put("provider_id", providerId);

        new WebServiceCall(getActivity(), WebServiceUrl.URL_POPULARSERVICS, textParams,
                ServiceListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rvPopularCategories.setVisibility(View.VISIBLE);
                if (status) {
                    ServiceListPOJO serviceListPojo = (ServiceListPOJO) obj;
                    serviceDataItems.clear();
                    serviceDataItems.addAll(serviceListPojo.getData());
                    populerServiceAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                popularServiceAsync = null;
            }

            @Override
            public void onCancelled() {
                popularServiceAsync = null;
            }
        });
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(categoryListAsync);
        Utils.cancelAsyncTask(popularServiceAsync);
        Utils.cancelAsyncTask(subCategoryAsync);
        super.onDestroy();
    }
}
