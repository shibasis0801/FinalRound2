package com.example.overlord.testing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.overlord.testing.immutable.Consumer;
import com.example.overlord.testing.model.Fused;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Consumer<ArrayList<Fused>> onNewSignalsListener;
    private MyService service;
    private Boolean isBound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((MyService.LocalBinder) iBinder).getService();
            isBound = true;
            service.setOnNewSignalsListener(onNewSignalsListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            service.setOnNewSignalsListener(null);
        }
    };

    public ArrayList<LineGraphSeries<DataPoint>> createQuadSeries(ArrayList<Fused> data) {
        return null;
    }

    public void doGraphStuff(ArrayList<Fused> data) {
        GraphView graph = findViewById(R.id.graph_heart);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0),
                new DataPoint(1, 1),
                new DataPoint(2, 2),
                new DataPoint(3, 3),
                new DataPoint(4, 4),
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });

        Log.d("InActivity", data.toString());

        graph.addSeries(series2);
    }

    public void predict(ArrayList<Fused> data) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onNewSignalsListener = data -> {
            doGraphStuff(data);
            predict(data);
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }
}
