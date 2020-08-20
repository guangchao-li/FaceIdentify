package com.arcsoft.arcfacedemo.api;



import com.arcsoft.arcfacedemo.api.bean.FaceModel;
import com.arcsoft.arcfacedemo.api.bean.WorkerInfoDto;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/web/workerInfo")
    Observable<JsonObjBase<WorkerInfoDto>> getMissedCount(@Body FaceModel model);

}
