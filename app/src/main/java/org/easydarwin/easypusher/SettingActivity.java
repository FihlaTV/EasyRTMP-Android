/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package org.easydarwin.easypusher;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.easydarwin.easypusher.databinding.ActivitySettingBinding;
import org.easydarwin.easypusher.util.Config;
import org.easydarwin.easypusher.util.SPUtil;

/**
 * 设置页
 * */
public class SettingActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    public static final int REQUEST_OVERLAY_PERMISSION = 1004;  // 悬浮框
    private static final int REQUEST_SCAN_TEXT_URL = 1003;      // 扫描二维码

    EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.mainToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.mainToolbar.setNavigationIcon(R.drawable.com_back);

        url = (EditText) findViewById(R.id.push_url);
        url.setText(Config.getServerURL(this));

        // 使能摄像头后台采集
        CheckBox backgroundPushing = (CheckBox) findViewById(R.id.enable_background_camera_pushing);
        backgroundPushing.setChecked(SPUtil.getEnableBackgroundCamera(this));
        backgroundPushing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(SettingActivity.this)) {
                            SPUtil.setEnableBackgroundCamera(SettingActivity.this, true);
                        } else {
                            new AlertDialog
                                    .Builder(SettingActivity.this)
                                    .setTitle("Upload video in background")
                                    .setMessage("Uploading videos in the background requires the APP to appear at the top. Are you sure?")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // 在Android 6.0后，Android需要动态获取权限，若没有权限，提示获取.
                                            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                                        }
                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            SPUtil.setEnableBackgroundCamera(SettingActivity.this, false);
                                            buttonView.toggle();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    } else {
                        SPUtil.setEnableBackgroundCamera(SettingActivity.this, true);
                    }
                } else {
                    SPUtil.setEnableBackgroundCamera(SettingActivity.this, false);
                }
            }
        });

        // 是否使用软编码
        CheckBox x264enc = findViewById(R.id.use_x264_encode);
        x264enc.setChecked(SPUtil.getswCodec(this));
        x264enc.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setswCodec(this, isChecked)
        );

        // 使能H.265编码
        CheckBox enable_hevc_cb = findViewById(R.id.enable_hevc);
        enable_hevc_cb.setChecked(SPUtil.getHevcCodec(this));
        enable_hevc_cb.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setHevcCodec(this, isChecked)
        );

        // 叠加水印
        CheckBox enable_video_overlay = findViewById(R.id.enable_video_overlay);
        enable_video_overlay.setChecked(SPUtil.getEnableVideoOverlay(this));
        enable_video_overlay.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setEnableVideoOverlay(this, isChecked)
        );

        // 推送内容
        RadioGroup push_content = findViewById(R.id.push_content);

        boolean videoEnable = SPUtil.getEnableVideo(this);
        if (videoEnable) {
            boolean audioEnable = SPUtil.getEnableAudio(this);

            if (audioEnable) {
                RadioButton push_av = findViewById(R.id.push_av);
                push_av.setChecked(true);
            } else {
                RadioButton push_v = findViewById(R.id.push_v);
                push_v.setChecked(true);
            }
        } else {
            RadioButton push_a = findViewById(R.id.push_a);
            push_a.setChecked(true);
        }

        push_content.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.push_av) {
                    SPUtil.setEnableVideo(SettingActivity.this, true);
                    SPUtil.setEnableAudio(SettingActivity.this, true);
                } else if (checkedId == R.id.push_a) {
                    SPUtil.setEnableVideo(SettingActivity.this, false);
                    SPUtil.setEnableAudio(SettingActivity.this, true);
                } else if (checkedId == R.id.push_v) {
                    SPUtil.setEnableVideo(SettingActivity.this, true);
                    SPUtil.setEnableAudio(SettingActivity.this, false);
                }
            }
        });

        SeekBar sb = findViewById(R.id.bitrate_seekbar);
        final TextView bitrateValue = findViewById(R.id.bitrate_value);

        int bitrate_added_kbps = SPUtil.getBitrateKbps(this);
        int kbps = 72000 + bitrate_added_kbps;
        bitrateValue.setText(kbps/1000 + "kbps");

        sb.setMax(5000000);
        sb.setProgress(bitrate_added_kbps);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int kbps = 72000 + progress;
                bitrateValue.setText(kbps/1000 + "kbps");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SPUtil.setBitrateKbps(SettingActivity.this, seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        String text = url.getText().toString().trim();
        if (text.toLowerCase().startsWith("rtmp://")) {
            Config.setServerURL(SettingActivity.this, text);
        }
    }

    /*
    * 二维码扫码
    * */
    public void onScanQRCode(View view) {
        Intent intent = new Intent(this, ScanQRActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_TEXT_URL);
        overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
    }

    /*
    * 本地录像
    * */
    public void onOpenLocalRecord(View view) {
        Intent intent = new Intent(this, MediaFilesActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    /*
    * 屏幕分辨率
    * */
    public void onScreenPushResolution(View view) {
        int defaultIdx = SPUtil.getScreenPushingResIndex(this);
        new AlertDialog.Builder(this).setTitle("Stream screen resolution").setSingleChoiceItems(
                new CharSequence[]{"1 times the screen size", "0.75 times the screen size", "0.5 times the screen size", "0.3 times the screen size", "0.25 times the screen size", "0.2 times the screen size"}, defaultIdx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SPUtil.setScreenPushingResIndex(SettingActivity.this, which);

                        Toast.makeText(SettingActivity.this,"Configuration changes will take effect the next time the screen is pushed", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean canDraw = Settings.canDrawOverlays(this);
                SPUtil.setEnableBackgroundCamera(SettingActivity.this, canDraw);

                if (!canDraw) {
                    CheckBox backgroundPushing = (CheckBox) findViewById(R.id.enable_background_camera_pushing);
                    backgroundPushing.setChecked(false);
                }
            }
        } else if (requestCode == REQUEST_SCAN_TEXT_URL) {
            if (resultCode == RESULT_OK) {
                String url = data.getStringExtra("text");
                this.url.setText(url);

                Config.setServerURL(SettingActivity.this, url);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // 返回的功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
