package com.cf.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cf.popularmovies.API.MoviesAPI;
import com.cf.popularmovies.model.Page;
import com.cf.popularmovies.model.Result;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class MovieFragment extends Fragment {

    private static final String KEY_RESULT_DATA = "result_data";

    private GridView gridView;
    private Bundle savedInstanceState;

    private List<Result> result_data = null;
    private ResultAdapter resultAdapter;
    private Boolean isTaskRunning;
    private String sort_by;

    public MovieFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Result data will be stored in ParcelableArrayList in order to prevent another API call
        outState.putParcelableArrayList(KEY_RESULT_DATA, new ArrayList<>(result_data));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_movie, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // If the user changed sort_by preferences get new results
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sort_by = sharedPreferences
                .getString(getString(R.string.preferences_sort_by_key),
                        getString(R.string.preferences_sort_by_default_value));

        if (sort_by != null && !this.sort_by.equals(sort_by)) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sort_by);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView = (GridView) getActivity().findViewById(R.id.gridView_movie_poster);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Get sort_by preference from SharedPreferences
        sort_by = sharedPreferences
                .getString(getString(R.string.preferences_sort_by_key),
                        getString(R.string.preferences_sort_by_default_value));



        // If a InstanceState is present, we don't have to do another API call in order to reduce network traffic
        if (savedInstanceState != null) {
            // Retrieve result data from ParcelableArrayList
            result_data = savedInstanceState.getParcelableArrayList(KEY_RESULT_DATA);

            // Use the custom adapter to the result data visible in the GUI
            resultAdapter = new ResultAdapter(getActivity(), R.layout.item_gridview_movie_poster, result_data);
            gridView.setAdapter(resultAdapter);
        }
        else
        // If no InstanceState is present, start the custom AsyncTask in order to retrieve data from the API
        {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sort_by);
        }

        // If a movie is selected MovieDetailActivity is started and result data is passed by
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Result result = resultAdapter.getItem(position);

                Intent sendResultToMovieDetailActivity = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra("result", result);

                startActivity(sendResultToMovieDetailActivity);
            }
        });

    }

    private class FetchMoviesTask extends AsyncTask<String, Void, List<Result>> {
        private final String API_KEY = getString(R.string.api_key);
        private String api_endpoint = "http://api.themoviedb.org/3";
        private RetrofitError retrofitError = null;

        // In order to avoid multiple AsyncTask we set isTaskRunning true first
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isTaskRunning = true;
        }

        // Use RetroFit to retrieve data from API
        @Override
        protected List<Result> doInBackground(String... params) {

            Page page;

            try {

                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .setEndpoint(api_endpoint)
                        .build();

                MoviesAPI moviesAPI = restAdapter.create(MoviesAPI.class);
                page = moviesAPI.getPages(API_KEY, params[0]);

            } catch (RetrofitError e) {

                retrofitError = e;
                page = new Page();

            }

            return page.getResults();
        }

        // After we retrieved data from API it's time to handle the GUI
        @Override
        protected void onPostExecute(List<Result> results) {
            super.onPostExecute(results);

            result_data = results;
            Snackbar snackbar = null;

            // Use the custom adapter to the result data visible in the GUI
            resultAdapter = new ResultAdapter(getActivity(), R.layout.item_gridview_movie_poster, result_data);
            gridView.setAdapter(resultAdapter);

            // If the API call didn't work out as expected Snackbar will appear
            if (retrofitError != null) {

                snackbar = Snackbar.make(getView(), retrofitError.getMessage(), Snackbar.LENGTH_INDEFINITE);

                snackbar.setAction(getString(R.string.button_reload), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isTaskRunning) {
                            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
                            fetchMoviesTask.execute(sort_by);
                        }

                    }
                });

                snackbar.show();
            }
            else
            {
                if (snackbar != null)
                {
                    // If the API call worked, hide Snackbar if present
                    snackbar.dismiss();
                }
            }

            // AsyncTask is done, so we set isTaskRunning back to false
            isTaskRunning = false;
        }
    }

}
