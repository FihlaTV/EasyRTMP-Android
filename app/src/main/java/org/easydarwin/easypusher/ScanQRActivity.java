package org.easydarwin.easypusher;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.zxing.Result;

public class ScanQRActivity extends AppCompatActivity  {

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanQRActivity.this, result.getText(), Toast.LENGTH_SHORT).show();

                        String orignalText = result.getText();
                        if (!TextUtils.isEmpty(orignalText)){
                            final String text = orignalText.trim();
                            if (text.toLowerCase().startsWith("rtmp://")) {
                                new AlertDialog
                                        .Builder(ScanQRActivity.this)
                                        .setTitle("Scanned the push address")
                                        .setMessage(text)
                                        .setPositiveButton("Use this address to push", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent();
                                                intent.putExtra("text", text);
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }
                                        }).setNegativeButton("Rescan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mCodeScanner.startPreview();
                                            }
                                        }).show();
                            }
                        }
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void onClose(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
