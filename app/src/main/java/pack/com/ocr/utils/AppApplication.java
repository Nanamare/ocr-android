package pack.com.ocr.utils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import android.util.Log;

import java.io.File;

import pack.com.ocr.R;


/**
 * Created by kinamare on 2016-12-17.
 */

public class AppApplication extends Application {

	//	public String picPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
	public static final String TAG = AppApplication.class.getSimpleName();

	private static AppApplication sInstance;

	public static synchronized AppApplication getInstance() {
		if (sInstance != null)
			return sInstance;
		return null;
	}

	public Context getContext() {
		return this.getApplicationContext();
	}


	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);


	}


	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;



	}

	//앱 내부에 보이지 않게 저장
	public static String getPicInnerPath(Context context) {
		String dirPath = context.getFilesDir().getAbsolutePath() + "/" +
				getInstance().getContext().getString(R.string.app_name);
		Log.d(TAG, dirPath);
		File dir = new File(dirPath);
		if (!dir.exists())
			dir.mkdirs();
		return dirPath + "/";
	}

	//앱 외부에 보이도록 저장
	public static String getPicExternalPath() {
		String folderPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM)
				+ "/Nanamare";
		Log.d(TAG, folderPath);
		File dir = new File(folderPath);
		if (!dir.exists())
			dir.mkdirs();
		return folderPath + "/";

	}

	public static String getPicExternalCropPath() {
		String folderPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM)
				+ "/Nanamare/CropImage";
		Log.d(TAG, folderPath);
		File dir = new File(folderPath);
		if (!dir.exists())
			dir.mkdirs();
		return folderPath + "/";

	}



}
