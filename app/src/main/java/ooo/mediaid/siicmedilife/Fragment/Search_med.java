package ooo.mediaid.siicmedilife.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ooo.mediaid.siicmedilife.R;


/**
 * Created by ankit on 3/12/2018.
 */

public class Search_med extends Fragment {

    View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.fragment_test, container, false);



        return  root;
    }
}
