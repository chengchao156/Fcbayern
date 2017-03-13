package cn.fcbayern.android.common;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by chengchao on 16/9/23.
 */
public class MyDialog extends Dialog{


    public MyDialog(Context context) {
        super(context);
    }

    public MyDialog(Context context, int theme) {
        super(context, theme);
    }

    protected MyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
