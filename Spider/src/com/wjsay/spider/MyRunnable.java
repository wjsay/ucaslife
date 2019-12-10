package com.wjsay.spider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyRunnable implements Runnable {
    String url;
    String title;
    MyRunnable(String url, String title) {
        this.url = url;
        this.title = title;
    }
    @Override
    public void run() {
        Pattern imagePattern = Pattern.compile("(<img class=\"lazy\" src=\"//.+?\">?)");
        URL curl = null;
        BufferedReader reader = null;
        String imgURL = null;
        String filepath = null;
        try {
            curl = new URL(url);
            reader = new BufferedReader(new InputStreamReader(curl.openStream()));
            //reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("tmp.html"))));
        } catch (IOException e) {
            Log.write("IO Exception: " + url);
            return;
        }
        try {
            String line = null;
            int st = "<img class=\"lazy\" src=\"//".length();
            //int ed = "\" width=\"200\" height=\"200\">".length();
            while ((line = reader.readLine()) != null) {
                Matcher imageMatcher = imagePattern.matcher(line);
                File path = new File("image");
                if (!path.exists()) {
                    path.mkdir();
                }
                path = new File(path.getAbsolutePath() + "/" + title);
                if (!path.exists()) {
                    path.mkdir();
                }
                int i = 0;
                while(imageMatcher.find()) {
                    String tmp = imageMatcher.group();
                    imgURL = tmp.substring(st, tmp.length() - 1);
                    BufferedImage image = null;
                    try {
                        image = ImageIO.read(new URL("https://" + imgURL));
                    } catch (IOException e) {
                        Log.write("Image exception: " + "https://" + imgURL);
                    }
                    try {
                        filepath = path.getAbsolutePath() + "/" + i++ + ".jpeg";
                        ImageIO.write(image, "jpeg", new File(filepath));
                    } catch (IOException e) {
                        Log.write("can't open : " + imgURL + "\t" + filepath);
                    }
                }

            }
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            Log.write("can't connect to:" + imgURL);
        } catch (IOException e) {
            Log.write("读入数据流异常");
        }
    }
}