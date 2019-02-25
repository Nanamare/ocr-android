package pack.com.ocr.process;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import pack.com.ocr.R;
import pack.com.ocr.utils.AppApplication;

/**
 * @author kinamare (nanamare.tistory.com)
 * @contents 이미지 처리에 대한 함수들을 담은 클래스
 */

public class CameraProcess implements ImplCameraProcess {

	public static final int RESIZE_NUMBER = 2;
	public static final int CROP_IMAGE = 999;

	private Context mContext;

	public CameraProcess(Context context) {
		mContext = context;
	}

	@Override
	public Mat imagePreProcessing(Mat mImageFrame) {

		Imgproc.resize(mImageFrame, mImageFrame, new Size(mImageFrame.cols() / RESIZE_NUMBER, mImageFrame.rows() / RESIZE_NUMBER));
		Mat grayFrame = new Mat();

		Imgproc.cvtColor(mImageFrame, grayFrame, Imgproc.COLOR_RGB2GRAY);

		return grayFrame;
	}

	@Override
	public Mat adaptiveThreshold(Mat mGrayFrame) {
		Mat thresholdFrame = new Mat();
		int kernelSize = 3; //docs recommend 3, 5, 7
		int minusValue = 12; //docs recommend values 10 ~ 12
		Imgproc.adaptiveThreshold(mGrayFrame, thresholdFrame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C
				, Imgproc.THRESH_BINARY_INV, kernelSize, minusValue);

		return thresholdFrame;
	}

	@Override
	public Mat findContours(Mat origin, Mat mImageFrame) {

		Imgproc.resize(origin, origin, new Size(origin.cols() / RESIZE_NUMBER, origin.rows() / RESIZE_NUMBER));
		Mat originalImage = origin.clone();

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		//모폴로지 클로즈 연산(팽창 후 침식)
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
		Imgproc.morphologyEx(mImageFrame, mImageFrame, Imgproc.MORPH_CLOSE, kernel);

		//findContours 에서 세로가 길어서 크게 잡히지 않게 미리 제거
		mImageFrame = removeVerticalLines(mImageFrame, 150);

		Imgproc.findContours(mImageFrame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		if (contours.size() > 0) {
			for (int i = 0; i < contours.size(); i++) {
				Rect rect = Imgproc.boundingRect(contours.get(i));
				if (rect.height > 5 && rect.width > 5 && !(rect.width >= 512 - 5 && rect.height >= 512 - 5)) {
					//find word
					Imgproc.rectangle(origin, new Point(rect.br().x - rect.width -10, rect.br().y - rect.height - 10)
							, rect.br(), new Scalar(255, 0, 0), 3);

					cropImage(originalImage, rect);

					Toast.makeText(mContext, mContext.getString(R.string.find_words), Toast.LENGTH_SHORT).show();

				}
			}

		} else {
			Toast.makeText(mContext, mContext.getString(R.string.empty_contours), Toast.LENGTH_SHORT).show();

		}

		return origin;
	}

	private void cropImage(Mat origin, Rect rect) {
		Mat data = origin.submat(rect);
		saveJpgFile(data, CROP_IMAGE);
	}

	@Override
	public void saveJpgFile(Mat image) {
		saveJpgFile(image, null);
	}


	public void saveJpgFile(Mat image, @Nullable Integer number) {

		//auto boxing
		if(number != null && number==CROP_IMAGE) {
			Imgcodecs.imwrite(AppApplication.getPicExternalCropPath() + System.currentTimeMillis() + "CropImage.jpg", image);
		} else {
			Imgcodecs.imwrite(AppApplication.getPicExternalPath() + System.currentTimeMillis() + "Picture.jpg", image);
		}

	}

	private Mat removeVerticalLines(Mat thresholdFrame, int limit) {
		Mat lines = new Mat();
		int threshold = 100; // 선 추출 임계값
		int minLength = 80; // 추출한 선의 길이
		int lineGap = 5; //5픽셀 이내로 겹치는 선은 제외
		int rho = 1;

		Imgproc.HoughLinesP(thresholdFrame, lines, rho, Math.PI / 180, threshold, minLength, lineGap);

		for (int i = 0; i < lines.total(); i++) {
			double[] vec = lines.get(i, 0);
			Point pt1, pt2;
			pt1 = new Point(vec[0], vec[1]);
			pt2 = new Point(vec[2], vec[3]);
			double gapY = Math.abs(vec[3] - vec[1]);
			double gapx = Math.abs(vec[2] - vec[0]);
			if (gapY > limit && limit > 0) {
				//세로로 긴 흰 부분 채우기
				Imgproc.line(thresholdFrame, pt1, pt2, new Scalar(0, 0, 0), 10);
			}
		}

		return thresholdFrame;
	}

}
