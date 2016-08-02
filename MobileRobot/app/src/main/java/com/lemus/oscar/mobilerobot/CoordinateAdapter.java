package com.lemus.oscar.mobilerobot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by olemu on 28/07/2016.
 */
public class CoordinateAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<Coordinate> datos;


    public CoordinateAdapter(Context context, ArrayList datos) {
        super(context, R.layout.coordinate, datos);
        this.context = context;
        this.datos = datos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // En primer lugar "inflamos" una nueva vista, que será la que se
        // mostrará en la celda del ListView. Para ello primero creamos el
        // inflater, y después inflamos la vista.
        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.coordinate, null);

        // A partir de la vista, recogeremos los controles que contiene para
        // poder manipularlos.
        // Recogemos el ImageView y le asignamos una foto.
        TextView tvLongitude = (TextView) item.findViewById(R.id.tvLongitude);
        TextView tvLatitude = (TextView) item.findViewById(R.id.tvLatitude);

        tvLongitude.setText(datos.get(position).getLongitude());
        tvLatitude.setText(datos.get(position).getLatitude());

        // Devolvemos la vista para que se muestre en el ListView.
        return item;
    }

}
