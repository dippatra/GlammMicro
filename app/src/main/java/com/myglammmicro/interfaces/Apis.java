package com.myglammmicro.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface Apis {
    @Headers("user-key: b227c258628ffd3a10d1b96189d854ad")
    @GET("api/v2.1/search")
    Call<String> getRestaurantsByLocation(@Query("entity_type") String entity_type, @Query("q") String query, @Query("count") int count, @Query("lat") double lat, @Query("lon") double lon);

    @Headers("user-key: b227c258628ffd3a10d1b96189d854ad")
    @GET("api/v2.1/search")
    Call<String> getRestaurantsByAreaName(@Query("entity_type") String entity_type, @Query("q") String query, @Query("count") int count);

    @Headers("user-key: b227c258628ffd3a10d1b96189d854ad")
    @GET("api/v2.1/geocode")
    Call<String> getLocationDetail(@Query("lat") double lat, @Query("lon") double lon);
}
