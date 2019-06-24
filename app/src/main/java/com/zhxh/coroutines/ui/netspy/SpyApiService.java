package com.zhxh.coroutines.ui.netspy;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

class SpyApiService {

    static HttpbinApi getInstance(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
        return retrofit.create(HttpbinApi.class);
    }

    static class Data {
        final String thing;

        Data(String thing) {
            this.thing = thing;
        }
    }

    interface HttpbinApi {
        @GET("zhxh/list")
        Call<Void> get();

        @POST("zhxh/list")
        Call<Void> post(@Body Data body);

        @PATCH("zhxh/array")
        Call<Void> patch(@Body Data body);

        @PUT("zhxh/list")
        Call<Void> put(@Body Data body);

        @DELETE("zhxh/array")
        Call<Void> delete();
    }
}