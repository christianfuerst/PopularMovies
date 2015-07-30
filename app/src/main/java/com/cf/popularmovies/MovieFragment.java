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
import android.widget.Button;
import android.widget.GridView;

import com.cf.popularmovies.API.MoviesAPI;
import com.cf.popularmovies.model.Page;
import com.cf.popularmovies.model.Result;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class MovieFragment extends Fragment implements View.OnClickListener {

    private static final String KEY_RESULT_DATA = "result_data";

    private GridView gridView;
    private Bundle savedInstanceState;

    private List<Result> result_data = null;
    private ResultAdapter resultAdapter;
    private String sort_by;
    private Boolean isTaskRunning;

    Button button_reload;

    public MovieFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_RESULT_DATA, new ArrayList<Result>(result_data));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_movie, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sort_by = sharedPreferences
                .getString(getString(R.string.preferences_sort_by_key),
                        getString(R.string.preferences_sort_by_default_value));

        if (this.sort_by != sort_by) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sort_by);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView = (GridView) getActivity().findViewById(R.id.gridView_movie_poster);
        button_reload = (Button) getActivity().findViewById(R.id.button_reload_movie_poster);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        sort_by = sharedPreferences
                .getString(getString(R.string.preferences_sort_by_key),
                        getString(R.string.preferences_sort_by_default_value));

        if (savedInstanceState != null) {
            result_data = savedInstanceState.getParcelableArrayList(KEY_RESULT_DATA);

            resultAdapter = new ResultAdapter(getActivity(), R.layout.item_gridview_movie_poster, result_data);
            gridView.setAdapter(resultAdapter);
        } else {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sort_by);
        }

        button_reload.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reload_movie_poster:

                if (! isTaskRunning) {
                    FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
                    fetchMoviesTask.execute(sort_by);
                }

                break;
        }
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, List<Result>> {
        private final String API_KEY = getString(R.string.api_key);
        private String api_endpoint = "http://api.themoviedb.org/3";
        private RetrofitError retrofitError = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isTaskRunning = true;
        }

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

        @Override
        protected void onPostExecute(List<Result> results) {
            super.onPostExecute(results);

            result_data = results;
            resultAdapter = new ResultAdapter(getActivity(), R.layout.item_gridview_movie_poster, result_data);
            gridView.setAdapter(resultAdapter);

            if (retrofitError != null) {
                Snackbar.make(getView(), retrofitError.getMessage(), Snackbar.LENGTH_LONG).show();
                button_reload.setVisibility(Button.VISIBLE);
            }
            else
            {
                button_reload.setVisibility(Button.GONE);
            }

            isTaskRunning = false;
        }
    }

}
