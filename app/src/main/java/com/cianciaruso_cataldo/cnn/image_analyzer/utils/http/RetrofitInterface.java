package com.cianciaruso_cataldo.cnn.image_analyzer.utils.http;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    @Multipart
    @POST("/")
    Call<String> uploadImage(@Part MultipartBody.Part image);


}
