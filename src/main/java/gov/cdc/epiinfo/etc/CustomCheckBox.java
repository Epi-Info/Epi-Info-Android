package gov.cdc.epiinfo.etc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by rmitchell64 on 3/2/17.
 */

public class CustomCheckBox extends CheckBox
{
    private int mIndex;

    public CustomCheckBox( Context context )
    {
        super( context );
    }

    public CustomCheckBox(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setIndex( int index )
    {
        mIndex = index;
    }

    public int getIndex()
    {
        return mIndex;
    }
}
