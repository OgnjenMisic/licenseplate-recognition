package zgio.lpr.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import zgio.lpr.utils.CvUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ALPRCtrl {

	private static final Size KSIZE_11 = new Size(1, 1);
	private static final Size KSIZE_33 = new Size(3, 3);
	private static final Size KSIZE_55 = new Size(5, 5);
	private static final Size KSIZE_77 = new Size(7, 7);
	private static final Size KSIZE_1111 = new Size(11, 11);
	private static final Point CENTER_POINT = new Point(0, 0);
	private static final Scalar SCALAR_RED = new Scalar(0, 0, 255);
	private static final Scalar SCALAR_GREEN = new Scalar(0, 255, 0);

	private static final Mat STRUCT_ELEMENT_11 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, KSIZE_11);
	private static final Mat STRUCT_ELEMENT_33 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, KSIZE_33);
	private static final Mat STRUCT_ELEMENT_55 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, KSIZE_55);
	private static final Mat STRUCT_ELEMENT_77 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, KSIZE_77);

	@FXML
	private ImageView imagePanel1;
	@FXML
	private ImageView imagePanel2;
	@FXML
	private ImageView imagePanel3;
	@FXML
	private ImageView imagePanel4;

	public void init() throws TesseractException {
		/** load image */
		BufferedImage bufferedImage = CvUtils.loadImage("/images/lp2.png");    // 1, 2, 4
		Mat imageMat = CvUtils.imageToMat(bufferedImage);
		Mat grayMat = new Mat();

		/** preprocess in order to locate the region of interest - licence plate */
		Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(grayMat, grayMat, KSIZE_55, 0);
		Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 55, 2);
		Mat morph = new Mat();
		Imgproc.morphologyEx(grayMat, morph, Imgproc.MORPH_CLOSE, STRUCT_ELEMENT_55);

		/** find contours of thresholded image */
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(morph, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		List<Rect> possiblePlates = new ArrayList<>();
		Mat imgClone = imageMat.clone();
		for (MatOfPoint mop : contours) {
			Rect rect = Imgproc.boundingRect(mop);
			/** area size and aspect ratio of the license plate */
			if (rect.area() > 4000) {
				if (rect.width > rect.height * 3.5 && rect.width < rect.height * 5) {
					possiblePlates.add(rect);
					Imgproc.rectangle(imgClone, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), SCALAR_GREEN, 2);
				}
			}
		}
		/** process the plate region itself */
		Mat plateMat = new Mat(imgClone, possiblePlates.get(0)); // working with one rect only atm, no robustness
		Mat grayPlateMat = new Mat();
		Imgproc.cvtColor(plateMat, grayPlateMat, Imgproc.COLOR_BGR2GRAY);
//				Imgproc.adaptiveThreshold(grayPlateMat, grayPlateMat, 200, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 5);
		Imgproc.threshold(grayPlateMat, grayPlateMat, 150, 255, Imgproc.THRESH_BINARY);
//		Imgproc.morphologyEx(grayPlateMat, grayPlateMat, Imgproc.MORPH_OPEN, STRUCT_ELEMENT_33);
//		Imgproc.GaussianBlur(grayPlateMat, grayPlateMat, KSIZE_33, 0);
		Mat equalizedMat = new Mat();
		Imgproc.equalizeHist(grayPlateMat, equalizedMat);
		List<MatOfPoint> nonEqualizedContours = new ArrayList<>();
		List<MatOfPoint> equalizedContours = new ArrayList<>();
		Imgproc.findContours(grayPlateMat, nonEqualizedContours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(equalizedMat, equalizedContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		List<Rect> listOfLetterRects = new ArrayList<>();
		for(MatOfPoint mop : nonEqualizedContours) {
			Rect rect = Imgproc.boundingRect(mop);
			if(rect.height > rect.width * 1.2 && rect.area() > 200) {
				Imgproc.rectangle(plateMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), SCALAR_GREEN);
				listOfLetterRects.add(rect);
			}
		}
		ITesseract tesseract = new Tesseract();
		tesseract.setDatapath(System.getenv("TESSDATA_PREFIX"));
		StringBuilder sb = new StringBuilder();
		for(Rect letterRect : listOfLetterRects){
			Mat letter = new Mat(grayPlateMat, letterRect);
			BufferedImage bfImg = CvUtils.matToBufferedImage(letter);
			imagePanel4.setImage(CvUtils.mat2Image(letter));
			sb.append(tesseract.doOCR(bfImg).trim());
		}
		System.out.println(sb.reverse().toString());

		for(MatOfPoint mop : equalizedContours) {
			Rect rect = Imgproc.boundingRect(mop);
			Imgproc.rectangle(plateMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), SCALAR_GREEN);
		}
 //		Mat nonEqualizedEdges = new Mat();
//		Mat equalizedEdges = new Mat();
//		Imgproc.Canny(grayPlateMat, nonEqualizedEdges, 1, 255);
//		Imgproc.Canny(equalizedMat, equalizedEdges, 1, 255);
		imagePanel1.setImage(CvUtils.mat2Image(plateMat));
		imagePanel2.setImage(CvUtils.mat2Image(grayMat));
		imagePanel3.setImage(CvUtils.mat2Image(morph));
		imagePanel4.setImage(CvUtils.mat2Image(imgClone));
	}

	public void setClosed() {
		this.imagePanel1.setImage(null);
		this.imagePanel2.setImage(null);
	}

}
