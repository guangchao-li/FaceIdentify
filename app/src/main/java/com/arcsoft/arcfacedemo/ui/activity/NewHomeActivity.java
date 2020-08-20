package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.databinding.ActivityActivationBinding;
import com.arcsoft.arcfacedemo.ui.viewmodel.ActiveViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.madgaze.smartglass.activity.MADMenuActivity;
import com.madgaze.smartglass.model.MADMenuItem;
import com.madgaze.smartglass.view.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import io.reactivex.annotations.NonNull;


public class NewHomeActivity extends MADMenuActivity {
	private static final String TAG = "HomeActivity";
	/**
	 * SnackBar能显示的最多行数
	 */
	private static final int SNACK_BAR_MAX_LINES = 50;
	private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
	private FaceEngine faceEngine = new FaceEngine();
	// 在线激活所需的权限
	private static final String[] NEEDED_PERMISSIONS = new String[]{
			Manifest.permission.READ_PHONE_STATE
	};
	private ActivityActivationBinding binding;
	private ActiveViewModel activeViewModel;
	private Snackbar snackbar;
	private static String DEFAULT_AUTH_FILE_PATH;

	/**
	 * 离线激活所需的所有权限信息
	 */
	private static final String[] NEEDED_PERMISSIONS_OFFLINE = new String[]{
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.READ_PHONE_STATE
	};
	/**
	 * 读取本地配置文件激活的所有权限信息
	 */
	private static final String[] NEEDED_PERMISSIONS_ACTIVE_FROM_CONFIG_FILE = new String[]{
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.READ_PHONE_STATE
	};
	/**
	 * 获取设备信息的所需的权限信息
	 */
	private static final String[] NEEDED_PERMISSIONS_GET_DEVICE_INFO = new String[]{
			Manifest.permission.READ_PHONE_STATE
	};
	private static final int ACTION_REQUEST_ACTIVE_OFFLINE = 1;
	private static final int ACTION_REQUEST_ACTIVE_ONLINE = 2;
	private static final int ACTION_REQUEST_COPY_DEVICE_FINGER = 3;
	private static final int ACTION_REQUEST_ACTIVE_FROM_CONFIG_FILE = 4;
	private static ExecutorService executor;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set menu page title
		setPageTitle("请选择");
		List<MADMenuItem> list = new ArrayList<>();
		list.add(new MADMenuItem("激活"));
		list.add(new MADMenuItem("人脸识别"));

		setData(list);
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
		initViewModel();
		initView();
	}

	@Override
	public void onMenuItemSelected(int position, MADMenuItem model) {
		if (position==0){
			activeEngine();
		}else if(position==1){
			startActivity(new Intent(this, RegisterAndRecognizeActivity.class));
		}
		if(model.isRadioButtonItem()) {
			model.switchSelected();
			notifyDataSetChanged();
		}
	}
	/**
	 * 在子线程中执行任务
	 *
	 * @param runnable
	 */
	public void runOnSubThread(Runnable runnable) {
		executor.execute(runnable);
	}

	/**
	 * 激活引擎
	 */
	public void activeEngine() {
		DEFAULT_AUTH_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.active_file_name);
		if (checkPermissions(NEEDED_PERMISSIONS_GET_DEVICE_INFO)) {
				snackbar = showIndefiniteSnackBar(binding.getRoot(), getString(R.string.please_wait), null, null);
				runOnSubThread(() -> activeViewModel.activeOnline(getApplicationContext(), ConfigUtil.getActiveKey(this), ConfigUtil.getAppId(this), ConfigUtil.getSdkKey(this)));
			} else {
				ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_GET_DEVICE_INFO, ACTION_REQUEST_ACTIVE_ONLINE);
			}

	}
	private void initView() {
		enableBackIfActionBarExists();
	}

	private void initViewModel() {
		activeViewModel = new ViewModelProvider(
				getViewModelStore(),
				new ViewModelProvider.AndroidViewModelFactory(getApplication())
		)
				.get(ActiveViewModel.class);
		activeViewModel.getActiveResult().observe(this, result -> {
			if (snackbar != null) {
				snackbar.dismiss();
				snackbar = null;
			}
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
		});
	}
	protected void showToast(String s) {
		Toast.makeText(this, "MADToast", s, Toast.Duration.SHORT).show();
	}
	/**
	 * 权限检查
	 *
	 * @param neededPermissions 需要的权限
	 * @return 是否全部被允许
	 */
	protected boolean checkPermissions(String[] neededPermissions) {
		if (neededPermissions == null || neededPermissions.length == 0) {
			return true;
		}
		boolean allGranted = true;
		for (String neededPermission : neededPermissions) {
			allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
		}
		return allGranted;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean isAllGranted = true;
		for (int grantResult : grantResults) {
			isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
		}
		if (requestCode == ACTION_REQUEST_PERMISSIONS) {
			if (isAllGranted) {
				activeEngine();
			} else {
				showToast(getString(R.string.permission_denied));
			}
		}
	}
	protected void enableBackIfActionBarExists() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	protected void showLongSnackBar(final View view, final String s) {
		Snackbar snackbar = Snackbar.make(view, s, Snackbar.LENGTH_LONG);
		enableSnackBarShowMultiLines(snackbar, SNACK_BAR_MAX_LINES);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			snackbar.show();
		} else {
			runOnUiThread(snackbar::show);
		}
	}
	protected Snackbar showIndefiniteSnackBar(final View view, final String s, String action, View.OnClickListener onClickListener) {
		Snackbar snackbar = Snackbar.make(view, s, Snackbar.LENGTH_INDEFINITE);
		enableSnackBarShowMultiLines(snackbar, SNACK_BAR_MAX_LINES);
		snackbar.setAction(action, onClickListener);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			snackbar.show();
		} else {
			runOnUiThread(snackbar::show);
		}
		return snackbar;
	}
	private void enableSnackBarShowMultiLines(Snackbar snackbar, int maxLines) {
		final SnackbarContentLayout contentLayout = (SnackbarContentLayout) ((ViewGroup) snackbar.getView()).getChildAt(0);
		final TextView tv = contentLayout.getMessageView();
		tv.setMaxLines(maxLines);
	}

}
