package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.util.HintUtils;
import cn.fcbayern.android.util.SharedPrefsStrListUtil;
import cn.fcbayern.android.util.ToastUtils;

public class SerachActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etSerach;
    private TextView cancle;
    private ListView listView;
    private LinearLayout linearLayout;
    private List<String> content = new ArrayList<>();

    private ImageView serach;
    private TextView type;

    PopupWindow popupWindow;
    TextView tvNews;
    TextView tvVideo;
    TextView tvMatch;

    static List<TextView> textViews = new ArrayList<>();
    private RelativeLayout select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serach);
        initView();
        setPopWindow();
        setOnClick();
        SerachAdapter adapter = new SerachAdapter(this);
        listView.setAdapter(adapter);
        initData();
        Collections.reverse(content);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        List<String> conten = SharedPrefsStrListUtil.getStrListValue(this, "content");
        if (conten != null) {
            content = conten;
        }
        if (content.size() <= 0) {
            linearLayout.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setOnClick() {
        cancle.setOnClickListener(this);
        select.setOnClickListener(this);
        serach.setOnClickListener(this);
    }

    private void initView() {
        etSerach = (EditText) this.findViewById(R.id.etSerach);
        HintUtils.setHintTextSize(etSerach, "搜索", 14);
        cancle = (TextView) this.findViewById(R.id.cancle);
        listView = (ListView) this.findViewById(R.id.sousuoList);
        linearLayout = (LinearLayout) this.findViewById(R.id.SerachView);
        serach = (ImageView) this.findViewById(R.id.iv_serach);
        select = (RelativeLayout) this.findViewById(R.id.select);
        type = (TextView) this.findViewById(R.id.type);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_news:
                popupWindow.dismiss();
                type.setText("新闻");
                break;
            case R.id.tv_video:
                popupWindow.dismiss();
                type.setText("视频");
                break;
            case R.id.tv_match:
                popupWindow.dismiss();
                type.setText("图集");
                break;
            case R.id.cancle:
                onBackPressed();
                break;
            case R.id.iv_serach:
                if (!String.valueOf(etSerach.getText()).equals("")) {
                    if (!content.contains(String.valueOf(etSerach.getText()))) {
                        content.add(String.valueOf(etSerach.getText()));
                    }
                    SharedPrefsStrListUtil.putStrListValue(this, "content", this.content);
                    Intent intent = new Intent(this, SerachResultctivity.class);
                    intent.putExtra("type", type.getText().toString());
                    intent.putExtra("value", etSerach.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtils.showToast(this, "请键入搜索内容");
                }

                break;
            case R.id.select:
                popupWindow.showAsDropDown(select, -60, 30);
                break;
        }
    }

    /**
     * 设置PopupWindow
     */
    private void setPopWindow() {
        popupWindow = new PopupWindow(LayoutInflater.from(this).inflate(R.layout.pop_type_layout, null), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        View contentView = popupWindow.getContentView();
        tvNews = (TextView) contentView.findViewById(R.id.tv_news);
        tvVideo = (TextView) contentView.findViewById(R.id.tv_video);
        tvMatch = (TextView) contentView.findViewById(R.id.tv_match);
        tvNews.setOnClickListener(this);
        tvVideo.setOnClickListener(this);
        tvMatch.setOnClickListener(this);
        textViews.add(tvNews);
        textViews.add(tvVideo);
        textViews.add(tvMatch);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    class SerachAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public SerachAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return content.size();
        }

        @Override
        public Object getItem(int position) {
            return content.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            if (null == convertView) {
                convertView = inflater.inflate(R.layout.list_item_serach, parent, false);
            }
            final TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView delete = (TextView) convertView.findViewById(R.id.delete);
            if (!content.get(position).equals("")) {
                title.setText(content.get(position));
            }

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SerachActivity.this, SerachResultctivity.class);
                    intent.putExtra("type", type.getText().toString());
                    intent.putExtra("value", title.getText().toString());
                    startActivity(intent);
                    finish();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    content.remove(position);
                    SerachAdapter.this.notifyDataSetChanged();
                    SharedPrefsStrListUtil.putStrListValue(SerachActivity.this, "content", content);
                    if (content.size() <= 0) {
                        linearLayout.setVisibility(View.GONE);
                    }
                }
            });
            return convertView;
        }
    }
}
