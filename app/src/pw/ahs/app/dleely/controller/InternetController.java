/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static pw.ahs.app.dleely.Globals.APP_HOME;
import static pw.ahs.app.dleely.Globals.getUserAgentString;

public class InternetController {
    private String errorMsg = "";
    //    public static final String HOME_PAGE = "http://localhost/www/app/dleely/index.php";
    private static InternetController instance = null;

    public static InternetController getInstance() {
        if (instance == null)
            instance = new InternetController();
        return instance;
    }

    private InternetController() {
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getLatestVersion() {
        try {
            URL url = new URL(APP_HOME);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            String content = "update=true";
            con.setRequestProperty("Charset", "utf-8");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", "" + content.length());
            con.setRequestProperty("REFERER", getUserAgentString());
            con.setUseCaches(false);
            con.setConnectTimeout(10000);
            con.setReadTimeout(5000);

            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            out.write(content.getBytes());
            out.close();
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                errorMsg = "Not OK!";
                return "";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = br.readLine();
            br.close();

            errorMsg = "";
            // expected pattern: <!-- 1.0.0 -->
            if (inputLine.matches("<!-- .+ -->"))
                return inputLine.substring(5, inputLine.length() - 4);
            else {
                errorMsg = "Invalid response";
                return "";
            }
        } catch (IOException e) {
            errorMsg = e.getLocalizedMessage();
            return "";
        }
    }

    public boolean sendFeedback(String feedback) {
        try {
            URL url = new URL(APP_HOME);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            String content = "feedback=" + URLEncoder.encode(feedback, "UTF-8");
            con.setRequestProperty("Charset", "utf-8");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", "" + content.length());
            con.setRequestProperty("REFERER", getUserAgentString());
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);

            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            out.write(content.getBytes());
            out.close();
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                errorMsg = "";
                return true;
            } else errorMsg = "Not OK!";
        } catch (IOException e) {
            errorMsg = e.getLocalizedMessage();
        }

        return false;
    }
}
