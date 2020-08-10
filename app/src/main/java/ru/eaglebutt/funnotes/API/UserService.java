package ru.eaglebutt.funnotes.API;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface UserService {
    @GET(APIConfig.GET_ALL_USER_URL)
    Call<AllUsersResponseData> getAllUserData(@Query("email") String email, @Query("password") String password);

    @GET(APIConfig.USER_API_URL)
    Call<User> getUser(@Query("email") String email, @Query("password") String password);

    @PUT(APIConfig.USER_API_URL)
    Call<Void> putUser(@Body User user);

    @DELETE(APIConfig.USER_API_URL)
    Call<Void> deleteUser(@Query("email") String email, @Query("password") String password);

    @POST(APIConfig.USER_API_URL)
    Call<Void> updateUser(@Query("email") String email, @Query("password") String password, @Body User user);

    @GET(APIConfig.USER_API_EVENT_URL)
    Call<Event> getEvent(@Query("email") String email, @Query("password") String password, @Query("id") int id);

    @POST(APIConfig.USER_API_EVENT_URL)
    Call<Void> putEvent(@Query("email") String email, @Query("password") String password, @Body Event event);

    @DELETE(APIConfig.USER_API_EVENT_URL)
    Call<Void> deleteEvent(@Query("email") String email, @Query("password") String password, @Query("id") int id);
}
