package com.oneuphero.pogotracker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by dan on 12/9/2016.
 */

public interface ApiEndpointInterface {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    @GET("spawns")
    Call<List<PokemonSpawn>> getSpawns(@Query("ll") String ll);

}
