package com.myglammmicro.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.myglammmicro.R;
import com.myglammmicro.models.Restaurants;
import com.myglammmicro.utils.CommonMethods;

import java.util.ArrayList;

public class RestaurantsListAdapter extends   RecyclerView.Adapter<RestaurantsListAdapter.MyViewHolder> {
    private static final String TAG=RestaurantsListAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<Restaurants>restaurants;
    public RestaurantsListAdapter(Context context, ArrayList<Restaurants>restaurants){
        this.context=context;
        this.restaurants=restaurants;
    }
    public void updateList(ArrayList<Restaurants>restaurants){
        this.restaurants=restaurants;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restaurant_list_item, viewGroup, false);
        return new MyViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Restaurants res;
        try{
            res=restaurants.get(position);
            holder.resName.setText(res.getResName());
            holder.resName.setSelected(true);
            CommonMethods.setFontBold(holder.resName);
            holder.locality.setText(res.getLocality());
            holder.locality.setSelected(true);
            CommonMethods.setFontMedium(holder.locality);
            holder.cuisines.setText(res.getCuisines());
            holder.cuisines.setSelected(true);
            CommonMethods.setFontMedium(holder.cuisines);
            holder.rating.setText(res.getResRating());
            CommonMethods.setFontMedium(holder.rating);
            holder.ratingContainer.setBackgroundColor(Color.parseColor("#"+res.getRatingColor()));
            holder.phoneNo.setText(res.getPhoneNO());
            CommonMethods.setFontMedium(holder.phoneNo);
            holder.phoneNo.setSelected(true);



        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if(restaurants!=null){
            return restaurants.size();
        }
        return 0 ;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView restaurantPhoto;
        TextView resName, locality,cuisines ,phoneNo,rating;
        RelativeLayout ratingContainer;
        public MyViewHolder(View view) {
            super(view);
            try{
                restaurantPhoto=(ImageView)view.findViewById(R.id.res_photo);
                resName=(TextView)view.findViewById(R.id.res_name);
                locality=(TextView)view.findViewById(R.id.locality);
                cuisines=(TextView)view.findViewById(R.id.cuisines);
                phoneNo=(TextView)view.findViewById(R.id.phone);
                rating=(TextView)view.findViewById(R.id.res_rating);
                ratingContainer=(RelativeLayout)view.findViewById(R.id.rating_container);

            }catch (Exception ex){
                Log.e(TAG,ex.getMessage());
            }

        }


    }
}
