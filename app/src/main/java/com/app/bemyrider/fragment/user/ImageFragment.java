package com.app.bemyrider.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.DetailImagesAdapter;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.R;

public class ImageFragment extends Fragment {

    private RecyclerView recyclerView_images;
    private ProviderServiceDetailsItem serviceDetailData;
    private GridLayoutManager staggeredGridLayoutManager;
    private TextView txt_no_data_images;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_images, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        recyclerView_images = view.findViewById(R.id.recycler_view);
        txt_no_data_images = view.findViewById(R.id.txt_no_data_images);

        recyclerView_images.setHasFixedSize(true);

        staggeredGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView_images.setLayoutManager(staggeredGridLayoutManager);

        if(serviceDetailData != null){
            if (serviceDetailData.getMediaData() != null && serviceDetailData.getMediaData().size() > 0) {
                DetailImagesAdapter rcAdapter = new DetailImagesAdapter(getContext(), serviceDetailData.getMediaData());
                recyclerView_images.setAdapter(rcAdapter);
            } else {
                txt_no_data_images.setVisibility(View.VISIBLE);
                recyclerView_images.setVisibility(View.GONE);
            }
        }

        return view;

    }
}
