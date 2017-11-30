package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Geocode extends EnterRule
{
    boolean IsExceptList = false;
    String LatitudeField = null;
    String LongitudeField = null;

    public Rule_Geocode(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);

        this.LatitudeField = this.ExtractIdentifier(pToken.get(3)).toString();
        this.LongitudeField = this.ExtractIdentifier(pToken.get(5)).toString();
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Context.CheckCodeInterface.CaptureCoordinates(this.LatitudeField, this.LongitudeField);
        return null;
    }
}
