package com.cf.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cf.popularmovies.model.Result;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ResultAdapter extends ArrayAdapter<Result> {

    Context context;
    int layoutRessource;
    List<Result> data = null;

    public ResultAdapter(Context context, int resource, List<Result> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutRessource = resource;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ResultHolder resultHolder = null;

        if (view == null) {
            LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
            view = layoutInflater.inflate(layoutRessource, parent, false);

            resultHolder = new ResultHolder();
            resultHolder.imageView_result = (ImageView)view.findViewById(R.id.ImageView_movie_poster);
            resultHolder.textView_result = (TextView)view.findViewById(R.id.textView_movie_poster);

            view.setTag(resultHolder);
        }
        else
        {
            resultHolder = (ResultHolder)view.getTag();
        }

        Result result = data.get(position);

        if (result.getPosterPath() != null)
        {
            Picasso.with(context)
                    .load(getImageURL(result.getPosterPath()))
                    .into(resultHolder.imageView_result);
        } else {
            Picasso.with(context)
                    .load(R.drawable.placeholder)
                    .into(resultHolder.imageView_result);
        }

        resultHolder.textView_result.setText(result.getTitle());

        return view;
    }

    static class ResultHolder {
        ImageView imageView_result;
        TextView textView_result;
    }

    private String getImageURL(String posterPath) {

        final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w185";

        return IMAGE_BASE_URL + IMAGE_SIZE + posterPath;
    }
}