package fi.ubiss2015.smokingrecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.Paint;
import android.widget.TextView;

import com.aware.Aware;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.Bar;

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Susanne on 6/12/2015.
 */
public class TrackerActivity extends Activity {

    private double cigarettePrice = 0.3;
    private BarChartView mDayChart, mMonthChart, mYearChart;
    private int dayColor, monthColor, yearColor;
    private Paint mBarGridPaint;

    private final static int BAR_MAX = 10;
    private final static int BAR_MIN = 0;
    private final static String[] dayLabels = {"", "02", "", "04", "", "06", "","08", "", "10", "", "12", "", "14","", "16", "", "18", "", "20", "","22","","24"};
    private final static float [] dayValues = {0f, 2f, 0f, 2f, 1f, 1f, 1f,0f, 2f, 0f, 2f, 1f, 0f, 1f,0f, 1f, 0f, 2f, 1f, 1f, 0f,0f,0f,0f};

    private final static String[] monthLabels = {"", "02", "", "04", "", "06", "","08", "", "10", "", "12", "", "14","", "16", "", "18", "", "20", "","22","","24","","26","","28","","30"};
    private final static float [] monthValues = {5f, 2f, 5f, 6f, 4f, 6f, 5f,4f, 5f, 6f, 7f, 10f, 0f, 0f,0f, 0f, 0f, 0f, 0f, 0f, 0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};

    private final static String[] yearLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
    private final static float [] yearValues = {20f, 22f, 20f, 25f, 30f, 40f, 0f,0f, 0f, 0f, 0f, 0f};

    private TextView cigarettesToday, yourAverage, moneySpent, timeSinceLastSmoke, currentDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_activiy_layout);

        initBarCharts();
        updateDayChart();
        updateMonthChart();
        updateYearChart();

        cigarettesToday = (TextView) findViewById(R.id.todaySmoke);
        yourAverage = (TextView) findViewById(R.id.averageSmoke);
        moneySpent = (TextView) findViewById(R.id.moneySpent);
        timeSinceLastSmoke = (TextView) findViewById(R.id.timeSinceLastSmoke);
        currentDay = (TextView) findViewById(R.id.dayLabel);

        int cigarettes = (int) Math.floor(Math.random() * 10);

        cigarettesToday.setText(""+ cigarettes);
        yourAverage.setText(""+ (int) Math.floor(Math.random() * 10));
        moneySpent.setText(""+ String.format("%.2f", cigarettes*cigarettePrice)+" €");

        int time = (int) Math.floor(Math.random() * 240);
        int hour = (int) Math.floor(time / 60);
        int minutes = time-(hour*60);

        timeSinceLastSmoke.setText(hour+"h"+minutes+"min");

        currentDay.setText(""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        //TODO: Query database of watch from phone and visualize.
        //TODO: DRAW CHARTS
        //TODO: UPDATE VALUES
    }

    //barchart

    /*------------------------------------*
	 *              BARCHART              *
	 *------------------------------------*/

    private void initBarCharts(){

        mDayChart = (BarChartView) findViewById(R.id.daychart);
        mMonthChart = (BarChartView) findViewById(R.id.monthchart);
        mYearChart = (BarChartView) findViewById(R.id.yearchart);

        dayColor = this.getResources().getColor(R.color.lightGreen);
        monthColor = this.getResources().getColor(R.color.darkYellow);
        yearColor = this.getResources().getColor(R.color.lightRed);


       /*
        mBarChart.setOnEntryClickListener(barEntryListener);
        mBarChart.setOnClickListener(barClickListener);
        */

    }


    private void updateDayChart(){

        mDayChart.reset();




        BarSet barSet = new BarSet();
        Bar bar;
        for(int i = 0; i < dayLabels.length; i++){
            bar = new Bar(dayLabels[i], dayValues[i]);
            bar.setColor(dayColor);
            barSet.addBar(bar);
        }
        mDayChart.addData(barSet);

        mDayChart.setBarSpacing(Tools.fromDpToPx(14));

        mDayChart.setBorderSpacing(0)
                .setAxisBorderValues(BAR_MIN, 5, 2)
                .setYAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.NONE)
                .show()
        //.show()
        ;
    }

    private void updateMonthChart(){

        mMonthChart.reset();



        BarSet barSet = new BarSet();
        Bar bar;
        for(int i = 0; i < monthLabels.length; i++){
            bar = new Bar(monthLabels[i], monthValues[i]);
            bar.setColor(monthColor);
            barSet.addBar(bar);
        }
        mMonthChart.addData(barSet);

        mMonthChart.setBarSpacing(Tools.fromDpToPx(14));

        mMonthChart.setBorderSpacing(0)
                .setAxisBorderValues(BAR_MIN, 15, 2)
                .setYAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.NONE)
                .show()
        //.show()
        ;

    }

    private void updateYearChart(){

        mYearChart.reset();



        BarSet barSet = new BarSet();
        Bar bar;
        for(int i = 0; i < yearLabels.length; i++){
            bar = new Bar(yearLabels[i], yearValues[i]);
            bar.setColor(yearColor);
            barSet.addBar(bar);
        }
        mYearChart.addData(barSet);

        mYearChart.setBarSpacing(Tools.fromDpToPx(14));

        mYearChart.setBorderSpacing(0)
                .setAxisBorderValues(BAR_MIN, 50, 2)
                .setYAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.NONE)
                .show()
        //.show()
        ;
    }


   /* private void updateValues(BarChartView chartView){

        chartView.updateValues(0, barValues);
        chartView.notifyDataUpdate();
    }*/

}
