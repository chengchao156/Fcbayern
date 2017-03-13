/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.fcbayern.android.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.fcbayern.android.R;


/**
 * Spinner model progress dialog that disables all tools for user interaction
 * after it shows up and and re-enables them after it dismisses.
 */
public class SpinnerProgressDialog extends Dialog {
    private ViewGroup mRootView;
    private ImageView mLoading;
    private TextView mTips;
    private Context mContext;

    private View mDialogParent;

	public SpinnerProgressDialog(Context context) {
		this(context, 0, 0, 0, 0);
	}

    public SpinnerProgressDialog(Context context, int theme) {
        this(context, 0, 0, 0, 0);
    }

    protected SpinnerProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        this(context, 0, 0, 0, 0);
    }

    public SpinnerProgressDialog(Context context, int leftPadding, int topPadding, int rightPadding, int bottomPadding) {
        super(context, R.style.SpinnerProgressDialog);
        mContext = context;

        mRootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);

        setContentView(mRootView);

        mDialogParent = (ViewGroup)mRootView.getParent();
        mDialogParent.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);

        mLoading = (ImageView) mRootView.findViewById(R.id.loadingImage);
        mTips = (TextView) mRootView.findViewById(R.id.text);
    }

    public SpinnerProgressDialog setPadding(int leftPadding, int topPadding, int rightPadding, int bottomPadding) {
        mDialogParent.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        return this;
    }

    public SpinnerProgressDialog showTips(boolean show) {
        mTips.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    @Override
    public void show() {
        super.show();
        if (mLoading.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) mLoading.getDrawable()).start();
        }
    }

    @Override
    public void dismiss() {
        if (mLoading.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) mLoading.getDrawable()).stop();
        }
        super.dismiss();
    }

    public SpinnerProgressDialog setMessage(int resid){
        String msg = mContext.getString(resid);
        setMessage(msg);
        return this;
	}

    public SpinnerProgressDialog setMessage(String msg) {
        mTips.setText(msg);
        if (TextUtils.isEmpty(msg)) {
            mTips.setVisibility(View.GONE);
        } else {
            mTips.setVisibility(View.VISIBLE);
        }
        return this;
    }
}
