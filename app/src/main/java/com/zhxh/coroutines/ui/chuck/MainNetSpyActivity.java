package com.zhxh.coroutines.ui.chuck;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.creditease.netspy.NetSpy;
import com.creditease.netspy.NetSpyInterceptor;

import com.zhxh.coroutines.R;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainNetSpyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chuck);
        findViewById(R.id.do_http).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHttpActivity();
            }
        });
        findViewById(R.id.launch_chuck_directly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchChuckDirectly();
            }
        });
    }

    private OkHttpClient getClient(Context context) {
        return new OkHttpClient.Builder()
                // Add a NetSpyInterceptor instance to your OkHttp client
                .addInterceptor(new NetSpyInterceptor(context))
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    private void launchChuckDirectly() {
        // Optionally launch NetSpy directly from your own app UI
        startActivity(NetSpy.getLaunchIntent(this));
    }

    private void doHttpActivity() {
        SampleApiService.HttpbinApi api = SampleApiService.getInstance(getClient(this));
        Callback<Void> cb = new Callback<Void>() {
            @Override
            public void onResponse(Call call, Response response) {
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
            }
        };
        api.get().enqueue(cb);
        api.post(new SampleApiService.Data("posted")).enqueue(cb);
        api.patch(new SampleApiService.Data("patched")).enqueue(cb);
        api.put(new SampleApiService.Data("put")).enqueue(cb);
        api.delete().enqueue(cb);
        api.status(201).enqueue(cb);
        api.status(401).enqueue(cb);
        api.status(500).enqueue(cb);
        api.delay(9).enqueue(cb);
        api.delay(15).enqueue(cb);
        api.redirectTo("https://http2.akamai.com").enqueue(cb);
        api.redirect(3).enqueue(cb);
        api.redirectRelative(2).enqueue(cb);
        api.redirectAbsolute(4).enqueue(cb);
        api.stream(500).enqueue(cb);
        api.streamBytes(2048).enqueue(cb);
        api.image("image/png").enqueue(cb);
        api.gzip().enqueue(cb);
        api.xml().enqueue(cb);
        api.utf8().enqueue(cb);
        api.deflate().enqueue(cb);
        api.cookieSet("v").enqueue(cb);
        api.basicAuth("me", "pass").enqueue(cb);
        api.drip(512, 5, 1, 200).enqueue(cb);
        api.deny().enqueue(cb);
        api.cache("Mon").enqueue(cb);
        api.cache(30).enqueue(cb);
    }
}