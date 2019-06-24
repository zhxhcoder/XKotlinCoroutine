package com.zhxh.coroutines.ui.netspy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.creditease.netspy.NetSpyHelper;
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
        setContentView(R.layout.activity_main_netspy);
        findViewById(R.id.do_http).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHttpActivity();
            }
        });
        findViewById(R.id.launch_netspy_directly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNetSpyDirectly();
            }
        });
    }

    private OkHttpClient getClient(Context context) {
        return new OkHttpClient.Builder()
                // Add a NetSpyInterceptor instance to your OkHttp client
                .addInterceptor(new NetSpyInterceptor(context, true))
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    private void launchNetSpyDirectly() {
        NetSpyHelper.launchActivity(this);
    }

    private void doHttpActivity() {
        SpyApiService.HttpbinApi api = SpyApiService.getInstance(getClient(this));
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
        api.post(new SpyApiService.Data("posted")).enqueue(cb);
        api.patch(new SpyApiService.Data("patched")).enqueue(cb);
        api.put(new SpyApiService.Data("put")).enqueue(cb);
        api.delete().enqueue(cb);

    }
}