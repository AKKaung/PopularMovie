package com.udacity.popularmovie.ui.movie_detail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.udacity.popularmovie.data.database.MovieTrailerResult;

public class MovieTrailerHandler {
    public Context mContext;

    public MovieTrailerHandler(Context context) {
        this.mContext = context;
    }

    public void onMovieTrailerClicked(MovieTrailerResult movieTrailerResult) {
        if (mContext != null) {
            watchYoutubeVideo(mContext, movieTrailerResult.key);
        }
    }

    /**
     * https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
     *
     * @param context
     * @param id
     */
    public static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }
}
