package com.example.ccsecuritysolutions;


import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;



public abstract class ImageProcessing {

    public static final int A = 0;
    public static final int R = 1;
    public static final int G = 2;
    public static final int B = 3;
    
    private ImageProcessing() {
    }

    /**
     * Get RGB values from pixel.
     * 
     * @param pixel
     *            Integer representation of a pixel.
     * @return float array of a,r,g,b values.
     */
    public static float[] getARGB(int pixel) {
        int a = (pixel >> 24) & 0xff;
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return (new float[] { a, r, g, b });
    }
    /**
     * Decode a YUV420SP image to RGB.
     * 
     * @param yuv420sp
     *            Byte array representing a YUV420SP image.
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @return Integer array representing the RGB image.
     * @throws NullPointerException
     *             if yuv420sp byte array is NULL.
     */
    public static int[] decodeYUV420SPtoRGB(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) throw new NullPointerException();

        final int frameSize = width * height;
        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & (yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }
}

