package pack.com.ocr.custom;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Point;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by kinamare on 2017-07-18.
 * 세로 카메라 헬퍼 클래스
 */

public class CustomPortraitCameraView extends PortraitCameraView implements Camera.PictureCallback {

	private static final String TAG = CustomPortraitCameraView.class.getSimpleName();
	private String mPictureFileName;

	public CustomPortraitCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<String> getEffectList() {
		return mCamera.getParameters().getSupportedColorEffects();
	}

	public boolean isEffectSupported() {
		return (mCamera.getParameters().getColorEffect() != null);
	}

	public String getEffect() {
		return mCamera.getParameters().getColorEffect();
	}

	public void setEffect(String effect) {
		Camera.Parameters params = mCamera.getParameters();
		params.setColorEffect(effect);
		mCamera.setParameters(params);
	}

	public List<Camera.Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Camera.Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());
	}

	public Camera.Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;
		// Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the current class
		mCamera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving a bitmap to file");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		// Write the mImage in a file (in jpeg format)
		try {
			FileOutputStream fos = new FileOutputStream(mPictureFileName);

			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("PictureDemo", "Exception in photoCallback", e);
		}

	}

	public void setFocusMode(Context context, int type) {
		Camera.Parameters params = mCamera.getParameters();
		mCamera.cancelAutoFocus();
		mCamera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean b, Camera camera) {

			}
		});

		List<String> focusModes = params.getSupportedFocusModes();

		switch (type) {

			case 0: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				} else {
					Toast.makeText(context, "Auto mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 1: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				} else {
					Toast.makeText(context, "Continuous Mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 2: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
				} else {
					Toast.makeText(context, "EDOF Mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 3: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
				} else {
					Toast.makeText(context, "Fixed Mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 4: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				} else {
					Toast.makeText(context, "Infinity Mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 5: {
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
				} else {
					Toast.makeText(context, "Macro Mode is not supported", Toast.LENGTH_SHORT).show();
				}
				break;
			}


		}

		mCamera.setParameters(params);

	}

	public Camera.Parameters getCameraPrameter(){
		return mCamera.getParameters();
	}

	public Point getCameraSize(){
		Point point = new Point(mFrameWidth, mFrameHeight);
		return point;
	}



}
