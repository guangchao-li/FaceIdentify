package com.arcsoft.arcfacedemo.api;


import com.arcsoft.arcfacedemo.api.bean.WorkerInfoDto;

import io.reactivex.Observable;

public interface BeanInterface {

    Observable<JsonObjBase<WorkerInfoDto>> getFaceInfo(String id, String face);

}
