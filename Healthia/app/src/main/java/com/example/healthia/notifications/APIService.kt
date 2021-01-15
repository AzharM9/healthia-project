package com.example.healthia.notifications

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type:application/json", "Authorization:key=AAAA3sSgh4A:APA91bEBCSPia0XOhDf-diVxVo6HAppvrilq8reShdQ-IeToy2v6by4jq5XZCpdKnD2-PnldH-B-Gu4YML-T_faL5UeFl_HNgrgB6kL4R9SyQoepvtuGYpD_kvA0734Me0KFW2eOcVHC")
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<Response?>
}