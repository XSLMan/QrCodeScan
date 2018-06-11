package com.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018-06-11.
 */

public interface RequestByPost {
    @FormUrlEncoded
    @POST("seat.php")
    Call<String> updateUser(@Field("email") String first);
}
