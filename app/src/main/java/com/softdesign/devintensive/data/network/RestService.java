package com.softdesign.devintensive.data.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UserModelRes;

public interface RestService {

    @POST("login")
    Call<UserModelRes> loginUser (@Body UserLoginReq req);
}
