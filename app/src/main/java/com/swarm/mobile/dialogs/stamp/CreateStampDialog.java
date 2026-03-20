package com.swarm.mobile.dialogs.stamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.mobile.R;
import com.swarm.mobile.interfaces.OnStampCreateListener;
import com.swarm.mobile.utils.SwarmPostageStampUtils;

import java.math.BigInteger;

public class CreateStampDialog {

    public static final int MIN_DEPTH = 17;
    public static final int MAX_DEPTH = 33;
    public static final long SECONDS_PER_DAY = 86_400L;
    public static final long SECONDS_PER_WEEK = 7 * SECONDS_PER_DAY;
    public static final long SECONDS_PER_MONTH = 30 * SECONDS_PER_DAY;

    public static void show(Context context, BigInteger currentNetworkPrice, OnStampCreateListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_stamp, null);

        TextView depthDisplay = dialogView.findViewById(R.id.depthInput);
        MaterialButton depthDecrease = dialogView.findViewById(R.id.depthDecreaseButton);
        MaterialButton depthIncrease = dialogView.findViewById(R.id.depthIncreaseButton);

        TextView ttlValueDisplay = dialogView.findViewById(R.id.ttlValueDisplay);
        MaterialButton ttlDecrease = dialogView.findViewById(R.id.ttlDecreaseButton);
        MaterialButton ttlIncrease = dialogView.findViewById(R.id.ttlIncreaseButton);
        MaterialButtonToggleGroup ttlUnitToggle = dialogView.findViewById(R.id.ttlUnitToggle);

        TextInputEditText labelInput = dialogView.findViewById(R.id.labelInput);
        MaterialCheckBox immutableCheckbox = dialogView.findViewById(R.id.immutableCheckbox);

        View capacityPreviewCard = dialogView.findViewById(R.id.capacityPreviewCard);
        TextView capacityPreviewText = dialogView.findViewById(R.id.capacityPreviewText);
        View indicativePriceRow = dialogView.findViewById(R.id.indicativePriceRow);
        TextView indicativePriceText = dialogView.findViewById(R.id.indicativePriceText);

        StampDialogState state = new StampDialogState();

        Runnable refresh = () -> {
            depthDisplay.setText(String.valueOf(state.getDepth()));
            depthDecrease.setEnabled(state.getDepth() > MIN_DEPTH);
            depthIncrease.setEnabled(state.getDepth() < MAX_DEPTH);
            capacityPreviewText.setText(SwarmPostageStampUtils.formatCapacitySummary(state.getDepth()));
            capacityPreviewCard.setVisibility(View.VISIBLE);

            ttlValueDisplay.setText(String.valueOf(state.getTtlValue()));
            ttlDecrease.setEnabled(state.getTtlValue() > state.getTtlMin());

            long amount = state.computeAmount(currentNetworkPrice);

            if (amount > 0) {
                indicativePriceText.setText(
                        SwarmPostageStampUtils.formatIndicativePrice(amount, state.getDepth()));
                indicativePriceRow.setVisibility(View.VISIBLE);
            } else {
                indicativePriceRow.setVisibility(View.GONE);
            }
        };

        depthDecrease.setOnClickListener(v -> {
            if (state.getDepth() > MIN_DEPTH) {
                state.decrementDepth();
                refresh.run();
            }
        });
        depthIncrease.setOnClickListener(v -> {
            if (state.getDepth() < MAX_DEPTH) {
                state.incrementDepth();
                refresh.run();
            }
        });

        ttlDecrease.setOnClickListener(v -> {
            if (state.getTtlValue() > state.getTtlMin()) {
                state.decrementTtlValue();
                refresh.run();
            }
        });
        ttlIncrease.setOnClickListener(v -> {
            state.incrementTtlValue();
            refresh.run();
        });

        // Unit toggle
        ttlUnitToggle.check(R.id.unitDays);
        ttlUnitToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.unitDays) {
                state.setTtlUnit(SECONDS_PER_DAY);
                state.setTtlMin(2);
            } else if (checkedId == R.id.unitWeeks) {
                state.setTtlUnit(SECONDS_PER_WEEK);
                state.setTtlMin(1);
            } else {
                state.setTtlUnit(SECONDS_PER_MONTH);
                state.setTtlMin(1);
            }
            state.clampTtlValue();
            refresh.run();
        });

        refresh.run();

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.createButton).setOnClickListener(v -> {
            long amount = state.computeAmount(currentNetworkPrice);
            if (amount <= 0) {
                Toast.makeText(context, "Invalid duration", Toast.LENGTH_SHORT).show();
                return;
            }
            String label = labelInput.getText() != null ? labelInput.getText().toString() : "";
            boolean immutable = immutableCheckbox.isChecked();
            listener.onCreate(String.valueOf(amount), String.valueOf(state.getDepth()), label, immutable);
            dialog.dismiss();
        });

        dialog.show();
    }
}

