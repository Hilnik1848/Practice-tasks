package com.example.travenor;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private OnApplyFilterListener listener;
    private EditText countryInput;
    private RatingBar ratingBar;
    private EditText amenitiesInput;

    public interface OnApplyFilterListener {
        void onApplyFilter(String country, float rating, List<String> amenities);
        void onResetFilter();
    }

    public void setOnApplyFilterListener(OnApplyFilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);

        countryInput = view.findViewById(R.id.input_country);
        ratingBar = view.findViewById(R.id.rating_bar);
        amenitiesInput = view.findViewById(R.id.input_amenities);
        Button applyButton = view.findViewById(R.id.btn_apply);
        Button resetButton = view.findViewById(R.id.btn_reset);

        applyButton.setOnClickListener(v -> {
            String country = countryInput.getText().toString().trim();
            float rating = ratingBar.getRating();
            String[] amenitiesArray = amenitiesInput.getText().toString().trim().split(",");
            List<String> amenities = new ArrayList<>();
            for (String a : amenitiesArray) {
                if (!a.isEmpty()) amenities.add(a.trim().toLowerCase());
            }
            if (listener != null) {
                listener.onApplyFilter(country, rating, amenities);
            }
            dismiss();
        });

        resetButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResetFilter();
            }
            dismiss();
        });

        return view;
    }
}