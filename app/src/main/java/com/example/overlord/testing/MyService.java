package com.example.overlord.testing;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import com.example.overlord.testing.immutable.AccelerometerSensor;
import com.example.overlord.testing.immutable.Consumer;
import com.example.overlord.testing.immutable.TimeUtils;
import com.example.overlord.testing.model.AccelerometerBuffer;
import com.example.overlord.testing.model.Fused;
import com.example.overlord.testing.model.MovingWindowBuffer;
import com.example.overlord.testing.mutable.Singleton;
import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.capabilities.Heartrate;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;

import java.util.ArrayList;


public class MyService extends Service {

    private HardwareConnector connector;
    private final HardwareConnector.Listener listener = new HardwareConnector.Listener() {
        @Override
        public void onHardwareConnectorStateChanged(HardwareConnectorTypes.NetworkType networkType, HardwareConnectorEnums.HardwareConnectorState hardwareConnectorState) {

        }

        @Override
        public void onFirmwareUpdateRequired(SensorConnection sensorConnection, String s, String s1) {

        }
    };
    //BlackBox
    public void startDiscovery(Consumer<Heartrate.Data> onNewHeartBeat) {
        connector.startDiscovery(new DiscoveryListener() {
            @Override
            public void onDeviceDiscovered(final ConnectionParams connectionParams) {
                connector.requestSensorConnection(connectionParams, new SensorConnection.Listener() {
                    @Override
                    public void onNewCapabilityDetected(SensorConnection sensorConnection, Capability.CapabilityType capabilityType) {
                        if (capabilityType == Capability.CapabilityType.Heartrate) {
                            Log.d("h","g");
                            Heartrate heartrate = (Heartrate) sensorConnection.getCurrentCapability(Capability.CapabilityType.Heartrate);
                            heartrate.addListener(new Heartrate.Listener() {
                                @Override
                                public void onHeartrateData(Heartrate.Data data) {
                                    Log.d("HeartRate", data.toString());
                                    onNewHeartBeat.accept(data);
                                }

                                @Override
                                public void onHeartrateDataReset() {

                                }
                            });
                        }

                    }

                    @Override
                    public void onSensorConnectionStateChanged(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionState sensorConnectionState) {

                    }

                    @Override
                    public void onSensorConnectionError(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionError sensorConnectionError) {

                    }
                });
            }

            @Override
            public void onDiscoveredDeviceLost(ConnectionParams connectionParams) {

            }

            @Override
            public void onDiscoveredDeviceRssiChanged(ConnectionParams connectionParams, int i) {

            }
        });
    }

    private Singleton global = Singleton.getInstance();

    private AccelerometerSensor accelerometerSensor;
    private AccelerometerBuffer accelerometerBuffer;
    private MovingWindowBuffer<Fused> windowBuffer;


    public String getTimeLabel(Heartrate.Data data) {
        return data.getTime().format("dd_MM_yyyy_HH_mm_ss");
    }

    public <T> void storeHeartBeat(String time, ArrayList<T> heartBeats) {
        Log.i("StoreHeartBeatAt", time);
        global.heartRef
                .child(time)
                .setValue(heartBeats)
                .addOnFailureListener(exception ->
                        Log.e("Fbase HeartRate Err", exception.getMessage())
                );
    }


    public Double getHeartBeat(Heartrate.Data data) {
        return data.getHeartrate().asEventsPerMinute();
    }

    public void addBeat(Heartrate.Data data) {

        int second = TimeUtils.getSecondsFromTimestamp(data.getTimeMs());

        float []values = accelerometerBuffer.getAverage(second);
        accelerometerBuffer.remove(second);

        Fused dataPoint = new Fused(getHeartBeat(data), values);
        Log.i("DataPoint", dataPoint.toString());

        windowBuffer.add(dataPoint)
                .setOnBufferFullListener(array ->
                        storeHeartBeat(getTimeLabel(data), array)
                );
    }


    @Override
    public void onCreate() {
        super.onCreate();
        connector = new HardwareConnector(this, listener);
        startDiscovery(heartBeat -> Log.d("", heartBeat.toString()));


        windowBuffer = new MovingWindowBuffer<>(60);
        accelerometerBuffer = new AccelerometerBuffer();

        accelerometerSensor = new AccelerometerSensor(this);
/*
        accelerometerSensor.registerListener(
                sensorValues -> {

                    long timestamp = TimeUtils.getTimeStamp();
                    int second = TimeUtils.getSecondsFromTimestamp(timestamp);

                    accelerometerBuffer.add(
                            second,
                            sensorValues
                    );
                }
        );
*/


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connector.shutdown();
    }

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
