package com;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * The tool to adding watermark to images,recommended markImage () methods to create watermark image.
 *
 *
 */
public class ImageWatermark {
	private static final int OFFSET_X = 10;
	private static final int OFFSET_Y = 10;

	public static final int MARK_LEFT_TOP = 1;
	public static final int MARK_RIGHT_TOP = 2;
	public static final int MARK_CENTER = 3;
	public static final int MARK_LEFT_BOTTOM = 4;
	public static final int MARK_RIGHT_BOTTOM = 5;

	/**
	 * add a text to an image. as a single color, the effect is rather poor.
	 * @param srcImg
	 * @param text
	 * @param font
	 * @param color
	 * @param offset_x
	 * @param offset_y
	 */
	public static void markText(String srcImg,String text, Font font, Color color, int offset_x, int offset_y) {
		try {
			File _file = new File(srcImg);
			Image src = ImageIO.read(_file);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);
			//    g.setBackground(Color.white);
			g.setColor(color);
			g.setFont(font);

			g.drawString(text,  offset_x, height - font.getSize() / 2 - offset_y);
			g.dispose();

			FileOutputStream out = new FileOutputStream(srcImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * A logo picture to picture and watermark, effective this way
	 * one instance : http://www.mvgod.com/images/poster/NaNiYaChuanQi2XKaiSiBinWangZi-10285-12811-19978-13383/3.jpg
	 * @param srcImg --
	 *          source image
	 * @param markImg --
	 *          watermark logo image
	 * @param alpha --
	 *          alpha composite  0 - 1,　0 Full transparency, 1 Opaque
	 * @param mark_position --
	 *          Watermark position, the four corners and central respectively,
	 *       and so constant that ImageWatermark.MARK_LEFT_TOP
	 */
	public final static void markImage(String srcImg, String markImg, float alpha, int mark_position) {
		try {
			File _file = new File(srcImg);
			if(!_file.exists()) return;

			Image src = ImageIO.read(_file);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);

			// watermark image file
			File markFile = new File(markImg);
			if(!markFile.exists()) return;

			Image mark_img = ImageIO.read(markFile);
			int mark_img_width = mark_img.getWidth(null);
			int mark_img_height = mark_img.getHeight(null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			switch (mark_position) {
			case ImageWatermark.MARK_LEFT_TOP:
				g.drawImage(mark_img, OFFSET_X, OFFSET_Y, mark_img_width, mark_img_height, null);
				break;
			case ImageWatermark.MARK_LEFT_BOTTOM:
				g.drawImage(mark_img, OFFSET_X, (height - mark_img_height - OFFSET_Y), mark_img_width, mark_img_height, null);
				break;
			case ImageWatermark.MARK_CENTER:
				g.drawImage(mark_img, (width - mark_img_width - OFFSET_X) / 2, (height - mark_img_height - OFFSET_Y) / 2, mark_img_width, mark_img_height, null);
				break;
			case ImageWatermark.MARK_RIGHT_TOP:
				g.drawImage(mark_img, (width - mark_img_width - OFFSET_X), OFFSET_Y, mark_img_width, mark_img_height, null);
				break;
			case ImageWatermark.MARK_RIGHT_BOTTOM:
			default:
				g.drawImage(mark_img, (width - mark_img_width - OFFSET_X), (height - mark_img_height - OFFSET_Y),mark_img_width, mark_img_height, null);
			}

			g.dispose();
			FileOutputStream out = new FileOutputStream(srcImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch(java.lang.OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void markImage(String srcImg, String markImg, float alpha) {
		markImage(srcImg, markImg, alpha, ImageWatermark.MARK_RIGHT_BOTTOM);
	}

	public static void markImage( String srcImg, String markImg) {
		markImage(srcImg, markImg, 0.5f, ImageWatermark.MARK_RIGHT_BOTTOM);
	}

	public static void markImageRandomPos(String srcImg,String markImg,  float alpha) {
		int[] a = { ImageWatermark.MARK_LEFT_TOP, ImageWatermark.MARK_RIGHT_TOP, ImageWatermark.MARK_LEFT_TOP, ImageWatermark.MARK_LEFT_BOTTOM,
				ImageWatermark.MARK_RIGHT_BOTTOM, ImageWatermark.MARK_RIGHT_BOTTOM, ImageWatermark.MARK_CENTER };

		int i =  new Random().nextInt(a.length);
		markImage(srcImg, markImg, alpha, a[i]);
	}

	/**
	 * Semi-transparent, random location imprint. 
	 * With a slightly higher risk of lower right corner of the upper left corner, the central minimum risk.
	 * @param srcImg
	 * @param markImg
	 */
	public static void markImageRandomPos(String srcImg,String markImg) {
		markImageRandomPos(srcImg, markImg, 0.5f);
	}
}