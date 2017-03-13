package cn.fcbayern.android.common;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.fcbayern.android.R;

/**
 * Created by chenzhan on 15/6/1.
 */
public class MyPreference extends Preference {

    private String mValue;
    private int mActionVisible;

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_setting_item, parent, false);
        return view;
    }

    @Override
    protected void onBindView(View view) {
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(getTitle());
        TextView value = (TextView) view.findViewById(R.id.value);
        value.setText(mValue);
        View action = view.findViewById(R.id.action);
        action.setVisibility(mActionVisible);
        super.onBindView(view);
    }

    public void setValue(String text) {
        mValue = text;
        notifyChanged();
    }

    public void setActionFlagVisible(int visible) {
        mActionVisible = visible;
        notifyChanged();
    }
}
