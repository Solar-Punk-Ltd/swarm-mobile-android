package com.swarm.mobile.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.swarm.mobile.R;
import com.swarm.mobile.utils.StringUtils;

public class TruncatedTextView extends LinearLayout {

    private TextView textView;
    private ImageButton toggleButton;
    private String fullText = "";
    private boolean isExpanded = false;
    private int maxLength = 20;
    private Context context;

    public TruncatedTextView(Context context) {
        super(context);
        init(context);
    }

    public TruncatedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TruncatedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_truncated_text, this, true);

        textView = findViewById(R.id.truncatedText);
        toggleButton = findViewById(R.id.toggleButton);

        toggleButton.setOnClickListener(v -> copyToClipboard());

        updateDisplay();
    }

    public void setText(String text) {
        this.fullText = text != null ? text : "";
        this.isExpanded = false;
        updateDisplay();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        updateDisplay();
    }

    public String getText() {
        return fullText;
    }

    private void copyToClipboard() {
        if (fullText.isEmpty()) {
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", fullText);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void updateDisplay() {
        if (fullText.isEmpty()) {
            textView.setText("");
            toggleButton.setVisibility(GONE);
            return;
        }

        if (fullText.length() <= maxLength) {
            textView.setText(fullText);
            toggleButton.setVisibility(GONE);
        } else {
            String truncated = StringUtils.ellipsizeMiddle(fullText, maxLength);
            textView.setText(truncated);
            toggleButton.setVisibility(VISIBLE);
        }
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        updateDisplay();
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
