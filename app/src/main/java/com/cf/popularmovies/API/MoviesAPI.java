package com.cf.popularmovies.API;

import com.cf.popularmovies.model.Page;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by fuerst on 09.07.15.
 */
public interface MoviesAPI {

    @GET("/discover/movie")
    Page getPages(
            @Query("api_key") String api_key,
            @Query("sort_by") String sort_by
    );

}
