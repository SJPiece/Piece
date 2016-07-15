package com.sealiu.piece.controller.LoginRegister;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ScrollView;

import com.sealiu.piece.R;
import com.sealiu.piece.controller.Maps.MapsActivity;
import com.sealiu.piece.model.Constants;
import com.sealiu.piece.model.User;
import com.sealiu.piece.service.PieceMainService;
import com.sealiu.piece.utils.Md5Utils;
import com.sealiu.piece.utils.SPUtils;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.Listener {

    private static final String TAG = "LoginActivity";
    private FragmentManager fm = getSupportFragmentManager();

    private ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启动MainService
        Intent intent1 = new Intent(this, PieceMainService.class);
        startService(intent1);
        Log.i(TAG, "onCreate");

        scrollView = (ScrollView) findViewById(R.id.login_form);

        String username = SPUtils.getString(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_USERNAME, null);
        String pwd = SPUtils.getString(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_PASSWORD, null);

        if (!isOutOfDate()
                && !SPUtils.getString(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_USER_OBJECT_ID, "").equals("")
                && SPUtils.getBoolean(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_IS_LOGIN, false)
                && username != null
                && pwd != null) {
            //距上次登录没有超过1个月，objectId不为空，且用户为登录状态，则自动登录
            //final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
            //progress.setMessage("正在登录中...");
            //progress.setCanceledOnTouchOutside(false);
            //progress.show();

            User user = new User();
            String password = Md5Utils.encode(pwd);
            user.setUsername(username);
            user.setPassword(password);
            user.login(new SaveListener<User>() {
                    @Override
                    public void done(User u, BmobException e) {
                        if (e == null) {
                            SPUtils.putString(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_USER_OBJECT_ID, u.getObjectId());
                            SPUtils.putBoolean(LoginActivity.this, Constants.SP_FILE_NAME, Constants.SP_IS_LOGIN, true);
                            Log.i(TAG, "Login success");
                            onSubmitLoginBtnClick();
                        } else {
                            SPUtils.clear(LoginActivity.this, Constants.SP_FILE_NAME);

                            setContentView(R.layout.activity_login);

                            Fragment fragment = fm.findFragmentById(R.id.content_frame);

                            if (fragment == null) {
                                fragment = new LoginFragment();
                                fm.beginTransaction()
                                        .add(R.id.content_frame, fragment, null)
                                        .commit();
                            }
                            Snackbar.make(scrollView, "用户名或密码错误", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });

            //progress.dismiss();
        } else {
            // 需要手动登录
            setContentView(R.layout.activity_login);
            Fragment fragment = fm.findFragmentById(R.id.content_frame);
            if (fragment == null) {
                fragment = new LoginFragment();
                fm.beginTransaction()
                        .add(R.id.content_frame, fragment, null)
                        .commit();
            }
        }

    }

    /**
     * 检查自动登录是否过期
     *
     * @return 超过：true  没有超过：false
     */
    private boolean isOutOfDate() {
        //获取当前时间
        long timeNow = System.currentTimeMillis();
        Log.i(TAG, "now:" + timeNow);
        //获取用户上次登录时间
        long timePre = SPUtils.getLong(this, Constants.SP_FILE_NAME, Constants.SP_LOGIN_TIME, 0);
        Log.i(TAG, "pre:" + timePre);
        //当用户本次登陆时间大于上次登录时间一个月
        boolean result = timeNow - timePre > Constants.OUT_OF_DATE_LIMIT;
        return result;
    }

    @Override
    public void onSubmitLoginBtnClick() {

        Log.i(TAG, "Start MapsActivity");
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackRegisterBtnClick() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    public void onThirdPartLoginBtnClick() {
        Fragment fragment = new ThirdPartFragment();
        fm.beginTransaction()
                .replace(R.id.content_frame, fragment, "ThirdPart")
                .addToBackStack("ThirdPart")
                .commit();
    }

}

