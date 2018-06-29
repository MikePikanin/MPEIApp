package ru.mpei.mpei_pk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkModel extends AsyncTask<String, String, String> {
    //private static final String charset_out = "ISO-8859-1";
    private static final String charset = "UTF-8";

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call
        String data = params[1]; //data to post
        String boundary = params[2]; //boundary of POST multipart

        try {
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setDoOutput(true); //triggers POST
            urlConnection.setDoInput(true);

            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Cache-Control", "no-cache");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(urlConnection.getOutputStream(), charset));

            writer.write(data);
            writer.close();

            BufferedReader reader = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(), charset));

            StringBuilder return_data = new StringBuilder();
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) > 0) {
                return_data.append(new String(buf, 0 , len));
            }

            reader.close();

            urlConnection.connect();

            return return_data.toString();

        } catch (Exception e) {
            Log.e("NetworkModel", e.getMessage());
            return null;
        }
    }
}
