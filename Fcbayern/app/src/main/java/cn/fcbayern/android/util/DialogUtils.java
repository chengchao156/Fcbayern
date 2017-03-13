package cn.fcbayern.android.util;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ShareDialog;

/**
 * Utility to create dialog
 *
 */
public class DialogUtils {

    /**
     * 创建简单的对话框
     *
     * @param ctx
     * @param negativeBtnListener
     * @param positiveBtnListener
     * @return
     */
    public static Dialog createCommonDialog(Context ctx, String msg, DialogInterface.OnClickListener negativeBtnListener, DialogInterface.OnClickListener positiveBtnListener) {
        return new AlertDialog.Builder(ctx)
                .setCancelable(true)
                .setMessage(msg)
                .setNegativeButton(R.string.dialog_cancel, negativeBtnListener)
                .setPositiveButton(R.string.dialog_confirm, positiveBtnListener)
                .create();
    }

    public static ShareDialog createShareDialog(Context ctx) {
        ShareDialog dialog = new ShareDialog(ctx, R.style.FullScreenDialogStyle);
        return dialog;
    }

}
