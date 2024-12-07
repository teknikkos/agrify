package com.app.agrify;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("create.php")
    Call<ResponseBody>
    createField(
            @Field("field_name") String fieldName,
            @Field("soil_type_id") int soilTypeId,
            @Field("crop_id") int cropId,
            @Field("date_planted") String datePlanted,
            @Field("field_latitude") double fieldLatitude,
            @Field("field_longtitude") double fieldLongtitude
    );

    @GET("read.php") Call<List<Field>> getFields();
}


