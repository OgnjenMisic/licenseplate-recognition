package zgio.lpr.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

public class CvUtils {
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 *
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.err.println("Cannot convert the Mat object to Image: " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Support for the {@link#mat2image()} method
	 *
	 * @param original
	 *            the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
	 */
	public static BufferedImage matToBufferedImage(Mat original) {
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	/**
	 * Load an image resource from path and return it as BufferedImage.
	 * @param path
	 * @return
	 */
	public static BufferedImage loadImage(String path) {
		try {
			InputStream resourceAsStream = CvUtils.class.getResourceAsStream(path);
			return ImageIO.read(resourceAsStream);
		} catch (Exception e) {
			System.err.println("Could not find image file on path: " + path);
//			e.printStackTrace();
		}
		return null;
	}

	/**k
	 * Convert a BufferedImage to opencv.Mat
	 * @param image
	 * @return9
	 */
	public static Mat imageToMat(BufferedImage image) {
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		final byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}
}
