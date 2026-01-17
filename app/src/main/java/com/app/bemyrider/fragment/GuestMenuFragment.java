package com.app.bemyrider.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.databinding.FragmentGuestMenuBinding;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

public class GuestMenuFragment extends Fragment implements MenuItemClickListener {

    FragmentGuestMenuBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private ConnectionManager connectionManager;
    private ModelForDrawer[] drawerItem;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_menu, container, false);

        initView();

        return binding.getRoot();
    }

    private void initView() {
        activity = (AppCompatActivity) getActivity();
        context = getContext();

        activity.setSupportActionBar(binding.toolbar);

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        // Coil Migration from Picasso
        ImageRequest request = new ImageRequest.Builder(context)
            .data(R.drawable.ic_user_menu)
            .placeholder(R.drawable.loading)
            .target(binding.imgProfile)
            .build();
        Coil.imageLoader(context).enqueue(request);

        binding.txtUserName.setText(getResources().getString(R.string.guest_user));

        setUpMenuItems();
    }

    void setUpMenuItems() {
        drawerItem = new ModelForDrawer[3];
        drawerItem[0] = new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us));
        drawerItem[1] = new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info));
        drawerItem[2] = new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.login_menu));

        binding.recGuestMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recGuestMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context,activity, this,drawerItem);
        binding.recGuestMenu.setAdapter(adapter);
    }


    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(activity, ContactUsActivity.class));
                break;
            case 1:
                startActivity(new Intent(activity, InfoPageActivity.class));
                break;
            case 2:
                startActivity(new Intent(activity, LoginActivity.class));
                //activity.finish();
                break;
            default:
                break;
        }
    }

}
