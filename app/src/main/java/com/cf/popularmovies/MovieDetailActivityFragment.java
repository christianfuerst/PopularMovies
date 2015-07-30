package com.cf.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cf.popularmovies.model.Result;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    private Result result;
    private TextView textView_movie_title;
    private TextView textView_release_date;
    private TextView textView_vote_average;
    private TextView textView_details;
    private ImageView imageView_movie_header;
    private ImageView imageView_movie_poster_small;

    public MovieDetailActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textView_movie_title = (TextView) getActivity().findViewById(R.id.textView_movie_title);
        textView_release_date = (TextView) getActivity().findViewById(R.id.textView_release_date);
        textView_vote_average = (TextView) getActivity().findViewById(R.id.textView_vote_average);
        textView_details = (TextView) getActivity().findViewById(R.id.textView_details);
        imageView_movie_header = (ImageView) getActivity().findViewById(R.id.imageView_movie_header);
        imageView_movie_poster_small = (ImageView) getActivity().findViewById(R.id.imageView_movie_poster_small);

        if (result.getBackdropPath() != null)
        {
            Picasso.with(getActivity().getApplicationContext())
                    .load(getImageURL(result.getBackdropPath()))
                    .into(imageView_movie_header);
        } else {
            Picasso.with(getActivity().getApplicationContext())
                    .load(R.drawable.placeholder)
                    .into(imageView_movie_header);
        }

        if (result.getPosterPath() != null)
        {
            Picasso.with(getActivity().getApplicationContext())
                    .load(getImageURL(result.getPosterPath()))
                    .into(imageView_movie_poster_small);
        } else {
            Picasso.with(getActivity().getApplicationContext())
                    .load(R.drawable.placeholder)
                    .into(imageView_movie_poster_small);
        }

        textView_movie_title.setText(result.getTitle());

        textView_release_date.setText(result.getReleaseDate());

        String vote_average = result.getVoteAverage() + "/10";
        textView_vote_average.setText(vote_average);

        textView_details.setText(result.getOverview());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("result")) {

            result = intent.getParcelableExtra("result");

        }

        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    private String getImageURL(String posterPath) {

        final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w342";

        return IMAGE_BASE_URL + IMAGE_SIZE + posterPath;
    }
}
