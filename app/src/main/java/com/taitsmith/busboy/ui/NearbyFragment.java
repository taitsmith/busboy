package com.taitsmith.busboy.ui;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.viewmodels.NearbyViewModel;

public class NearbyFragment extends Fragment {

    NearbyViewModel nearbyViewModel;
    OnListItemLongListener listItemLongListener;
    OnListItemSelectedListener listItemListener;

    public interface OnListItemSelectedListener {
        void onNearbySelected(int position);
    }

    public interface OnListItemLongListener {
        void onNearbyLongSelected(int position);
    }

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nearby_fragment, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listItemListener = (OnListItemSelectedListener) context;
            listItemLongListener = (OnListItemLongListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "Must Implement OnListItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listItemListener = null;
        listItemLongListener = null;
    }
}