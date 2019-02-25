package pack.com.ocr.process;

import org.opencv.core.Mat;

/**
 * Created by kinamare on 2017-10-22.
 */

public interface ImplCameraProcess {

	Mat imagePreProcessing(Mat mImageFrame);
	Mat adaptiveThreshold(Mat mGrayFrame);
	Mat findContours(Mat result, Mat mImageFrame);
	void saveJpgFile(Mat image);

}
