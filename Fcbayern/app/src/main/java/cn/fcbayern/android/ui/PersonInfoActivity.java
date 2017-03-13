package cn.fcbayern.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.CustomDialog;
import cn.fcbayern.android.util.GlideImageLoader;

public class PersonInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;
    private RelativeLayout relativeSex;
    private RelativeLayout relativeBirth;
    private RelativeLayout relativeAdres;

    private TextView createTime;
    private ImageView headImage;
    private TextView sex;
    private TextView birth;
    private TextView adress;
    private TextView email;
    private TextView userName;

    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        initView();
        setOnClick();
        setInfo();
    }

    private void setInfo() {
        MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();
            userName.setText(userInfo.getUsername());
            createTime.setText("注册时间: " + userInfo.getReg_date().substring(0, 10));
            if (userInfo.getGender().equals("未设置")) {
                sex.setText("未设置");
            } else {
                sex.setText(userInfo.getGender());
            }
            GlideImageLoader.loadHeadImage(PersonInfoActivity.this, userInfo.getAvatar(), headImage);
            email.setText(userInfo.getEmail());
            if (userInfo.getArea().equals("未设置")) {
                adress.setText("未设置");
            } else {
                adress.setText(userInfo.getArea());
            }
            if (userInfo.getBirth().equals("未设置")) {
                birth.setText("未设置");
            } else {
                birth.setText(userInfo.getBirth());
            }
        }

    private void setOnClick() {
        back.setOnClickListener(this);
        logout.setOnClickListener(this);
        relativeSex.setOnClickListener(this);
        relativeBirth.setOnClickListener(this);
        relativeAdres.setOnClickListener(this);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        createTime = (TextView) this.findViewById(R.id.create_time);
        headImage = (ImageView) this.findViewById(R.id.head);
        userName = (TextView) this.findViewById(R.id.userName);
        sex = (TextView) this.findViewById(R.id.sex);
        birth = (TextView) this.findViewById(R.id.birthday);
        adress = (TextView) this.findViewById(R.id.adress);
        email = (TextView) this.findViewById(R.id.email);
        logout = (Button) this.findViewById(R.id.logout);
        relativeSex = (RelativeLayout) this.findViewById(R.id.relativeSex);
        relativeBirth = (RelativeLayout) this.findViewById(R.id.relativeBirth);
        relativeAdres = (RelativeLayout) this.findViewById(R.id.relativeAdress);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.logout:
                CustomDialog dialog = new CustomDialog(this, R.style.customdialog, R.layout.customdialog);
                dialog.setOwnerActivity(this);
                dialog.show();
                break;
            case R.id.relativeSex:
                Intent intent = new Intent(this,UpDateSexActivity.class);
                startActivity(intent);
                break;
            case R.id.relativeBirth:
                Intent birth = new Intent(this,UpdateBirthActivity.class);
                startActivity(birth);
                break;
            case R.id.relativeAdress:
                Intent adress = new Intent(this,UpdateAdressActivity.class);
                startActivity(adress);
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        setInfo();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
