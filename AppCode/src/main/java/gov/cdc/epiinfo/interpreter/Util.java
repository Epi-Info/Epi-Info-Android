package gov.cdc.epiinfo.interpreter;

import java.util.Date;

public class Util 
{
    private static Date nullDateTime = new Date();

    /// <summary>
    /// Gets a null value DateTime object.
    /// </summary>
    public static Date getNullDateTime()
    {
        return nullDateTime;
    }

    /// <summary>
    /// Determines if the string object is empty.
    /// </summary>
    /// <param name="str"></param>
    /// <returns></returns>
    public static boolean IsEmpty(String str)
    {
    	if(str == null) return true;
    	
        return str.length() == 0;
    }

    /// <summary>
    /// Determines if the datetime object is empty.
    /// </summary>
    /// <param name="dt"></param>
    /// <returns></returns>
    public static boolean IsEmpty(Date dt)
    {
        if (dt == null) return true;
        else return dt.compareTo(nullDateTime) == 0;
    }

    /// <summary>
    /// Tests an object for empty or null.
    /// </summary>
    /// <param name="obj">Any object.</param>
    /// <returns>Results of empty or null.</returns>
    public static boolean IsEmpty(Object obj)
    {
        if (obj == null) return true;
        //else if (obj == DBNull.Value) return true;
        else return IsEmpty(obj.toString());
    }

}
