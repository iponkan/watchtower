package com.hitqz.robot.watchtower.net;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ISkyNet {

    @GET("/article/list/0/json")
    Observable<BaseRespond<DataBean>> request(@Query("cid") int cid);
}
