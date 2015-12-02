package com.example.saturn.imagefilter;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sileria.android.view.HorzListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {


    public VideoFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video,container,false);

        // listview Item la anh
        HorzListView listviewImg = (HorzListView) v.findViewById(R.id.horizontal_lv);
        int[] arrImg = { R.drawable.effect_black, R.drawable.effect_boost_1, R.drawable.effect_boost_2,
                R.drawable.effect_boost_3, R.drawable.effect_brightness, R.drawable.effect_brightness,
                R.drawable.effect_color_red,
                R.drawable.effect_color_green,
                R.drawable.effect_color_blue,
                R.drawable.effect_color_depth_64,
                R.drawable.effect_color_depth_32};
        ListFilterAdapter adapterImg = new ListFilterAdapter(
                getActivity(), arrImg);
        listviewImg.setAdapter(adapterImg);
        //listviewImg.setOnItemClickListener(onFilterClickListener);

        FloatingActionButton btTake = (FloatingActionButton)v.findViewById(R.id.fab_take);
        //btTake.setOnClickListener(onTakeClick);

        FloatingActionButton btSave = (FloatingActionButton)v.findViewById(R.id.fab_save);
        //btSave.setOnClickListener(onSaveClick);

        FloatingActionButton btAdd = (FloatingActionButton)v.findViewById(R.id.fab_add);
        //btAdd.setOnClickListener(onAddClick);
        return v;
    }

}
