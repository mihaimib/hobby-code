/**
 * TimeSeriesCharts have X ranges and two types of axis squeeze, 
 * can notify listeners of click-drag events and 
 * can have data series on two y axes
 * 
 * You can pass a label generator
 */
public abstract class TimeSeriesChart extends Chart
{

     /**
    * specifies an object which sould be notified when a range is selected
    * on the chart via a 'click-drag-release'. this object will implement the
    * TSChartListener Interface. Note that the chart may have more than one object
    * which wishes to be notified when a range is selected.
    */
    public abstract void addTSChartListener(TSChartListener listener);
    
    
    /**
    * sets the range of the x axis when a dataSeries is plotted, the ints xMin 
    * and xMax should be interpreted as calender indexes on the calendar 
    * associated with that DataSeries. ( note that these are unadjusted).
    * no changes to display are made until showData() is called
    */
    public abstract void setTSXRange(int xMin, int xMax);
    
    /**
    * returns the range of the graph as a TSXRange object (which
    * basically stores an int for xMin and an int for xMax). these ints
    * should correspond to indexes on the Calender associated whith
    * the presently displayed dataSeries. in the case where we are displaying
    */
    public abstract TSXRange getTSXRange();
    
    /**
    * sets the Dataseries which provides the data to be plotted on the
    * left axis.
    */
    public abstract void setLeftAxisDataSource(TSDataSeries dSeries);
    
    /**
    * sets the Dataseries which provides the data to be plotted on the
    * right axis.
    */
    public abstract void setRightAxisDataSource(TSDataSeries dSeries);
    
    /**
    * sets the object which the chart will make calls to in order to get
    * the strings for the date labels
    */
    public abstract void setTSLabelGenerator(TSLabelGenerator labelGenerator);
    
    /**
    * sets the setting of axis squeeze to true or false. no changes to
    * display are made untill showData() is called
    */
    public abstract void setAxisSqueezeMissing(boolean squeezeSetting);
    
    /**
    * sets the setting of axis squeeze to true or false. no changes to
    * display are made untill showData() is called
    */
    public abstract void setAxisSqueezeC(boolean squeezeSetting);
}
    
