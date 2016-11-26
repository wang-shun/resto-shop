package com.resto.shop.web.util;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by carl on 2016/11/25.
 */
public class ImageUtil {

    public static String imageBytesScale(BufferedImage image, String systemPath, String filePath) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int cha = width - height;
        double scale; // 压缩比
        int scaledW = 0; // 新压缩后宽度
        int scaledH = 0; // 新压缩后高度
        BufferedImage outputImage = null;
        if (cha > 0) {
            if (width > 512) {
                scale = (double) 512 / (double) width;
                scaledW = (int) (scale * width); // 新压缩后宽度
                scaledH = (int) (scale * height); // 新压缩后高度
            }
        } else {
            if (height > 512) {
                scale = (double) 512 / (double) height;
                scaledW = (int) (scale * width); // 新压缩后宽度
                scaledH = (int) (scale * height); // 新压缩后高度
            }
        }
//        Image img = image.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
        if (scaledW == 0) {
            scaledW = width;
            scaledH = height;
        }
        int cha1 = scaledW - scaledH;
        int cha2 = scaledH - scaledW;

        int endWH = 0;
        if(scaledW < scaledH){
            endWH = scaledW;
        }else{
            endWH = scaledH;
        }
        outputImage = new BufferedImage(endWH, endWH, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) outputImage.getGraphics();
        //截图
        if (scaledW < scaledH) {
            image = image.getSubimage(0, cha2 / 2, endWH, endWH);
        } else if (scaledW > scaledH) {
            image = image.getSubimage(cha1 / 2, 0, endWH, endWH);
        }
        graphics.drawImage(image, 0, 0, endWH, endWH, null);
        graphics.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean flag = ImageIO.write(outputImage, "jpg", out);
        byte[] bytes = out.toByteArray();
        String uuId = UUID.randomUUID().toString() + ".jpg";
        String savePath = systemPath + filePath + uuId;
        if (bytes.length < 3 || savePath.equals("")) {
            return null;
        }
        File file = new File(savePath);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes, 0, bytes.length);
        fos.flush();
        fos.close();
        return filePath + uuId;
    }
}