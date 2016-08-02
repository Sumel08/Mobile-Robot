package com.lemus.oscar.mobilerobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

public class MRMainActivity extends AppCompatActivity {


    private String aux;

    private TextView tvTitle, tvFront, tvLeft, tvRight, tvBack;
    private ArrayList<Coordinate> datos;
    private CoordinateAdapter coordinateAdapter;
    private ListView lvCoordinate;
    private ImageView ivCar;
    private CoordinateData cdb;
    private EditText etFileName;
    Handler bluetoothIn;

    final int handlerState = 0;             //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrmain);

        tvTitle = (TextView) findViewById(R.id.titleTest);

        tvFront = (TextView) findViewById(R.id.tvFront);
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        tvBack = (TextView) findViewById(R.id.tvBack);

        ivCar = (ImageView) findViewById(R.id.ivCar);

        etFileName = (EditText) findViewById(R.id.etFileName);

        cdb = new CoordinateData(getApplicationContext());

        datos = new ArrayList<>();

        datos.add(new Coordinate("0, 0", "0, 0", "Latitude", "Longitude"));
        /*datos.add(new Coordinate("US", "US", "16.742773333", "-93.146318333"));
        datos.add(new Coordinate("China", "China", "16.743121667", "-93.14642"));
        datos.add(new Coordinate("United Kingdom", "United Kingdom", "16.743115", "-93.146425"));
        datos.add(new Coordinate("Germany", "Germany", "16.743461667", "-93.146725"));
        datos.add(new Coordinate("Korea", "Korea", "16.743461667", "-93.146725"));
        datos.add(new Coordinate("India", "India", "16.743325", "-93.145816667"));
        datos.add(new Coordinate("Russia", "Russia", "16.743106667", "-93.145668333"));
        datos.add(new Coordinate("France", "France", "16.743093333", "-93.145685"));
        datos.add(new Coordinate("Canada", "Canada", "16.743056667", "-93.145743333"));

        datos.add(new Coordinate("Canada", "Canada", "16.743051667", "-93.14576"));
        datos.add(new Coordinate("Canada", "Canada", "16.743031667", "-93.145788333"));
        datos.add(new Coordinate("Canada", "Canada", "16.743028333", "-93.145806667"));
        datos.add(new Coordinate("Canada", "Canada", "16.74302", "-93.14584666"));
        datos.add(new Coordinate("Canada", "Canada", "16.743016667", "-93.145866667"));
        datos.add(new Coordinate("Canada", "Canada", "16.743011667", "-93.145901667"));
        datos.add(new Coordinate("Canada", "Canada", "16.743015", "-93.145911667"));*/


        coordinateAdapter = new CoordinateAdapter(getApplicationContext(), datos);

        lvCoordinate = (ListView) findViewById(R.id.lvCoordinates);
        lvCoordinate.setAdapter(coordinateAdapter);

        aux = "";


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);              //keep appending to string until ~

                    //tvTitle.setText(aux);

                    if (aux.lastIndexOf("?") > aux.lastIndexOf("+")) {

                        String valueSens = aux.substring(aux.lastIndexOf("+")+1, aux.lastIndexOf("?"));
                        //tvTitle.setText(String.valueOf(aux));
                        int dir = Integer.parseInt(valueSens);

                        switch (dir) {
                            case 0:
                                tvFront.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvLeft.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvRight.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvBack.setBackgroundColor(getResources().getColor(R.color.noActive));
                                break;
                            case 1:
                                tvFront.setBackgroundColor(getResources().getColor(R.color.active));
                                tvLeft.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvRight.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvBack.setBackgroundColor(getResources().getColor(R.color.noActive));
                                break;
                            case 2:
                                tvFront.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvLeft.setBackgroundColor(getResources().getColor(R.color.active));
                                tvRight.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvBack.setBackgroundColor(getResources().getColor(R.color.noActive));
                                break;
                            case 3:
                                tvFront.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvLeft.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvRight.setBackgroundColor(getResources().getColor(R.color.active));
                                tvBack.setBackgroundColor(getResources().getColor(R.color.noActive));
                                break;
                            case 4:
                                tvFront.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvLeft.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvRight.setBackgroundColor(getResources().getColor(R.color.noActive));
                                tvBack.setBackgroundColor(getResources().getColor(R.color.active));
                                break;
                            default:
                                break;
                        }
                    }

                    if (aux.lastIndexOf("}") > aux.lastIndexOf("{")) {
                        String auxCoor = aux.substring(aux.lastIndexOf("{")+1, aux.lastIndexOf("}")-1);
                        String[] fields = auxCoor.split(",");
                        double latDe = Double.parseDouble(fields[0].substring(0, fields[0].length()-1));
                        double lonDe = Double.parseDouble(fields[1].substring(0, fields[1].length()-2));

                        int latDeg = (int) latDe/100;
                        double latMin = latDe - latDeg*100;
                        double latitude = latDeg + latMin/60;
                        if (fields[0].contains("S"))
                            latitude = -latitude;

                        int lonDeg = (int) lonDe/100;
                        double lonMin = lonDe - lonDeg*100;
                        double longitude = lonDeg + lonMin/60;
                        if (fields[1].contains("W"))
                            longitude = -longitude;

                        datos.add(new Coordinate("uno", "dos", String.valueOf(latitude), String.valueOf(longitude)));

                        cdb.getWritableDatabase().execSQL("INSERT INTO coordinateTable VALUES(" + String.valueOf(latitude) + "," + String.valueOf(longitude) +")");

                        coordinateAdapter = new CoordinateAdapter(getApplicationContext(), datos);
                        lvCoordinate.setAdapter(coordinateAdapter);
                        lvCoordinate.setSelection(datos.size()-1);



                      // tvTitle.setText(String.valueOf(longitude));
                        //tvTitle.setText(dataRaw + dataRaw2);
                        aux = "";
                    }

                    if (aux.length() > 200)
                        aux = "";

                    aux = aux + recDataString.substring(0);
                    //tvTitle.setText(aux);
                    recDataString.delete(0, recDataString.length());
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    public void loadDB(View vie) {
        Cursor cursor = cdb.getReadableDatabase().rawQuery("SELECT * FROM coordinateTable", null);
        cursor.moveToFirst();
        String aux = "";
        datos.clear();
        datos.add(new Coordinate("0, 0", "0, 0", "Latitude", "Longitude"));
        while(cursor.moveToNext()) {
            datos.add(new Coordinate("l", "k", cursor.getString(0), cursor.getString(1)));
            coordinateAdapter = new CoordinateAdapter(getApplicationContext(), datos);
            lvCoordinate.setAdapter(coordinateAdapter);
            lvCoordinate.setSelection(datos.size()-1);
        }


    }

    public void eraseDB(View view) {
        cdb.getWritableDatabase().execSQL("delete from coordinateTable");
        datos.clear();
        datos.add(new Coordinate("0, 0", "0, 0", "Latitude", "Longitude"));
        lvCoordinate.setAdapter(coordinateAdapter);
        lvCoordinate.setSelection(datos.size()-1);
    }

    public void saveFile(View view) {
        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "Sí está", Toast.LENGTH_SHORT).show();
        }

        try
        {
            File ruta_sd = Environment.getExternalStorageDirectory();

            File f = new File(ruta_sd.getAbsolutePath(), etFileName.getText().toString() + ".txt");

            Toast.makeText(getApplicationContext(), ruta_sd.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            OutputStreamWriter fout =
                    new OutputStreamWriter(
                            new FileOutputStream(f));

            for (Coordinate coordinate: datos) {
                fout.write(coordinate.getLatitude() + "," + coordinate.getLongitude() + "\n");
            }
            fout.close();

            Toast.makeText(getApplicationContext(), "Se guardó con éxito", Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a tarjeta SD");
            Toast.makeText(getApplicationContext(), "Algo salió mal", Toast.LENGTH_SHORT).show();
        }
    }

    public void plotMap(View view) {

        if (datos.size()>1) {

            ArrayList<String> longitudeAL = new ArrayList<>();
            ArrayList<String> latitudeAL = new ArrayList<>();

            for (int i=1; i<datos.size(); i++) {
                latitudeAL.add(datos.get(i).getLatitude());
                longitudeAL.add(datos.get(i).getLongitude());


            }

            Intent mapIntent = new Intent(this, ViewMap.class);
            Bundle extras = new Bundle();
            extras.putStringArrayList("longitude", longitudeAL);
            extras.putStringArrayList("latitude", latitudeAL);
            mapIntent.putExtras(extras);
            startActivity(mapIntent);
        }
        else {
            Toast.makeText(getApplicationContext(), "No hay datos", Toast.LENGTH_LONG).show();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListMR.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
