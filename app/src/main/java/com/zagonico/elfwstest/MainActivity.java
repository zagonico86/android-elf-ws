package com.zagonico.elfwstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zagonico.elfws.ElfWsCallback;
import com.zagonico.elfws.ElfWsResponse;

public class MainActivity extends AppCompatActivity {
    private static final int OPEN_ATTACHMENT_FOR_UPLOAD_1 = 1001;
    private static final int OPEN_ATTACHMENT_FOR_UPLOAD_2 = 1002;
    private ElfWsTest elfWsTest;
    private Uri uri1, uri2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void initWsElf(View view) {
        EditText editText = findViewById(R.id.editTextUrl);

        String url = editText.getText().toString();
        if ("".equals(url)) {
            Toast.makeText(this, "Insert a valid URL to init ElfWsTest", Toast.LENGTH_LONG).show();
        }
        else {
            elfWsTest = new ElfWsTest(url);
            elfWsTest.setDebugMode(true);

            final MainActivity local = this;
            elfWsTest.setCallback(response -> {
                if (response.getType() == ElfWsResponse.TYPE_JSON) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            try {
                                Toast.makeText(local, response.getJsonObject().getString("message"), Toast.LENGTH_LONG).show();
                            }
                            catch (Exception e) {
                                Toast.makeText(local, "error in JSON response: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(local, response.getFilename()+": "+response.getMime(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            Toast.makeText(this, "Init done", Toast.LENGTH_LONG).show();
        }
    }

    public void actionGet(View view) {
        if (elfWsTest==null) {
            Toast.makeText(this, "Init ElfWsTest class first", Toast.LENGTH_LONG).show();
            return;
        }

        EditText editText = findViewById(R.id.editTextGet);
        String param1 = editText.getText().toString();

        SwitchCompat switchCompat = findViewById(R.id.switchGet);

        if (switchCompat.isChecked())
            elfWsTest.configureActionGetFile(param1);
        else
            elfWsTest.configureActionGetJson(param1);

        elfWsTest.executeRequest();
    }

    public void actionPost(View view) {
        if (elfWsTest==null) {
            Toast.makeText(this, "Init ElfWsTest class first", Toast.LENGTH_LONG).show();
            return;
        }

        EditText editText = findViewById(R.id.editTextPost);
        String param1 = editText.getText().toString();

        SwitchCompat switchCompat = findViewById(R.id.switchPost);

        if (switchCompat.isChecked())
            elfWsTest.configureActionPostFile(param1);
        else
            elfWsTest.configureActionPostJson(param1);

        elfWsTest.executeRequest();
    }

    public void addAttachment1(View view) {
        if (elfWsTest==null) {
            Toast.makeText(this, "Init WS elf class first", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_ATTACHMENT_FOR_UPLOAD_1);
    }

    public void addAttachment2(View view) {
        if (elfWsTest==null) {
            Toast.makeText(this, "Init WS elf class first", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_ATTACHMENT_FOR_UPLOAD_2);
    }

    public void actionFile(View view) {
        if (elfWsTest==null) {
            Toast.makeText(this, "Init ElfWsTest class first", Toast.LENGTH_LONG).show();
            return;
        }

        EditText editText = findViewById(R.id.editTextFileGet);
        String paramGet = editText.getText().toString();

        editText = findViewById(R.id.editTextFilePost);
        String paramPost = editText.getText().toString();

        SwitchCompat switchCompat = findViewById(R.id.switchFile);
        final boolean checked = switchCompat.isChecked();

        if (checked)
            elfWsTest.configureActionFileFile(this, paramGet, paramPost, uri1, uri2);
        else
            elfWsTest.configureActionFileJson(this, paramGet, paramPost, uri1, uri2);

        elfWsTest.executeRequest();
    }

    @Override
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        final MainActivity loc = this;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_ATTACHMENT_FOR_UPLOAD_1) {
                uri1 = data.getData();
            }
            else if (requestCode == OPEN_ATTACHMENT_FOR_UPLOAD_2) {
                uri2 = data.getData();
            }
        }
    }

}