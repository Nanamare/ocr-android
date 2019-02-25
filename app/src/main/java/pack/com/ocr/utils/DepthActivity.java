package pack.com.ocr.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import pack.com.ocr.R;


/**
 * @author kinamare (nanamare.tistory.com)
 * @contents 앱의 전반적인 생명주기를 담당하는 Activity
 */

public class DepthActivity extends AppCompatActivity {


	private static final String TAG = DepthActivity.class.getSimpleName();

	static {
		System.loadLibrary("opencv_java3");
	}

	static {
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "OpenCV is not loaded!");
		} else {
			Log.d(TAG, "OpenCV is loaded successfully");
		}
	}

	private boolean mIsRootActivity = false;
	private boolean mCloseFlag = false;


	private final Handler mCloseHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mCloseFlag = false;
		}
	};


	@Override
	protected void onCreate(Bundle s){
		super.onCreate(s);

	}

	@Override
	public void onStart(){
		super.onStart();


	}

	@Override
	public void onBackPressed() {
		if (mIsRootActivity) {
			if (mCloseFlag == false) {
				showToast(getResources().getString(R.string.finish_application), Toast.LENGTH_SHORT);

				mCloseFlag = true;
				mCloseHandler.sendEmptyMessageDelayed(0, 3000);
			} else {
				finish();
				onRootFinish();
			}
		} else {
			super.onBackPressed();
		}
	}

	protected void onRootFinish() {}

	public void showToast(final String message, final int duration) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), message, duration).show();
			}
		});
	}



	protected void setRootActivity(boolean isRootActivity) {
		mIsRootActivity = isRootActivity;
	}


}
