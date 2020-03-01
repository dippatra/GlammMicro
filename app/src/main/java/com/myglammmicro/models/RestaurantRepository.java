package com.myglammmicro.models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myglammmicro.interfaces.Apis;
import com.myglammmicro.utils.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository extends ViewModel {
    private static final String TAG=RestaurantRepository.class.getSimpleName();
    private static MutableLiveData<List<Restaurants>>restaurantList;
     public LiveData<List<Restaurants>> getRestaurantsByLocation(String entityType,String query,int count,double lat,double lon){
         try {
             if(restaurantList==null){
                 restaurantList=new MutableLiveData<List<Restaurants>>();
                 fetchRestaurantByLocation(entityType,query,count,lat,lon);
             }
         }catch (Exception ex){
             Log.e(TAG,ex.getMessage());
         }
         return restaurantList;
     }
     private void fetchRestaurantByLocation(String entityType,String query,int count,double lat,double lon){
         try {
             Apis apiService = RetrofitClient.getClient().create(Apis.class);

             Call<String> call = apiService.getRestaurantsByLocation(entityType,query,count,lat,lon);

             call.enqueue(new Callback<String>() {
                 @Override
                 public void onResponse(Call<String> call, Response<String> response) {
                     try{
                         if(response.isSuccessful()){
                             restaurantList.postValue(getRestaurantList(response.body()));
                         }


                     }catch (Exception ex){
                         Log.e(TAG,ex.getMessage());
                     }
                 }

                 @Override
                 public void onFailure(Call<String> call, Throwable t) {
                     try{

                     }catch (Exception ex){
                         Log.e(TAG,ex.getMessage());
                     }
                 }
             });

         }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
         }
     }
     public void initializeData(){
         restaurantList=null;
     }

    public LiveData<List<Restaurants>> getRestaurantsByArea(String entityType,String query,int count){
        try {
            if(restaurantList==null){
                restaurantList=new MutableLiveData<List<Restaurants>>();
                fetchRestaurantByArea(entityType,query,count);
            }
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
        return restaurantList;
    }
    private void fetchRestaurantByArea(String entityType,String query,int count){
        try {
            Apis apiService = RetrofitClient.getClient().create(Apis.class);
            Call<String> call = apiService.getRestaurantsByAreaName(entityType,query,count);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        if(response.isSuccessful()){
                            restaurantList.postValue(getRestaurantList(response.body()));
                        }

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    try{

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private List<Restaurants>getRestaurantList(String serverResponse){
        JSONObject object,resObject;
        JSONArray resArray;
        List<Restaurants>resList=null;
        Restaurants restaurants;

         try{
             if (!serverResponse.isEmpty()){
                 object=new JSONObject(serverResponse);
                 if(object.has("restaurants")){
                     resList=new ArrayList<>();
                     resArray=object.getJSONArray("restaurants");
                     for (int i=0;i<resArray.length();i++){
                         resObject=resArray.getJSONObject(i);
                         restaurants=new Restaurants(resObject.getJSONObject("restaurant").getString("id"),resObject.getJSONObject("restaurant").getString("name"),resObject.getJSONObject("restaurant").getString("photos_url"),resObject.getJSONObject("restaurant").getJSONObject("user_rating").getString("aggregate_rating")
                         ,resObject.getJSONObject("restaurant").getJSONObject("location").getString("locality"),resObject.getJSONObject("restaurant").getJSONObject("location").getString("address"),resObject.getJSONObject("restaurant").getString("phone_numbers"),resObject.getJSONObject("restaurant").getString("cuisines"),
                                 resObject.getJSONObject("restaurant").getJSONObject("user_rating").getString("rating_color"));
                         resList.add(restaurants);
                     }
                 }


             }

         }catch (Exception ex){
             Log.e(TAG,ex.getMessage());
         }
         return  resList;
    }
}
