package com.example.travenor;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private static final String ARG_POSITION = "position";

    public static OnboardingFragment newInstance(int position) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        int position = getArguments().getInt(ARG_POSITION);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        ImageView imageView = view.findViewById(R.id.onboarding_image);

        switch (position) {
            case 0:
                title.setText(R.string.onboarding_text1);
                description.setText(R.string.onboarding1);
                imageView.setImageResource(R.drawable.img1);
                break;
            case 1:
                title.setText(R.string.onboarding_text2);
                description.setText(R.string.onboarding2);
                imageView.setImageResource(R.drawable.img2);
                break;
            case 2:
                title.setText(R.string.onboarding_text3);
                description.setText(R.string.onboarding3);
                imageView.setImageResource(R.drawable.img3);
                break;
        }

        return view;
    }
}