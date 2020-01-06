package com.icebem.akt.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.AppUtil;
import com.icebem.akt.util.IOUtil;

import org.json.JSONObject;

public class AboutActivity extends AppCompatActivity {
    private int i;
    private TextView typeDesc;
    private LinearLayout versionContainer;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.fab).setOnClickListener(this::onClick);
        initView();
    }

    private void initView() {
        findViewById(R.id.container_comment).setOnClickListener(this::onClick);
        findViewById(R.id.container_discuss).setOnClickListener(this::onClick);
        findViewById(R.id.container_project).setOnClickListener(this::onClick);
        versionContainer = findViewById(R.id.container_version_state);
        versionContainer.setOnClickListener(this::onClick);
        findViewById(R.id.container_version_type).setOnClickListener(this::onClick);
        ((TextView) findViewById(R.id.txt_version_state_desc)).setText(BuildConfig.VERSION_NAME);
        typeDesc = findViewById(R.id.txt_version_type_desc);
        manager = new PreferenceManager(this);
        typeDesc.setText(manager.isPro() ? R.string.version_type_pro : R.string.version_type_lite);
    }

    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.action_donate);
                builder.setMessage(R.string.msg_donate);
                builder.setNeutralButton(R.string.action_donate_alipay, (dialog, which) -> {
                    try {
                        startActivity(Intent.parseUri(AppUtil.URL_ALIPAY_API, Intent.URI_INTENT_SCHEME));
                        Snackbar.make(view, R.string.info_donate_thanks, Snackbar.LENGTH_INDEFINITE).show();
                    } catch (Exception e) {
                        Snackbar.make(view, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
                    }
                });
                builder.setPositiveButton(R.string.no_way, null);
                builder.setNegativeButton(R.string.no_thanks, null);
                builder.create().show();
                break;
            case R.id.container_comment:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())).setPackage(AppUtil.PACKAGE_COOLAPK);
                if (intent.resolveActivity(getPackageManager()) == null)
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_COOLAPK));
                startActivity(intent);
                break;
            case R.id.container_discuss:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_QQ_API)));
                } catch (Exception e) {
                    Snackbar.make(view, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.container_project:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_PROJECT)));
                break;
            case R.id.container_version_state:
                versionContainer.setOnClickListener(null);
                new Thread(this::checkVersionUpdate, "update").start();
                Snackbar.make(view, R.string.version_checking, Snackbar.LENGTH_INDEFINITE).show();
                break;
            case R.id.container_version_type:
                if (i == 15) {
                    i = 0;
                    manager.setPro(!manager.isPro());
                    typeDesc.setText(manager.isPro() ? R.string.version_type_pro : R.string.version_type_lite);
                    Snackbar.make(view, R.string.version_type_changed, Snackbar.LENGTH_LONG).show();
                } else i++;
                break;
        }
    }

    private void checkVersionUpdate() {
        int id;
        String url = AppUtil.URL_RELEASE_LATEST;
        try {
            if (AppUtil.isLatestVersion()) {
                id = R.string.version_latest;
            } else {
                id = R.string.version_update;
                JSONObject json = new JSONObject(IOUtil.stream2String(IOUtil.fromWeb(AppUtil.URL_RELEASE_LATEST_API)));
                url = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            }
        } catch (Exception e) {
            id = R.string.version_checking_failed;
        }
        int result = id;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        runOnUiThread(() -> {
            if (result == R.string.version_update)
                Snackbar.make(typeDesc, result, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_update, v -> startActivity(intent)).show();
            else
                Snackbar.make(typeDesc, result, Snackbar.LENGTH_LONG).show();
            versionContainer.setOnClickListener(v -> startActivity(intent));
        });
    }
}