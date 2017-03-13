
package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateBirthActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout choose;
    private RelativeLayout back;
    private TimePickerView timePicker;

    private TextView time;
    private Button commit;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_birth);
        initView();
        if (System.currentTimeMillis() == new Date().getTime()) {
            time.setText("");
        }

        timePicker.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                time.setText(getTime(date));
            }
        });
    }

    private void initView() {
        choose = (RelativeLayout) this.findViewById(R.id.chooseDate);
        choose.setOnClickListener(this);
        timePicker = new TimePickerView(this, TimePickerView.Type.YEAR_MONTH_DAY);
        Calendar calendar = Calendar.getInstance();
        timePicker.setRange(calendar.get(Calendar.YEAR) - 100, calendar.get(Calendar.YEAR));
        timePicker.setTime(new Date());
        timePicker.setCyclic(false);
        timePicker.setCancelable(true);
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        time = (TextView) this.findViewById(R.id.time);
        commit = (Button) this.findViewById(R.id.commit);
        commit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.chooseDate:
                timePicker.show();
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.commit:
                if(time.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "请先选择日期");
                }else {
                    update(time.getText().toString());
                }
                break;
        }
    }

    public static String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    private void update(final String str) {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        String uid = "";
        String call = "";

        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        final MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();

        if (preferences != null) {
            uid = String.valueOf(preferences.getInt("uid", 0));
            call = preferences.getString("call", "");
        }

        params.put("action", "profile_update");
        params.put("uid", uid);
        params.put("callback_verify", call);
        params.put("field_name", "birth");
        params.put("field_value", str);


        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("profile_update", 0, "", 0, params))
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
                                    if (userInfo != null) {
                                        userInfo.setBirth(str);
                                    }
                                    Toast.makeText(UpdateBirthActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                    UpdateBirthActivity.this.finish();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (timePicker.isShowing()) {
                timePicker.dismiss();
                return true;
            }
            if (timePicker.isShowing()) {
                timePicker.dismiss();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
