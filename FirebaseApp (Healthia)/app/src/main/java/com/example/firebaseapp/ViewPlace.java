package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.firebaseapp.models.Photos;
import com.example.firebaseapp.models.PlaceDetail;
import com.example.firebaseapp.remote.Common;
import com.example.firebaseapp.remote.IGoogleAPIService;
import com.squareup.picasso.Picasso;

public class ViewPlace extends AppCompatActivity {

    ImageView photo;
    RatingBar ratingBar;
    TextView opening_hours, place_address, place_name;
    Button btnViewOnMap;

    IGoogleAPIService mService;

    PlaceDetail mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        mService = Common.getGoogleAPIService();

        photo = findViewById(R.id.photo);
        ratingBar = findViewById(R.id.ratingBar);
        place_name = findViewById(R.id.place_name);
        place_address = findViewById(R.id.place_address);
        opening_hours = findViewById(R.id.place_open_hour);
        btnViewOnMap = findViewById(R.id.btn_show_map);

        //empty all view
        place_name.setText("");
        place_address.setText("");
        opening_hours.setText("");

        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPlace.getResult().getUrl()));
                startActivity(mapIntent);
            }
        });

        //Photo
        if (Common.currentResult.getPhotos() != null && Common.currentResult.getPhotos().length > 0){
            //because getPhoto() return array so we will take first item
            Picasso.get()
                    .load(getPhotoOfPiece(Common.currentResult.getPhotos()[0].getPhoto_reference(), 1000))
                    .placeholder(R.drawable.ic_image_black_24)
                    .error(R.drawable.ic_image_black_24)
                    .into(photo);
        }

        //Rating
        if (Common.currentResult.getRating() != null && !TextUtils.isEmpty(Common.currentResult.getRating())){

            ratingBar.setRating(Float.parseFloat(Common.currentResult.getRating()));
        }
        else{
            ratingBar.setVisibility(View.GONE);
        }

        //Opening_hours
        if (Common.currentResult.getOpening_hours() != null){

            opening_hours.setText("Open Now: "+Common.currentResult.getOpening_hours().getOpen_now());
        }
        else{
            opening_hours.setVisibility(View.GONE);
        }

        //User service to fetch address & name
        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult.getPlace_id()))
                .enqueue(new Callback<PlaceDetail>() {
                    @Override
                    public void onResponse(Call<PlaceDetail> call, Response<PlaceDetail> response) {
                        mPlace = response.body();

                        place_name.setText(mPlace.getResult().getName());
                        place_address.setText(mPlace.getResult().getFormatted_address());
                    }

                    @Override
                    public void onFailure(Call<PlaceDetail> call, Throwable t) {

                    }
                });
    }

    private String getPlaceDetailUrl(String place_id) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        url.append("place_id="+place_id);
        url.append("&key="+getResources().getString(R.string.browser_key));
        return url.toString();
    }

    private String getPhotoOfPiece(String photo_reference, int maxWidth) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        url.append("maxwidth="+maxWidth);
        url.append("&photoreference="+photo_reference);
        url.append("&key="+getResources().getString(R.string.browser_key));
        return url.toString();
    }
}