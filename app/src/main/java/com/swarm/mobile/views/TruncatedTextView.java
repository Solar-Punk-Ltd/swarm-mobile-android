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

    private LinearLayout hashContainer;
    private TextView textView;
    private ImageButton copyButton;
    private String fullText = "";
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

        hashContainer = findViewById(R.id.hashContainer);
        textView = findViewById(R.id.truncatedText);
        copyButton = findViewById(R.id.copyButton);

        hashContainer.setOnClickListener(v -> copyHashToClipboard());
        copyButton.setOnClickListener(v -> copyHashToClipboard());

        updateDisplay();
    }

    public void setText(String text) {
        this.fullText = text != null ? text : "";
        updateDisplay();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        updateDisplay();
    }

    public String getText() {
        return fullText;
    }

    private void copyHashToClipboard() {
        if (fullText.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Swarm Hash", fullText);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.hash_copied, Toast.LENGTH_SHORT).show();
    }

    private void updateDisplay() {
        if (fullText.isEmpty()) {
            hashContainer.setBackground(null);
            hashContainer.setClickable(false);
            textView.setText("");
            copyButton.setVisibility(GONE);
            return;
        }

        hashContainer.setBackground(context.getDrawable(R.drawable.hash_border_background));
        hashContainer.setClickable(true);
        copyButton.setVisibility(VISIBLE);

        if (fullText.length() <= maxLength) {
            textView.setText(fullText);
        } else {
            textView.setText(StringUtils.ellipsizeMiddle(fullText, maxLength));
        }
    }
}
