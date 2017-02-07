package com.bisma.buswatch.service;


import com.bisma.buswatch.model.BusInfoList;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Bisma on 1/2/17.
 */

public interface APIService {

    @GET("bus")
    Call<BusInfoList> getBusInfo();

}
