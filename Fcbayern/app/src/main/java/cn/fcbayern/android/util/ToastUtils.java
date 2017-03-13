package cn.fcbayern.android.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.fcbayern.android.R;

/**
 * Created by chengchao on 16/9/26.
 */
public class ToastUtils {

    public static void showToast(Context context,String str){
        Toast toast;
        toast = Toast.makeText(context,str,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL,10,10);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(str);
        toast.setView(view);
        toast.show();
    }


}
