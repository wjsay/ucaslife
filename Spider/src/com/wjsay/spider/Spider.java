package com.wjsay.spider;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Spider {

    public static void main(String[] args){
        Spider spider = new Spider();
        spider.start();
    }
    public void start() {
        //new Thread(new MyRunnable("2", "3")).start();
        ExecutorService service = Executors.newCachedThreadPool();
        String website = "https://www.woyaogexing.com";
        String surl = "https://www.woyaogexing.com/touxiang/weixin/index_%d.html";
        URL url = null;
        BufferedReader reader = null;
        for (int i = 2; i <= 40; ++i) {
            try {
                url = new URL(String.format(surl, i));
            } catch (MalformedURLException e) {
                Log.write("url execption");
            }
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                Log.write("open stream exception");
            }
            Pattern imagePatter = Pattern.compile("<a href=.*><img");
            Pattern hrefPatter = Pattern.compile("(href=\"\\S*\")");
            Pattern titlePatter = Pattern.compile("(title=\".*\")");
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    Matcher imageMatcher = imagePatter.matcher(line);
                    if (imageMatcher.find()) {
                        Matcher hrefMatcher = hrefPatter.matcher(imageMatcher.group(0));
                        String href = null, title = null;
                        if (hrefMatcher.find()) {
                            href = hrefMatcher.group(0).substring(6, hrefMatcher.group(0).length() - 1);
                        }
                        Matcher titleMatcher = titlePatter.matcher(imageMatcher.group(0));
                        if (titleMatcher.find()) {
                            title = titleMatcher.group(0).substring(7, titleMatcher.group(0).length() - 1);
                        }
                        if (href != null && title != null) {
                            service.execute(new MyRunnable(website + href, title));
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                Log.write("IO exception");
            }
        }
    }

}
