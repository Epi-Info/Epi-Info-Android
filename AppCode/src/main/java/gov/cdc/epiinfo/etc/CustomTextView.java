package gov.cdc.epiinfo.etc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by rmitchell64 on 2/17/17.
 */

public class CustomTextView extends TextView
{
    private int mIndex;

    public CustomTextView( Context context )
    {
        super( context );
    }

    public CustomTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle)
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
