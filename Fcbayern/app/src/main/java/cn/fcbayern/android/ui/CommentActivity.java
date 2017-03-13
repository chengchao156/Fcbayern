package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.MyDialog;
import cn.fcbayern.android.common.PullRefreshListView2;
import cn.fcbayern.android.model.NewsCommentBean;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener {

    private List<NewsCommentBean> commentBeen = new ArrayList<>();
    private PullRefreshListView2 listView;
    private RelativeLayout back;

    private Dialog dialog;
    private TextView title;

    private String nid;
    private CommentAdapter adapter;
    private Dialog dia;
    private TextView dialogTitle;
    private TextView count;

    private Button commit;
    private PopupWindow popupWindow;
    private EditText etComment;

    private int allCount;
    private SharedPreferences preferences;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        initView();
        title.setText("一大波评论正在赶来");
        dialog.show();
        View view = LayoutInflater.from(this).inflate(R.layout.comment_head, null);
        count = (TextView) view.findViewById(R.id.count);
        listView.getRefreshableView().addHeaderView(view);
        nid = getIntent().getStringExtra("nid");
        adapter = new CommentAdapter(this);
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                commentBeen.clear();
                initDate("");
                complete();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (commentBeen.size() != 0) {
                    initDate(String.valueOf(commentBeen.get(commentBeen.size() - 1).getId()));
                    complete();
                } else {
                    ToastUtils.showToast(CommentActivity.this, "当前暂无评论");
                    complete();
                }
            }
        });
        initDate("");
    }

    private void complete() {
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.onRefreshComplete();
            }
        }, 1000);
    }

    private void initDate(String last) {

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "comment_list");
        params.put("nid", nid);
        params.put("uid", "");
        params.put("last_id", last);
        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("comment_list", 0, "", 0, params))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    dialog.dismiss();
                                    JSONObject obje = object.getJSONObject("info");
                                    allCount = obje.getInt("all_count");
                                    count.setText(String.valueOf(allCount));
                                    JSONArray data = object.getJSONArray("data");
                                    if (data.length() == 0) {

                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            int id = obj.getInt("id");
                                            int cont_id = obj.getInt("cont_id");
                                            int uid = obj.getInt("uid");
                                            String username = obj.getString("username");
                                            String avatar = obj.getString("avatar");
                                            String content = obj.getString("content");
                                            long time = obj.getInt("time");
                                            int good = obj.getInt("good");
                                            int gooded = obj.getInt("gooded");
                                            NewsCommentBean bean = new NewsCommentBean(id, cont_id, uid, username, avatar, content, time, good, gooded);
                                            commentBeen.add(bean);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                } else if (code.equals("-1")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(CommentActivity.this, "未找到相关数据");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        listView = (PullRefreshListView2) this.findViewById(R.id.commentListView);
        dialog = new MyDialog(this, R.style.dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
        commit = (Button) this.findViewById(R.id.commitComment);
        etComment = (EditText) this.findViewById(R.id.etComment);
        View vi = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dia = new MyDialog(this, R.style.dialog);
        dia.setContentView(vi);
        dialogTitle = (TextView) vi.findViewById(R.id.title);
        commit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.commitComment:
                if (MainApp.isLogin) {
                    if (etComment.getText().toString().equals("")) {
                        ToastUtils.showToast(this, "请先输入评论内容");
                    } else {
                        dialogTitle.setText("评论提交中");
                        dia.show();
                        commitComment();
                    }
                } else {
                    setPopWindow("提交评论需要登录哦");
                }
                break;
            case R.id.cancle:
                popupWindow.dismiss();
                break;
            case R.id.confirm:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
                break;
        }
    }

    private void commitComment() {

        String uid = "";
        String callback = "";

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        if (preferences != null) {
            uid = String.valueOf(preferences.getInt("uid", 0));
            callback = preferences.getString("call", "");
        }
        params.put("action", "add_comment");
        params.put("nid", nid);
        params.put("uid", uid);
        params.put("callback_verify", callback);
        params.put("content", etComment.getText().toString());

        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("add_comment", 0, "", 0, params))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    dia.dismiss();
                    final String json = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject data = object.getJSONObject("data");
                                    JSONObject comment = data.getJSONObject("comment");
                                    int id = comment.getInt("id");
                                    int cont_id = comment.getInt("cont_id");
                                    int uid = comment.getInt("uid");
                                    String username = comment.getString("username");
                                    String avatar = comment.getString("avatar");
                                    String content = comment.getString("content");
                                    long time = comment.getInt("time");
                                    int good = comment.getInt("good");
                                    NewsCommentBean bean = new NewsCommentBean(id, cont_id, uid, username, avatar, content, time, good, 0);
                                    commentBeen.add(bean);
                                    adapter.notifyDataSetChanged();
                                    allCount = allCount + 1;
                                    count.setText(String.valueOf(allCount));
                                    Toast.makeText(CommentActivity.this, " 评论成功", Toast.LENGTH_SHORT).show();
                                } else if (code.equals("-3")) {
                                    ToastUtils.showToast(CommentActivity.this, "未找到相关数据");
                                } else if (code.equals("-6")) {
                                    ToastUtils.showToast(CommentActivity.this, "操作频繁");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setPopWindow(String str) {
        popupWindow = new PopupWindow(LayoutInflater.from(this).inflate(R.layout.comment_popupwindow, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        View contentView = popupWindow.getContentView();
        TextView cancale = (TextView) contentView.findViewById(R.id.cancle);
        TextView confirm = (TextView) contentView.findViewById(R.id.confirm);
        TextView title = (TextView) contentView.findViewById(R.id.popTitle);
        cancale.setOnClickListener(this);
        confirm.setOnClickListener(this);
        title.setText(str);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_newsweb2, null);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public class CommentAdapter extends BaseAdapter implements View.OnClickListener {

        LayoutInflater inflater;
        NewsCommentBean bean;
        ViewHolder holder;

        public CommentAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return commentBeen.size();
        }

        @Override
        public Object getItem(int position) {
            return commentBeen.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_comment, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            bean = commentBeen.get(position);
            holder.name.setText(bean.getUsername());
            GlideImageLoader.loadHeadImage(CommentActivity.this, bean.getAvatar(), holder.head);
            holder.likecount.setText(String.valueOf(bean.getGood()));

            if (bean.getGooded() == 0) {
                holder.like.setBackgroundResource(R.mipmap.agree_nor);
            } else {
                holder.like.setBackgroundResource(R.mipmap.agree_sel);
            }

            holder.like.setTag(position);
            holder.like.setOnClickListener(this);
            holder.content.setText(bean.getContent());
            holder.date.setText(DateUtils.getStandardDate(String.valueOf(bean.getTime())));
            return convertView;
        }

        @Override
        public void onClick(View v) {
            int position = (int) (int) v.getTag();
            switch (v.getId()) {
                case R.id.like:
                    bean = commentBeen.get(position);
                    if (MainApp.isLogin) {
                        String uid = "";
                        String callback = "";
                        HashMap<String, String> params = new HashMap<>();
                        params.put("action", "comment_good");
                        int id = bean.getId();
                        params.put("cid", String.valueOf(id));
                        if (preferences != null) {
                            uid = String.valueOf(preferences.getInt("uid", 0));
                            callback = preferences.getString("call", "");
                        }
                        params.put("uid", uid);
                        params.put("callback_verify", callback);

                        Request request = new Request.Builder()
                                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("comment_good", 0, "", 0, params))
                                .get()
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    final String json = response.body().string();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject object = new JSONObject(json);
                                                String code = object.getString("code");
                                                if (code.equals("0")) {
                                                    bean.setGood(bean.getGood() + 1);
                                                    bean.setGooded(1);
                                                    notifyDataSetChanged();
                                                } else if (code.equals("-3")) {
                                                    ToastUtils.showToast(CommentActivity.this, "没有找到相应数据");
                                                } else if (code.equals("-4")) {
                                                    ToastUtils.showToast(CommentActivity.this, "操作频繁");
                                                } else if (code.equals("-5")) {
                                                    ToastUtils.showToast(CommentActivity.this, "你已点赞过此评论");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        setPopWindow("点赞需要登录哦");
                    }
                    break;
            }

        }


        class ViewHolder {
            TextView name;
            TextView date;
            TextView content;
            TextView likecount;
            ImageView head;
            ImageView like;

            public ViewHolder(View view) {
                like = (ImageView) view.findViewById(R.id.like);
                name = (TextView) view.findViewById(R.id.name);
                head = (ImageView) view.findViewById(R.id.head);
                likecount = (TextView) view.findViewById(R.id.likeCount);
                content = (TextView) view.findViewById(R.id.content);
                date = (TextView) view.findViewById(R.id.showTime);
            }
        }
    }

}
