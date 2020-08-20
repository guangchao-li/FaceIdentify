package com.arcsoft.arcfacedemo.api;



import com.arcsoft.arcfacedemo.api.bean.FaceModel;
import com.arcsoft.arcfacedemo.api.bean.WorkerInfoDto;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BeanImpl implements BeanInterface {

    private static BeanImpl mImpl;
    private ApiService apiService;

    public BeanImpl(
            ApiService apiService
    ) {
        this.apiService = apiService;
    }


    public static BeanImpl getInstance() {
        ApiService apiService = null;
        if (null == mImpl) {
            synchronized (BeanImpl.class) {
                if (mImpl == null) {
                    mImpl = new BeanImpl(apiService);
                }
            }
        }
        return mImpl;
    }

    @Override
    public Observable<JsonObjBase<WorkerInfoDto>> getFaceInfo(String id, String face) {
        return RetrofitManager.getInstance().getRetrofit().create(ApiService.class)
                .getMissedCount(new FaceModel(id, face))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
