package com.example.firebaseapp.remote;

import com.example.firebaseapp.models.MyPlaces;
import com.example.firebaseapp.models.Results;

public class Common {

    public static Results currentResult;

    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static IGoogleAPIService getGoogleAPIService(){

        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
