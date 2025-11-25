package com.app.bemyrider.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.R;

/**
 * Created by nct33 on 13/9/17.
 */

public class ChangePassFragment extends Fragment {

    //no need

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_pass_fragment,container,false);
        return view;
    }
}
