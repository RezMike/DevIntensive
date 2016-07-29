package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UploadPhotoRes;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserLikeRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.network.responses.UserModelRes;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RestService {

    @POST("login")
    Call<UserModelRes> loginUser(@Body UserLoginReq req);

    @GET("user/{userId}")
    Call<UserInfoRes> loginToken(@Path("userId") String userId);

    @GET("user/list?orderBy=rating")
    Call<UserListRes> getUserList();

    @Multipart
    @POST("user/{userId}/publicValues/profilePhoto")
    Call<UploadPhotoRes> uploadPhoto(@Path("userId") String userId,
                                     @Part MultipartBody.Part file);

    @Multipart
    @POST("user/{userId}/publicValues/profileAvatar")
    Call<UploadPhotoRes> uploadAvatar(@Path("userId") String userId,
                                      @Part MultipartBody.Part file);

    @POST("user/{userId}/like")
    Call<UserLikeRes> likeUser(@Path("userId") String userId);

    @POST("user/{userId}/unlike")
    Call<UserLikeRes> unlikeUser(@Path("userId") String userId);
}
