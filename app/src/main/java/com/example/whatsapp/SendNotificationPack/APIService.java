package com.example.whatsapp.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAWaZf0aM:APA91bGvlfn9jDanKnAjX2jsEgo0bwQMIASNiXeFie_qUAl8TdTqrNuBG4NAA3W6E4tdnIicOWG9UsyZCHdtVgGTW86xLOL5kz_vBwraRejVw9gXhn0Yjk8Z06GD5f1smDf4RYtUceKd" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}
