package com.wappkup;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] files;
    private final Integer[] icons;

    public CustomListAdapter(Activity context, String[] files, Integer[] icons) {

        super(context, R.layout.file_list_model, files);

        this.context=context;
        this.files=files;
        this.icons=icons;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.file_list_model, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.iconRowFile);
        TextView extratxt = (TextView) rowView.findViewById(R.id.descrRowFile);

        txtTitle.setText(files[position]);
        imageView.setImageResource(icons[position]);
        extratxt.setText("Descrizione "+files[position]);
        return rowView;

    };
}
