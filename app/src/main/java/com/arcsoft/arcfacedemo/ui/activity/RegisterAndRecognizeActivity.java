package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.api.BaseObserver;
import com.arcsoft.arcfacedemo.api.BeanImpl;
import com.arcsoft.arcfacedemo.api.JsonObjBase;
import com.arcsoft.arcfacedemo.api.bean.WorkerInfoDto;
import com.arcsoft.arcfacedemo.databinding.ActivityRegisterAndRecognizeBinding;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
import com.arcsoft.arcfacedemo.ui.viewmodel.ActiveViewModel;
import com.arcsoft.arcfacedemo.ui.viewmodel.RecognizeViewModel;
import com.arcsoft.arcfacedemo.util.Base64Utils;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.SpUtils;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.constants.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.RecognizeAreaView;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceFeature;
import com.madgaze.smartglass.detector.MADGestureTouchDetector;
import com.madgaze.smartglass.hardware.KeyCodeHelper;
import com.madgaze.smartglass.view.GestureView;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegisterAndRecognizeActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "RegisterAndRecognize";

    private Context context;
    private DualCameraHelper rgbCameraHelper;
    private DualCameraHelper irCameraHelper;
    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;
    private FaceHelper faceHelper;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    int actionAfterFinish = 0;
    private static final int NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY = 1;
    private static final int NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY = 2;
    public ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };
    private ActivityRegisterAndRecognizeBinding binding;
    private RecognizeViewModel recognizeViewModel;
    private LivenessType livenessType;
    private boolean enableLivenessDetect = false;
    RecognizeAreaView recognizeAreaView;
    TextView textViewRgb;
    TextView textViewIr;
    private GestureView gestureView;
    private TextView name,xm,ry,name1,xm1,ry1,name3,xm3,ry3,name4,xm4,ry4;
    private LinearLayout llshow,llshow1,llshow3,llshow4;
    private  int shownum=1;
    private WorkerInfoDto work;
    private FaceFeature face;
    private ImageView face1;
    private ImageView face2;
    private static ExecutorService executor;
    private ActiveViewModel activeViewModel;
    private static String DEFAULT_AUTH_FILE_PATH;
    private static final int ACTION_REQUEST_ACTIVE_OFFLINE = 1;
    private static final int ACTION_REQUEST_ACTIVE_ONLINE = 2;
    private static final int ACTION_REQUEST_COPY_DEVICE_FINGER = 3;
    private static final int ACTION_REQUEST_ACTIVE_FROM_CONFIG_FILE = 4;
    /**
     * 获取设备信息的所需的权限信息
     */
    private static final String[] NEEDED_PERMISSIONS_GET_DEVICE_INFO = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_and_recognize);
        if (executor == null) {
            executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("activity-sub-thread-" + t.getId());
                        return t;
                    });
        }

        boolean isFirstOpen = SpUtils.getBoolean(this, "first_open");
        if(isFirstOpen){
            prepare();
        }else{
            initViewFir();
            activeEngine();
            SpUtils.putBoolean(this, "first_open", true);
        }
    }

    private void prepare(){

       context = this;
        face1 = (ImageView) findViewById(R.id.face1);
        face2 = (ImageView) findViewById(R.id.face2);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        initData();
        initViewModel();
        initView();
    }
    /**
     * 激活引擎
     */
    public void activeEngine() {
        DEFAULT_AUTH_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.active_file_name);
        if (checkPermissions(NEEDED_PERMISSIONS_GET_DEVICE_INFO)) {
            runOnSubThread(() -> activeViewModel.activeOnline(getApplicationContext(), ConfigUtil.getActiveKey(this), ConfigUtil.getAppId(this), ConfigUtil.getSdkKey(this)));
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_GET_DEVICE_INFO, ACTION_REQUEST_ACTIVE_ONLINE);
        }

    }
    private void initViewFir() {
        activeViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        )
                .get(ActiveViewModel.class);
        activeViewModel.getActiveResult().observe(this, result -> {
            String notice;
            switch (result) {
                case ErrorInfo.MOK:
                    notice = getString(R.string.active_success);
                    break;
                case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED:
                    notice = getString(R.string.already_activated);
                    break;
                case ErrorInfo.MERR_ASF_ACTIVEKEY_ACTIVEKEY_ACTIVATED:
                    notice = getString(R.string.active_key_activated);
                    break;
                default:
                    notice = getString(R.string.active_failed, result, ErrorCodeUtil.arcFaceErrorCodeToFieldName(result));
                    break;
            }
            showLongSnackBar(binding.getRoot(), notice);
            if(notice.equals(getString(R.string.active_success))){
                prepare();
            }
        });
    }
    private void initData() {
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
        if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))){
            livenessType = LivenessType.RGB;
        }else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))){
            livenessType = LivenessType.IR;
        }else {
            livenessType = null;
        }
        enableLivenessDetect = !ConfigUtil.getLivenessDetectType(this).equals(getString(R.string.value_liveness_type_disable));
    }


    private void initViewModel() {
        recognizeViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        )
                .get(RecognizeViewModel.class);
        recognizeViewModel.setContext(this);
        recognizeViewModel.setLivenessType(livenessType);

        recognizeViewModel.getFtInitCode().observe(this, ftInitCode -> {
            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine",
                        ftInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(ftInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFrInitCode().observe(this, frInitCode -> {
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine",
                        frInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(frInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFlInitCode().observe(this, flInitCode -> {
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine",
                        flInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(flInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFaceItemEventMutableLiveData().observe(this, faceItemEvent -> {
            RecyclerView.Adapter adapter = binding.dualCameraRecyclerViewPerson.getAdapter();
            switch (faceItemEvent.getEventType()) {
                case REMOVED:
                    if (adapter != null) {
                        adapter.notifyItemRemoved(faceItemEvent.getIndex());
                    }
                    break;
                case INSERTED:
                    if (adapter != null) {
                        adapter.notifyItemInserted(faceItemEvent.getIndex());
                    }
                    break;
                default:
                    break;
            }
        });

        recognizeViewModel.getRecognizeConfiguration().observe(this, recognizeConfiguration -> {
            Log.i(TAG, "initViewModel recognizeConfiguration: " + recognizeConfiguration.toString());
        });

        recognizeViewModel.setOnRegisterFinishedCallback((facePreviewInfo, success) -> showToast(success ? "register success" : "register failed"));
    }

    private void initView() {
        //在布局结束后才做初始化操作
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        binding.setCompareResultList(recognizeViewModel.getCompareResultList().getValue());
        name = (TextView)findViewById(R.id.tv_name);
        xm = (TextView)findViewById(R.id.tv_xm);
        ry=(TextView)findViewById(R.id.tv_ry);
        llshow = (LinearLayout)findViewById(R.id.ll_show);
        name1= (TextView)findViewById(R.id.tv_name1);
        xm1 = (TextView)findViewById(R.id.tv_xm1);
        ry1=(TextView)findViewById(R.id.tv_ry1);
        llshow1 = (LinearLayout)findViewById(R.id.ll_show1);
        name3= (TextView)findViewById(R.id.tv_name3);
        xm3 = (TextView)findViewById(R.id.tv_xm3);
        ry3=(TextView)findViewById(R.id.tv_ry3);
        llshow3 = (LinearLayout)findViewById(R.id.ll_show3);
        name4= (TextView)findViewById(R.id.tv_name4);
        xm4 = (TextView)findViewById(R.id.tv_xm4);
        ry4=(TextView)findViewById(R.id.tv_ry4);
        llshow4 = (LinearLayout)findViewById(R.id.ll_show4);
        gestureView = (GestureView) findViewById(R.id.my_gesture);
        gestureView.getGestureListener().setOnSwipeListener(new MADGestureTouchDetector.OnSwipeListener() {
            @Override
            public void onSwipeRight() {
                llshow.setVisibility(View.VISIBLE);
                llshow1.setVisibility(View.VISIBLE);
                llshow3.setVisibility(View.VISIBLE);
                llshow4.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSwipeLeft() {
                llshow.setVisibility(View.VISIBLE);
                llshow1.setVisibility(View.VISIBLE);
                llshow3.setVisibility(View.VISIBLE);
                llshow4.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeUp() {

            }

            @Override
            public void onSwipeDown() {

            }
        });
        gestureView.getGestureListener().setOnTapListener(new MADGestureTouchDetector.OnTapListener() {
            @Override
            public void onTapEvent(int finger, MotionEvent motionEvent) {

            }

            @Override
            public void onDoubleTap(int finger, MotionEvent motionEvent) {
                finish();
            }

            @Override
            public void onTripleTap(int finger, MotionEvent motionEvent) {

            }
        });
    }

    private void show(int num){
        if(work!=null){
            if(work!=null){
                    llshow.setVisibility(View.VISIBLE);
                    name.setText("姓名："+work.getWorkerName() +"   年龄："+work.getAge()+" 岁");
                    xm.setText("进场时间：" +work.getJoinTime());
                    ry.setText("工种："+work.getWorkerType()+"   工龄："+work.getWorkYears()+" 年");

                    llshow1.setVisibility(View.VISIBLE);
                    name1.setText("重大病史："+work.getMajorMedicalHistory());
                    if(work.getIsPhysicalExamed()!=null){
                        xm1.setText("是否体检："+work.getIsPhysicalExamed());
                    }else{
                        xm1.setText("是否体检： 暂无信息");
                    }
                    ry1.setText("安全考试得分："+work.getSafetyEducationScore()+" 分");

                    llshow3.setVisibility(View.VISIBLE);
                    name3.setText("所属项目："+work.getProjectDepartmentName());
                    xm3.setText("所属架子队："+work.getShelfTeamName());
                    ry3.setText("所在班组："+work.getTeamName());

                    llshow4.setVisibility(View.VISIBLE);
                    name4.setText("行为安全之星："+work.getBehaviorSafetystars());
                    xm4.setText("不良记录："+work.getBadRecordCount()+" 次");
                    ry4.setText("良好记录："+work.getGoodRecordCount()+" 次");
            }
        }
    }
    @Override
    protected void onDestroy() {
        if (irCameraHelper != null) {
            irCameraHelper.release();
            irCameraHelper = null;
        }

        if (rgbCameraHelper != null) {
            rgbCameraHelper.release();
            rgbCameraHelper = null;
        }

        recognizeViewModel.destroy();
        switch (actionAfterFinish) {
            case NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY:
                navigateToNewPage(RecognizeDebugActivity.class);
                break;
            case NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY:
                navigateToNewPage(RecognizeSettingsActivity.class);
                break;
            default:
                break;
        }
        super.onDestroy();
    }


    /**
     * 调整View的宽高，使2个预览同时显示
     *
     * @param previewView        显示预览数据的view
     * @param faceRectView       画框的view
     * @param previewSize        预览大小
     * @param displayOrientation 相机旋转角度
     * @return 调整后的LayoutParams
     */
    private ViewGroup.LayoutParams adjustPreviewViewSize(View previewView, FaceRectView faceRectView, Camera.Size previewSize, int displayOrientation, float scale) {
        ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
        int measuredWidth = previewView.getMeasuredWidth();
        int measuredHeight = previewView.getMeasuredHeight();
        float ratio = ((float) previewSize.height) / (float) previewSize.width;
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        if (displayOrientation % 180 == 0) {
            layoutParams.width = measuredWidth;
            layoutParams.height = (int) (measuredWidth * ratio);
        } else {
            layoutParams.height = measuredHeight;
            layoutParams.width = (int) (measuredHeight * ratio);
        }
        layoutParams.width *= scale;
        layoutParams.height *= scale;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (layoutParams.width >= metrics.widthPixels) {
            float viewRatio = layoutParams.width / ((float) metrics.widthPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }
        if (layoutParams.height >= metrics.heightPixels) {
            float viewRatio = layoutParams.height / ((float) metrics.heightPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }

        previewView.setLayoutParams(layoutParams);
        faceRectView.setLayoutParams(layoutParams);
        return layoutParams;
    }

    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(
                        binding.dualCameraTexturePreviewRgb, binding.dualCameraFaceRectView,
                        previewSizeRgb, displayOrientation, 1.0f);
                rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawRgbRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                        ConfigUtil.isDrawRgbRectVerticalMirror(RegisterAndRecognizeActivity.this));

                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());

                if (textViewRgb == null) {
                    textViewRgb = new TextView(RegisterAndRecognizeActivity.this, null);
                } else {
                    parentView.removeView(textViewRgb);
                }
                textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewRgb.setText(getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                textViewRgb.setTextColor(Color.WHITE);
                textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                parentView.addView(textViewRgb);
                // 父View宽度和子View一致，保持居中
                ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();
                parentLayoutParams.width = layoutParams.width;
                parentView.setLayoutParams(parentLayoutParams);

                // 添加recognizeAreaView，在识别区域发生变更时，更新数据给FaceHelper
                if (ConfigUtil.isRecognizeAreaLimited(RegisterAndRecognizeActivity.this)) {
                    if (recognizeAreaView == null) {
                        recognizeAreaView = new RecognizeAreaView(RegisterAndRecognizeActivity.this);
                        recognizeAreaView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    } else {
                        parentView.removeView(recognizeAreaView);
                    }
                    recognizeAreaView.setOnRecognizeAreaChangedListener(recognizeArea -> recognizeViewModel.setRecognizeArea(recognizeArea));
                    parentView.addView(recognizeAreaView);
                }

                recognizeViewModel.onRgbCameraOpened(camera);
                recognizeViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                binding.dualCameraFaceRectView.clearFaceInfo();
                List<FacePreviewInfo> facePreviewInfoList = recognizeViewModel.onPreviewFrame(nv21, true);
                if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
                recognizeViewModel.clearLeftFace(facePreviewInfoList);
                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 ) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status =  recognizeViewModel.faceHelper.recognizeInfoMap.get(facePreviewInfoList.get(i).getTrackId()).getRecognizeStatus();
                        if ( status!=null && status == RequestFeatureStatus.SUCCEED){
                            FaceFeature faceFeature =  FaceServer.getInstance().getFaceFeature(nv21,recognizeViewModel.previewSize.width,recognizeViewModel.previewSize.height,facePreviewInfoList.get(i).getFaceInfoRgb());
                            Bitmap bitmap = FaceServer.getInstance().getFaceBitmap(nv21,recognizeViewModel.previewSize.width,recognizeViewModel.previewSize.height,facePreviewInfoList.get(i).getFaceInfoRgb());
                            if (face==null){
                                face = faceFeature;
//                                face1.setImageBitmap(bitmap);
                            }else {
                                if (isSameFace == false){
                                    isSameFace =  FaceServer.getInstance().compareTowFace(face,faceFeature);
                                    if (isSameFace){
                                        face2.setImageBitmap(bitmap);
                                        getWorkInfo("1",faceFeature.getFeatureData(),recognizeViewModel.num);
                                    }else {
                                        face = null;
                                        isSameFace = false;
                                    }
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (rgbFaceRectTransformer != null) {
                    rgbFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        rgbCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(), binding.dualCameraTexturePreviewRgb.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation())
                .previewSize(recognizeViewModel.loadPreviewSize())
                .specificCameraId(previewConfig.getRgbCameraId())
                .isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this))
                .previewOn(binding.dualCameraTexturePreviewRgb)
                .cameraListener(cameraListener)
                .build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }
    public boolean isSameFace = false;
    /**
     * 初始化红外相机，若活体检测类型是可见光活体检测或不启用活体，则不需要启用
     */
    private void initIrCamera() {
        if (livenessType == LivenessType.RGB || !enableLivenessDetect) {
            return;
        }
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(
                        binding.dualCameraTexturePreviewIr, binding.dualCameraFaceRectViewIr,
                        previewSizeIr, displayOrientation, 0.25f);

                irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawIrRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                        ConfigUtil.isDrawIrRectVerticalMirror(RegisterAndRecognizeActivity.this));

                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent());
                if (textViewIr == null) {
                    textViewIr = new TextView(RegisterAndRecognizeActivity.this, null);
                } else {
                    parentView.removeView(textViewIr);
                }
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr.setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                parentView.addView(textViewIr);

                recognizeViewModel.onIrCameraOpened(camera);
                recognizeViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                recognizeViewModel.refreshIrPreviewData(nv21);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (irFaceRectTransformer != null) {
                    irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        irCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(), binding.dualCameraTexturePreviewIr.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(previewConfig.getIrCameraId())
                .previewOn(binding.dualCameraTexturePreviewIr)
                .cameraListener(irCameraListener)
                .isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this))
                .previewSize(recognizeViewModel.loadPreviewSize()) //相机预览大小设置，RGB与IR需使用相同大小
                .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation()) //额外旋转角度
                .build();
        irCameraHelper.init();
        try {
            irCameraHelper.start();
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }


    /**
     * 绘制RGB、IR画面的实时人脸信息
     *
     * @param facePreviewInfoList RGB画面的实时人脸信息
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        if (rgbFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> rgbDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB);
            binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
        }
        if (irFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> irDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR);
            binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                recognizeViewModel.init();
                initRgbCamera();
                initIrCamera();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }


    /**
     * 将准备注册的状态置为待注册
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        recognizeViewModel.prepareRegister();
    }

    /**
     * 参数配置
     *
     * @param view
     */
    public void setting(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 识别分析界面
     *
     * @param view 注册按钮
     */
    public void recognizeDebug(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 在{@link ActivityRegisterAndRecognizeBinding#dualCameraTexturePreviewRgb}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            recognizeViewModel.init();
            initRgbCamera();
            initIrCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
        }
    }

    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
    }

    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
        }
    }
    public void getWorkInfo(String id, byte[] face,final Integer requestId) {
        String facedata = Base64Utils.byte2Base64String(face);

        BeanImpl.getInstance().getFaceInfo(id,facedata).subscribe(new BaseObserver<JsonObjBase<WorkerInfoDto>>(context) {
            @Override
            public void onError(int errorCode, String message) {
                Toast.makeText(RegisterAndRecognizeActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(JsonObjBase<WorkerInfoDto> userJsonObjBase) {
                if (userJsonObjBase.getData()!=null){
                    work = userJsonObjBase.getData();
                    show(1);
                }else{
                    Toast.makeText(RegisterAndRecognizeActivity.this, "无此人信息", Toast.LENGTH_SHORT).show();
                }
            }
        }) ;

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        switch (KeyCodeHelper.CheckKeyType(event.getKeyCode(), event)) {
            //KeyCodeHelper.CheckKeyType() transforms the hardware buttons to custom keycodes.
            case RIGHT_DOWN:
                // Equivalent to Button B clicked.
            case LEFT_UP:
                // Equivalent to Button C clicked.
            case CONFIRM:
                finish();
                // Equivalent to Button A clicked.
            case MENU:
                // Equivalent to Button B long-pressed.
            case BACK:
                // Equivalent to Button C long-pressed.
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
