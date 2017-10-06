package zgio.lpr.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.collections.Buffer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import zgio.lpr.utils.CvUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LprController {
	@FXML
	private ImageView imagePanel1;
	@FXML
	private ImageView imagePanel2;

	public void init() {
		BufferedImage bufferedImage = CvUtils.loadImage("/images/plate1.jpg");

		Mat imageMat = CvUtils.imageToMat(bufferedImage);
		Mat grayImage = new Mat();
		Imgproc.cvtColor(imageMat, grayImage, Imgproc.COLOR_BGR2GRAY);
		Mat blurImage = new Mat();
		Imgproc.GaussianBlur(grayImage, blurImage, new Size(5, 5), 0);
		Mat edgeImage = new Mat();
		Imgproc.Sobel(blurImage, edgeImage, -1, 1, 0);
		Mat thresh = new Mat();
		Imgproc.threshold(edgeImage, thresh, 50, 255, Imgproc.THRESH_BINARY);
		Mat erodeImage = new Mat();
		Mat dilateImage = new Mat();
		Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 11));
		Imgproc.dilate(thresh, dilateImage, structuringElement);
		Imgproc.erode(dilateImage, erodeImage, structuringElement);
		List<MatOfPoint> contours = new ArrayList<>();
		Mat contourHierarchy = new Mat();
		Imgproc.findContours(erodeImage, contours, contourHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat imageClone = imageMat.clone();

		// TODO: rotadedrect from Imgproc.minAreaRect if the image is angled
		for (int i = 0; i < contours.size(); i++) {
			Rect rect = Imgproc.boundingRect(contours.get(i));
			if (rect.width > rect.height && rect.height * 2 < rect.width && rect.area() > 1800) {
				Imgproc.rectangle(imageClone, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
			}
		}

		//					Mat adaptiveThreshImage = new Mat();
		//					Imgproc.adaptiveThreshold(edgeImage, adaptiveThreshImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 4);
		//		Imgproc.Canny(thresh, edgeImage, 70, 210);
		//			Mat threshImage = new Mat();
		//			Imgproc.threshold(edgeImage, threshImage, 155, 255, Imgproc.THRESH_BINARY);
		//		Imgproc.drawContours(imageClone, contours, -1, new Scalar(0, 255, 0), 1);


		/** OCR */
		BufferedImage bfimg = SwingFXUtils.fromFXImage(CvUtils.mat2Image(imageClone), null);
//		ITesseract tesseract = new Tesseract();
//		tesseract.setLanguage("eng");
//		tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUWXYZ0123456789");
//		try {
//			String str = tesseract.doOCR(bfimg);
//			System.out.println(str);
//		} catch (TesseractException e) {
//			e.printStackTrace();
//		}

		imagePanel1.setImage(CvUtils.mat2Image(erodeImage));
		imagePanel2.setImage(CvUtils.mat2Image(imageClone));

	}

	public void setClosed() {
		this.imagePanel1.setImage(null);
	}
}
