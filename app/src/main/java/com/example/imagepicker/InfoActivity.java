package com.example.imagepicker;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String title = getIntent().getStringExtra("title");
        String file = getIntent().getStringExtra("file");

        getSupportActionBar().setTitle(title);

        WebView webView = findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/" + file);
    }
}
