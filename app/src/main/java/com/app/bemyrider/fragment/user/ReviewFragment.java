package com.app.bemyrider.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.DetailReviewAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderServiceReviewDataItem;

import java.util.ArrayList;

public class ReviewFragment extends Fragment {

    private RecyclerView rv_reviews;
    private TextView txt_no_data_reviews;
    private ArrayList<ProviderServiceReviewDataItem> arrayList;
    private DetailReviewAdapter adapter;

    public ReviewFragment(ArrayList<ProviderServiceReviewDataItem> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        rv_reviews = view.findViewById(R.id.rv_reviews);
        txt_no_data_reviews = view.findViewById(R.id.txt_no_data_reviews);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rv_reviews.setLayoutManager(mLayoutManager);
        rv_reviews.setItemAnimator(new DefaultItemAnimator());
        adapter = new DetailReviewAdapter(getActivity(), arrayList);
        rv_reviews.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        if (arrayList != null && arrayList.size() > 0) {
            txt_no_data_reviews.setVisibility(View.GONE);
        } else {
            txt_no_data_reviews.setVisibility(View.VISIBLE);
        }

        return view;
    }

}
