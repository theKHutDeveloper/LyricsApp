package com.developments.knowledgehut.lyricsapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText artist, song;
    TextView lyrics;

    public void getLyrics(View view){
        Log.i("artist", artist.getText().toString());
        Log.i("song", song.getText().toString());
        Log.i("Url", "https://api.lyrics.ovh/v1/" + artist.getText().toString() + "/" + song.getText().toString());

        InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(song.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(artist.getWindowToken(), 0);

        if (artist.getText().toString().length() == 0 || song.getText().toString().length() == 0) {

            Log.i("artist length", String.valueOf(artist.getText().length()));
            Log.i("song length", String.valueOf(song.getText().length()));

            Toast.makeText(getApplicationContext(), "You must enter artist name and song title", Toast.LENGTH_LONG).show();

        } else {

            try {

                String artistText = removeSpaceAtEndOfText(artist.getText().toString());
                String songText = removeSpaceAtEndOfText(song.getText().toString());

                Log.i("artist text", artistText);
                Log.i("song text", songText);

                String encodedArtist = URLEncoder.encode(artistText, "UTF-8").replace("+", "%20");
                String encodedSong = URLEncoder.encode(songText, "UTF-8").replace("+", "%20");

                DownloadTask task = new DownloadTask();
                task.execute("https://api.lyrics.ovh/v1/" + encodedArtist + "/" + encodedSong);
                Log.i("Url", "https://api.lyrics.ovh/v1/" + encodedArtist + "/" + encodedSong);

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), "Could not download lyrics!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String removeSpaceAtEndOfText(String s) {
        String amended = s;

        if (String.valueOf(amended.charAt(amended.length() - 1)).equals(" ")) {
            amended = amended.substring(0, amended.length() - 1);
        }

        return amended;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        artist = (EditText)findViewById(R.id.artist);
        song = (EditText)findViewById(R.id.song);
        lyrics = (TextView)findViewById(R.id.lyrics);
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpsURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1){

                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    String lyric = jsonObject.getString("lyrics");

                    if (lyric != null && lyric.length() > 0) {
                        Log.i("lyrics", String.valueOf(lyric));
                        artist.getText().clear();
                        song.getText().clear();

                        lyrics.setMovementMethod(new ScrollingMovementMethod());
                        lyrics.setText(lyric);
                        lyrics.setVisibility(View.VISIBLE);

                    } else {

                        showToast();
                    }
                } else {

                    showToast();
                }

            } catch (JSONException e) {

                showToast();
            }

        }

        private void showToast(){
            Toast.makeText(getApplicationContext(), "Lyrics could not be found", Toast.LENGTH_LONG).show();
        }
    }
}
