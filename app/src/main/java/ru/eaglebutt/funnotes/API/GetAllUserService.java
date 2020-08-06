package ru.eaglebutt.funnotes.API;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetAllUserService {
    @GET(APIConfig.GET_ALL_USER)
    Call<AllUsersResponseData> getAllUserData(@Query("email") String email, @Query("password") String password );
}
