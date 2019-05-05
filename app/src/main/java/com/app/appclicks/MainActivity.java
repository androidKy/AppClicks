package com.app.appclicks;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.appclicks.base.BaseAccessibilityService;
import com.app.appclicks.util.Constants;
import com.app.appclicks.wxservice.WxService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private PackageManager packageManager;
    private EditText etComment;
    private EditText etAttention;
    private Button btComment;
    private Button btAttention;
    private EditText etNewsCount;
    private Button btTencentNews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        packageManager = getPackageManager();

      /*  if (!isAccessibilitySettingsOn(this)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }*/

        etAttention = findViewById(R.id.et_attention);
        etComment = findViewById(R.id.et_comment);
        btAttention = findViewById(R.id.bt_attention);
        btComment = findViewById(R.id.bt_comment);
        etNewsCount = findViewById(R.id.et_newsCount);
        btTencentNews = findViewById(R.id.bt_tencentNews);

        initListener();
    }


    private void initListener() {
        btComment.setOnClickListener(this);
        btAttention.setOnClickListener(this);
        btTencentNews.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!BaseAccessibilityService.isAccessibilitySettingsOn(this, WxService.class.getCanonicalName())) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return;
        }
        switch (v.getId()) {
            case R.id.bt_attention:
                sendAttention();
                break;
            case R.id.bt_comment:
                sendComment();
                break;
            case R.id.bt_tencentNews:
                lookNews();
                break;
        }

    }

    private void lookNews() {
        String newsCount = etNewsCount.getText().toString();
        if (!TextUtils.isEmpty(newsCount) && Integer.valueOf(newsCount) <= 0) {
            Toast.makeText(this, "浏览次数必须大于1", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(Constants.Package.WE_CHAT);
        if (launchIntentForPackage != null) {
            startActivity(launchIntentForPackage);
        }
    }

    /**
     * 私信我的关注
     */
    private void sendAttention() {
        String msg = etAttention.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "请输入信息", Toast.LENGTH_SHORT).show();
            return;
        }
        BaseAccessibilityService.setAttentionMsg(msg);
        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(Constants.Package.TODAY_NEWS);
        if (launchIntentForPackage != null) {
            startActivity(launchIntentForPackage);
        }
    }

    /**
     * 私信评论用户
     */
    public void sendComment() {
        String msg = etComment.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "请输入信息", Toast.LENGTH_SHORT).show();
            return;
        }
        BaseAccessibilityService.setCommentMsg(msg);
        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(Constants.Package.TODAY_NEWS);
        if (launchIntentForPackage != null) {
            startActivity(launchIntentForPackage);
        }
    }


}
