package gov.cdc.epiinfo.etc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.ShareActionProvider;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ShareProvider extends ShareActionProvider {

    private final Context mContext;

    public ShareProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        View chooserView =
                super.onCreateActionView();

        Drawable icon =
                mContext.getResources().getDrawable(android.R.drawable.ic_menu_share);

        Class cl = chooserView.getClass();

        try {
            Method method = cl.getMethod("setExpandActivityOverflowButtonDrawable", Drawable.class);
            method.invoke(chooserView, icon);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return chooserView;
    }
}
