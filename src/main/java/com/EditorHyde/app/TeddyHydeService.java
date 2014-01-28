package com.EditorHyde.app;

import com.google.api.client.json.Json;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by xrdawson on 1/24/14.
 */
public interface TeddyHydeService {

    @FormUrlEncoded
    @POST( "/repository/mobile" )
    void create(
            @Field( "token" ) String token,
            @Field( "title" ) String title,
            @Field( "subtitle" ) String subtitle,
            @Field( "type" ) String type,
            @Field("theme") String theme,
            Callback<Blog> cb
    );

    @GET( "/repository/{id}/status")
    void status( @Path("id") String id, Callback<Blog> cb );

    class Blog {
        int id;
        String status;
    }

}
