class TSDataSeries
{
    // member variables
    //
    
    /**
    * this is the group of the StyledTSDataSets to be displayed upon the graph
    */
    private Vector  theStyledDataSets	= new Vector(1,1);
    
    /**
    * the calendar which all of the styledDataSets have their values
    * interpreted with
    */
    private String calName;
    
    // methods
    //
    
    /**
    * Gets the calendar associated with this data set (if any).
    *
    * @return		the calendar or <code>null</code>
    */
    public CalImp       getCalendar()
    {
	return calendar;
    }
    
    /**
    * adds a styled dataSet at the specified index in theStyledDataSets
    * note that it may have to extend the size of theStyledDataSets to
    * do this.
    */
    public void setDataSet(int index,StyledDataSets ds)
    {
    	
    }
    
    /**
    * adds a styled dataSet to the end of theStyledDataSets
    */
    public void addDataSet(StyledDataSets ds)
    {
    	
    }
    
    /**
    * Remove all the styles DataSets from this series
    */
    public void removeAllDataSets()
    {
	
    }
    
    /**
    * Check if there is at least one DataSet in this series.
    * Need to check the contents of "theStyledDataSets" as it may not be empty
    * but all null
    */
    public boolean isEmpty()
    {
    
    }
    
}
