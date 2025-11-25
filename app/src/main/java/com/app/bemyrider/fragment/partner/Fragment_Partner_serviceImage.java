package com.app.bemyrider.fragment.partner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.SolventRecyclerViewAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nct121 on 12/12/16.
 */

public class Fragment_Partner_serviceImage extends Fragment {
    private RecyclerView recyclerView_images;
    private GridLayoutManager gaggeredGridLayoutManager;
    private TextView txt_no_data_partner_images;
    private List<ProviderServiceMediaDataItem> imageslist = new ArrayList<>();
    private ProviderServiceDetailsItem serviceDetailData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.partner_images_fragment, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        imageslist.clear();
        recyclerView_images = view.findViewById(R.id.recycler_view_grid);
        txt_no_data_partner_images = view.findViewById(R.id.txt_no_data_partner_images);
        recyclerView_images.setHasFixedSize(true);

        gaggeredGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView_images.setLayoutManager(gaggeredGridLayoutManager);

        imageslist.addAll(serviceDetailData.getMediaData());

        if (imageslist.size() > 0) {
            txt_no_data_partner_images.setVisibility(View.GONE);
            recyclerView_images.setVisibility(View.VISIBLE);
        } else {
            recyclerView_images.setVisibility(View.GONE);
            txt_no_data_partner_images.setVisibility(View.VISIBLE);
        }

        SolventRecyclerViewAdapter rcAdapter = new SolventRecyclerViewAdapter(getActivity(), imageslist);
        recyclerView_images.setAdapter(rcAdapter);

        return view;

    }
}
