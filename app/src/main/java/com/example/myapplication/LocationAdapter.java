package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationtViewHolder> {

    private Context mCtx;
    private List<Location> locationList;

    public LocationAdapter(Context mCtx, List<Location> locationList) {
        this.mCtx = mCtx;
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public LocationtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.custom_list_layout,null);
        return new LocationtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationtViewHolder holder, int position) {
        Location location = locationList.get(position);

        holder.id.setText(location.getId());
        holder.coordinates.setText(location.getCheck_in_location());
        
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    class LocationtViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView id,coordinates,rating,test;

        public LocationtViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            id = itemView.findViewById(R.id.tvtv_passengerID);
            coordinates = itemView.findViewById(R.id.tvtv_coordinates);
            rating = itemView.findViewById(R.id.textViewRating);
            test = itemView.findViewById(R.id.test);
        }
    }
}
