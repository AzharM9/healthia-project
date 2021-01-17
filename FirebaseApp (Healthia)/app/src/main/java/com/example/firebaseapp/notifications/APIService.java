package com.example.firebaseapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAfv6YjN8:APA91bEnHW7iY-8uC2a0-jlpT9DJCLi9ht0fgGX_y67BgRHQFw-PUb3mh5LbzEkBZ3KeOfKZGAy-3yvM--AHgE6G6MjnouICX0otXtR697NcJ1NsWSQALqRx6yxW72c0xPBLbqPWfoVF"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
