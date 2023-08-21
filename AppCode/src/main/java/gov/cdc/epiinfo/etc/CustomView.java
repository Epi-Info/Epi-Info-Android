package gov.cdc.epiinfo.etc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by asad on 5/1/18.
 */

public class CustomView extends View {
    private int mIndex;
    private String text;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
