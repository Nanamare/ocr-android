package pack.com.ocr;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import pack.com.ocr.custom.CustomPortraitCameraView;
import pack.com.ocr.custom.PortraitCameraBridgeViewBase;
import pack.com.ocr.dto.PhotoAlbum;
import pack.com.ocr.process.CameraProcess;
import pack.com.ocr.utils.DepthActivity;

public class MainActivity extends DepthActivity implements CustomPortraitCameraView.CvCameraViewListener2 {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int PERMISSIONS_REQUEST_CODE = 9999;

	public static final int REQUEST_IMAGE_FROM_ALBUM = 999;


	@BindView(R.id.activity_main_custom_camera_view) CustomPortraitCameraView mCustomCameraView;
	@BindView(R.id.fragment_camera_album_iv) CircleImageView mAlbumIv;


	private Mat mRgb = null;
	private Mat mImageFrame = null;
	private PhotoAlbum mPhotoAlbum;
	private CameraProcess mCameraProcess;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		checkThePermission();

		ButterKnife.bind(this);

		setRootActivity(true);

	}

	public void checkThePermission() {
		if (Build.VERSION.SDK_INT > 22) {
			boolean hasPermission = (ContextCompat.checkSelfPermission(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasPermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{
								Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case PERMISSIONS_REQUEST_CODE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					initOpenCvCameraSetting();
					initView();
					mCameraProcess = new CameraProcess(this);

				} else {
					Toast.makeText(this, getString(R.string.warning_content), Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@OnClick(R.id.fragment_camera_send_iv)
	void sendBtnOnClick() {

		mImageFrame = new Mat();
		Imgproc.cvtColor(mRgb, mImageFrame, Imgproc.COLOR_RGBA2RGB);
		findWord(mImageFrame);

	}

	/**
	 * @contents adaptiveThreshold 기반의 글자 영억 추출
	 */
	private Mat findWord(Mat mImageFrame) {

		Mat origin = mImageFrame.clone();

		Mat grayFrame = mCameraProcess.imagePreProcessing(mImageFrame);

		Mat thresholdFrame = mCameraProcess.adaptiveThreshold(grayFrame);

		mCameraProcess.saveJpgFile(thresholdFrame);

		Mat resultFrame = mCameraProcess.findContours(origin, thresholdFrame);

		mCameraProcess.saveJpgFile(resultFrame);

		return resultFrame;
	}


	public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					mCustomCameraView.enableView();
					break;
				}
				default: {
					super.onManagerConnected(status);
					break;
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
		} else {
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}

	}

	private void initOpenCvCameraSetting() {

		mCustomCameraView.setVisibility(SurfaceView.VISIBLE);
		mCustomCameraView.setCvCameraViewListener(this);

	}


	@Override
	public void onPause() {
		super.onPause();
		if (mCustomCameraView != null)
			mCustomCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mCustomCameraView != null)
			mCustomCameraView.disableView();
	}


	/**
	 * 카메라 포커스
	 */
	@OnClick(R.id.activity_main_custom_camera_view)
	void cameraOnClick() {

		mCustomCameraView.setFocusMode(this, 0);

	}


	@Override
	public void onCameraViewStarted(int width, int height) {

	}

	@Override
	public void onCameraViewStopped() {

	}


	@Override
	synchronized public Mat onCameraFrame(PortraitCameraBridgeViewBase.CvCameraViewFrame inputFrame) {

		mRgb = inputFrame.rgba();
		Core.add(mRgb, new Scalar(10, 10, 10), mRgb);

		Mat filter = mRgb.clone();

		return filter;
	}

	@OnClick(R.id.fragment_camera_album_iv)
	void onAlbumBtnClick() {

		loadToPicFromAlbum();
	}

	private void loadToPicFromAlbum() {

		Intent albumIntent = new Intent();
		albumIntent.setAction(Intent.ACTION_PICK);
		albumIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(albumIntent, REQUEST_IMAGE_FROM_ALBUM);

	}


	//load to recent picture in circleView
	private void initView() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mPhotoAlbum = fetchOneImage();
				if (mPhotoAlbum != null) {
					handler.sendEmptyMessage(0);
				}
			}
		}).start();

	}

	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			if (message.what == 0) {
				mAlbumIv.setImageURI(mPhotoAlbum.getThumnailUri());
			}
			return false;
		}
	});

	private PhotoAlbum fetchOneImage() {
		PhotoAlbum result;
		String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

		Cursor imageCursor = this.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection,
				null,
				null,
				null
		);

		if (imageCursor != null) {
			imageCursor.moveToLast();
			int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
			int idColumnIndex = imageCursor.getColumnIndex(projection[1]);
			String filePath = imageCursor.getString(dataColumnIndex);
			String imageId = imageCursor.getString(idColumnIndex);
			Uri imageUri = Uri.parse(filePath);
			Uri thumbnailUri = uriToThumbnail(imageId);
			result = new PhotoAlbum(thumbnailUri, imageUri);
		} else {
			result = null;
		}
		if (imageCursor != null) {
			imageCursor.close();
		}
		return result;
	}

	private Uri uriToThumbnail(String imageId) {
		String[] projection = {MediaStore.Images.Media.DATA};
		ContentResolver contentResolver = this.getContentResolver();

		Cursor thumbnailCursor = contentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection,
				"_ID='" + imageId + "'",
				null,
				null
		);

		if (thumbnailCursor != null) {
			thumbnailCursor.moveToLast();
			int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);
			String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
			thumbnailCursor.close();
			return Uri.parse(thumbnailPath);
		} else {
			MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
			return uriToThumbnail(imageId);
		}
	}


}
