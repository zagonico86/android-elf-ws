/*
Copyright (c) 2021 Nicola Zago

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.zagonico.elfwstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.zagonico.elfws.ElfWsCallback;
import com.zagonico.elfws.ElfWsResponse;
import com.zagonico.elfws.auth.ElfAuthBasic;
import com.zagonico.elfws.auth.ElfOAuth2;
import com.zagonico.elfws.auth.ElfWsAuth;

public class MainActivity extends AppCompatActivity {
    private static final int OPEN_ATTACHMENT_FOR_UPLOAD_1 = 1001;
    private static final int OPEN_ATTACHMENT_FOR_UPLOAD_2 = 1002;
    private ElfWsTest elfWsTest;
    private ElfWsAuth auth;
    private Uri uri1, uri2;

    private Spinner spinnerAuth;
    private GenericSpinnerAdapter adapterAuth;
    private GenericPair[] pairsAuth;
    private GenericPair selectedAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerAuth = findViewById(R.id.auth_spinner);

        pairsAuth = new GenericPair[4];
        pairsAuth[0] = new GenericPair("none", "None");
        pairsAuth[1] = new GenericPair("basic", "Basic");
        pairsAuth[2] = new GenericPair("custom", "Custom");
        pairsAuth[3] = new GenericPair("oauth2", "OAuth2");

        adapterAuth = new GenericSpinnerAdapter( this,
                R.layout.my_spinner_item,
                pairsAuth);
        spinnerAuth.setAdapter(adapterAuth);
        spinnerAuth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                selectedAuth = adapterAuth.getItem(position);

                if (elfWsTest==null) return;

                switch (selectedAuth.id) {
                    case "none":
                        auth = null;
                        elfWsTest.removeAuth();
                        break;
                    case "basic":
                        auth = new ElfAuthBasic("elfws", "test");
                        break;
                    case "custom":
                        auth = new MyCustomAuth("Test");
                        break;
                    case "oauth2":
                        auth = new ElfOAuth2("","","",null);
                        break;
                }
                elfWsTest.setAuth(auth);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });

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