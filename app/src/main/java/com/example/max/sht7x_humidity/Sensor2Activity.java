package com.example.max.sht7x_humidity;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import java.text.DecimalFormat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Sensor2Activity extends AppCompatActivity {

    private static final String TAG = "debugger";

    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    class FirebaseObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private SampleDynamicXYDatasource data;
    private DatabaseReference database;
    private FirebaseObservable firebaseNotifier;
    boolean fistTime;
    ArrayList<String> humidityReferences = new ArrayList<>();
    SampleDynamicSeries sine1Series;
    SampleDynamicSeries sine2Series;
    SampleDynamicSeries sine3Series;
    SampleDynamicSeries sine4Series;
    SampleDynamicSeries sine5Series;
    LineAndPointFormatter formatter1;
    LineAndPointFormatter formatter2;
    LineAndPointFormatter formatter3;
    LineAndPointFormatter formatter4;
    LineAndPointFormatter formatter5;
    private boolean[] state = {true, true, true, true, true};

    ValueEventListener myValueEventListener;

    //comment from windows
    //one more comment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor2);

        setTitle(getResources().getString(R.string.plot));

        firebaseNotifier = new FirebaseObservable();

        dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);
        firebaseNotifier.addObserver(plotUpdater);

        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));

        data = new SampleDynamicXYDatasource();

        sine1Series = new SampleDynamicSeries(data, 0, "1");
        sine2Series = new SampleDynamicSeries(data, 1, "2");
        sine3Series = new SampleDynamicSeries(data, 2, "3");
        sine4Series = new SampleDynamicSeries(data, 3, "4");
        sine5Series = new SampleDynamicSeries(data, 4, "5");

        formatter1 = new LineAndPointFormatter(Color.rgb(0, 200, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(10);
        dynamicPlot.addSeries(sine1Series, formatter1);

        formatter2 = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
        formatter2.getLinePaint().setStrokeWidth(10);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        dynamicPlot.addSeries(sine2Series, formatter2);

        formatter3 = new LineAndPointFormatter(Color.rgb(200, 0, 0), null, null, null);
        formatter3.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter3.getLinePaint().setStrokeWidth(10);
        dynamicPlot.addSeries(sine3Series, formatter3);

        formatter4 = new LineAndPointFormatter(Color.rgb(200, 200, 0), null, null, null);
        formatter4.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter4.getLinePaint().setStrokeWidth(10);
        dynamicPlot.addSeries(sine4Series, formatter4);

        formatter5 = new LineAndPointFormatter(Color.rgb(200, 0, 200), null, null, null);
        formatter5.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter5.getLinePaint().setStrokeWidth(10);
        dynamicPlot.addSeries(sine5Series, formatter5);

        dynamicPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(1);

        dynamicPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(1);

        dynamicPlot.getGraph().getLineLabelStyle(
                XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###.#"));

        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.AUTO);

        final DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);

        database = FirebaseDatabase.getInstance().getReference();

        fistTime = true;
        humidityReferences.add("humidity1");
        humidityReferences.add("humidity2");
        humidityReferences.add("humidity3");
        humidityReferences.add("humidity4");
        humidityReferences.add("humidity5");
        humidityReferences.add("time");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.plot_menu_sensor1){
            if (state[0]) {
                state[0] = false;
                state[1] = true;
                state[2] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.removeSeries(sine1Series);
                dynamicPlot.removeSeries(sine2Series);
                dynamicPlot.removeSeries(sine3Series);
                dynamicPlot.removeSeries(sine4Series);
                dynamicPlot.removeSeries(sine5Series);
                dynamicPlot.addSeries(sine1Series, formatter1);
                dynamicPlot.redraw();
            }
            else {
                state[0] = true;
                state[1] = true;
                state[2] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.addSeries(sine2Series, formatter2);
                dynamicPlot.addSeries(sine3Series, formatter3);
                dynamicPlot.addSeries(sine4Series, formatter4);
                dynamicPlot.addSeries(sine5Series, formatter5);
                dynamicPlot.redraw();
            }
        }
        if (item.getItemId() == R.id.plot_menu_sensor2){
            if (state[1]) {
                state[1] = false;
                state[0] = true;
                state[2] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.removeSeries(sine1Series);
                dynamicPlot.removeSeries(sine2Series);
                dynamicPlot.removeSeries(sine3Series);
                dynamicPlot.removeSeries(sine4Series);
                dynamicPlot.removeSeries(sine5Series);
                dynamicPlot.addSeries(sine2Series, formatter2);
                dynamicPlot.redraw();
            }
            else {
                state[1] = true;
                state[0] = true;
                state[2] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.addSeries(sine1Series, formatter1);
                dynamicPlot.addSeries(sine3Series, formatter3);
                dynamicPlot.addSeries(sine4Series, formatter4);
                dynamicPlot.addSeries(sine5Series, formatter5);
                dynamicPlot.redraw();
            }
        }
        if (item.getItemId() == R.id.plot_menu_sensor3) {
            if (state[2]) {
                state[2] = false;
                state[0] = true;
                state[1] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.removeSeries(sine1Series);
                dynamicPlot.removeSeries(sine2Series);
                dynamicPlot.removeSeries(sine3Series);
                dynamicPlot.removeSeries(sine4Series);
                dynamicPlot.removeSeries(sine5Series);
                dynamicPlot.addSeries(sine3Series, formatter3);
                dynamicPlot.redraw();
            }
            else {
                state[2] = true;
                state[0] = true;
                state[1] = true;
                state[3] = true;
                state[4] = true;
                dynamicPlot.addSeries(sine1Series, formatter1);
                dynamicPlot.addSeries(sine2Series, formatter2);
                dynamicPlot.addSeries(sine4Series, formatter4);
                dynamicPlot.addSeries(sine5Series, formatter5);
                dynamicPlot.redraw();
            }
        }
        if (item.getItemId() == R.id.plot_menu_sensor4){
            if (state[3]) {
                state[3] = false;
                state[0] = true;
                state[1] = true;
                state[2] = true;
                state[4] = true;
                dynamicPlot.removeSeries(sine1Series);
                dynamicPlot.removeSeries(sine2Series);
                dynamicPlot.removeSeries(sine3Series);
                dynamicPlot.removeSeries(sine4Series);
                dynamicPlot.removeSeries(sine5Series);
                dynamicPlot.addSeries(sine3Series, formatter4);
                dynamicPlot.redraw();
            }
            else {
                state[3] = true;
                state[0] = true;
                state[1] = true;
                state[2] = true;
                state[4] = true;
                dynamicPlot.addSeries(sine1Series, formatter1);
                dynamicPlot.addSeries(sine2Series, formatter2);
                dynamicPlot.addSeries(sine3Series, formatter3);
                dynamicPlot.addSeries(sine5Series, formatter5);
                dynamicPlot.redraw();
            }
        }
        if (item.getItemId() == R.id.plot_menu_sensor5){
            if (state[4]) {
                state[4] = false;
                state[0] = true;
                state[1] = true;
                state[2] = true;
                state[3] = true;
                dynamicPlot.removeSeries(sine1Series);
                dynamicPlot.removeSeries(sine2Series);
                dynamicPlot.removeSeries(sine3Series);
                dynamicPlot.removeSeries(sine4Series);
                dynamicPlot.removeSeries(sine5Series);
                dynamicPlot.addSeries(sine5Series, formatter5);
                dynamicPlot.redraw();
            }
            else {
                state[4] = true;
                state[0] = true;
                state[1] = true;
                state[2] = true;
                state[3] = true;
                dynamicPlot.addSeries(sine1Series, formatter1);
                dynamicPlot.addSeries(sine2Series, formatter2);
                dynamicPlot.addSeries(sine3Series, formatter3);
                dynamicPlot.addSeries(sine4Series, formatter4);
                dynamicPlot.redraw();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOnValueListener() {
        database.child("sht75").child("data").child("humidityArray").addValueEventListener(myValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (int i = 0; i < humidityReferences.size(); i++) {
                    DataSnapshot humidityDataSnapshot = dataSnapshot.child(humidityReferences.get(i));
                    if (fistTime) {
                        for (DataSnapshot humidityItemDataSnapshot : humidityDataSnapshot.getChildren()) {
                            double humidity = (Double) humidityItemDataSnapshot.getValue();
                            data.queues.get(i).add(humidity);
                        }
                    }
                    else {
                        GenericTypeIndicator<List<Double>> humidityGeneric = new GenericTypeIndicator<List<Double>>(){};
                        ArrayList<Double> humidities = (ArrayList<Double>) humidityDataSnapshot.getValue(humidityGeneric);
                        data.queues.get(i).add(humidities.get(humidities.size() - 1));
                    }
                }
                fistTime = false;
                data.SAMPLE_SIZE = data.queues.get(0).size();
                firebaseNotifier.notifyObservers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setOnValueListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        database.removeEventListener(myValueEventListener);
    }

    class SampleDynamicXYDatasource {

        public class LimitedQueue<E> extends LinkedList<E> {
            private int limit;

            public LimitedQueue(int limit) {
                this.limit = limit;
            }

            @Override
            public boolean add(E o) {
                super.add(o);
                while (size() > limit) { super.remove(); }
                return true;
            }
        }

        public static final int SERIES1 = 0;
        public static final int SERIES2 = 1;
        public static final int SERIES3 = 2;
        public static final int SERIES4 = 3;
        public static final int SERIES5 = 4;
        //public static final int SAMPLE_SIZE = 31;
        private ArrayList<LimitedQueue<Double>> queues = new ArrayList<>();
        private LimitedQueue<Double> queueHumidity1 = new LimitedQueue<>(111);
        private LimitedQueue<Double> queueHumidity2 = new LimitedQueue<>(111);
        private LimitedQueue<Double> queueHumidity3 = new LimitedQueue<>(111);
        private LimitedQueue<Double> queueHumidity4 = new LimitedQueue<>(111);
        private LimitedQueue<Double> queueHumidity5 = new LimitedQueue<>(111);
        private LimitedQueue<Double> queueTime = new LimitedQueue<>(111);
        public int SAMPLE_SIZE = queueHumidity1.size();

        public SampleDynamicXYDatasource() {
            queues.add(queueHumidity1);
            queues.add(queueHumidity2);
            queues.add(queueHumidity3);
            queues.add(queueHumidity4);
            queues.add(queueHumidity5);
            queues.add(queueTime);
        }

        public int getItemCount(int series) {
            return SAMPLE_SIZE;
        }

        public Number getX(int series, int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
            return queues.get(queues.size() - 1).get(index);
        }

        public Number getY(int series, int index) {

            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
            switch (series) {
                case SERIES1:
                    return queues.get(series).get(index);
                case SERIES2:
                    return queues.get(series).get(index);
                case SERIES3:
                    return queues.get(series).get(index);
                case SERIES4:
                    return queues.get(series).get(index);
                case SERIES5:
                    return queues.get(series).get(index);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    class SampleDynamicSeries implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private int seriesIndex;
        private String title;

        public SampleDynamicSeries(SampleDynamicXYDatasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return datasource.getItemCount(seriesIndex);
        }

        @Override
        public Number getX(int index) {
            return datasource.getX(seriesIndex, index);
        }

        @Override
        public Number getY(int index) {
            if (seriesIndex == 0) {
            }
            return datasource.getY(seriesIndex, index);
        }
    }
}





/*database.child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot.getChildren();
                List<Sensor> sensors = new ArrayList<>();
                for (DataSnapshot sensorDataSnapshot : dataSnapshot.getChildren()) {
                    String humidity = String.valueOf(sensorDataSnapshot.child("humidity").getValue());
                    String temperature = String.valueOf(sensorDataSnapshot.child("temperature").getValue());
                    //Sensor sensor = sensorDataSnapshot.getValue(Sensor.class);
                    Sensor sensor = new Sensor(humidity, temperature);
                    sensors.add(sensor);
                    //data.queue.add(Double.valueOf(sensor.getHumidity()));
                }
                data.queue.add(Double.valueOf(sensors.get(4).getHumidity()));
                data.SAMPLE_SIZE = data.queue.size();
                firebaseNotifier.notifyObservers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
