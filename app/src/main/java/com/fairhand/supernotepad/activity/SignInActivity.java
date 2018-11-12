package com.fairhand.supernotepad.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.supernotepad.app.Config;
import com.fairhand.supernotepad.R;
import com.fairhand.supernotepad.http.LoginService;
import com.fairhand.supernotepad.http.UserIndividualInfoBean;
import com.fairhand.supernotepad.util.CacheUtil;
import com.fairhand.supernotepad.util.Logger;
import com.fairhand.supernotepad.util.Toaster;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 登录界面
 *
 * @author FairHand
 * @date 2018/10/22
 */
public class SignInActivity extends BaseActivity implements View.OnClickListener {
    
    private ImageView closeFace;
    private TextView user;
    private TextView password;
    private TextView forgetPassword;
    private EditText userName;
    private EditText userPassword;
    private Button signIn;
    
    /**
     * 用户输入的手机号、密码
     */
    private String phone, pass;
    
    private UserIndividualInfoBean userIndividualInfoBean;
    
    private Retrofit retrofit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        
        initView();
        setData();
        setAnimation();
    }
    
    @Override
    protected void onDestroy() {
        mLoginCallBack = null;
        super.onDestroy();
    }
    
    /**
     * 初始化视图
     */
    private void initView() {
        // 改状态栏字体颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        
        closeFace = findViewById(R.id.iv_close);
        user = findViewById(R.id.tv_mob);
        userName = findViewById(R.id.et_mob_num);
        password = findViewById(R.id.tv_password);
        userPassword = findViewById(R.id.et_password);
        forgetPassword = findViewById(R.id.tv_forget_password);
        signIn = findViewById(R.id.btn_sign_in);
    }
    
    /**
     * 设置数据
     */
    private void setData() {
        closeFace.setOnClickListener(this);
        signIn.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);
        
        Intent intent = getIntent();
        if (intent != null) {
            // 获取到传过来的手机号和密码
            phone = intent.getStringExtra(SignUpActivity.KEY_INTENT_PHONE_NUMBER);
            pass = intent.getStringExtra(SignUpActivity.KEY_INTENT_PASSWORD);
            Logger.d("数据来了SignInActivity", "手机号" + phone + ", " + "密码" + pass);
            // 设置值
            userName.setText(phone);
            userPassword.setText(pass);
            userName.setSelection(phone == null ? 0 : phone.length());
        }
        
        // 初始化Retrofit
        retrofit = new Retrofit.Builder()
                           .baseUrl(Config.BASE_URL)
                           .addConverterFactory(GsonConverterFactory.create())
                           .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                           .build();
    }
    
    /**
     * 设置动画
     */
    private void setAnimation() {
        // 从屏幕底部进入
        TranslateAnimation appear = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0f);
        appear.setDuration(888);
        // 设置开始结束缓慢，中间加速的插值器
        appear.setInterpolator(new AccelerateDecelerateInterpolator());
        user.startAnimation(appear);
        userName.startAnimation(appear);
        password.startAnimation(appear);
        userPassword.startAnimation(appear);
        forgetPassword.startAnimation(appear);
        signIn.startAnimation(appear);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // 启动主界面
                signIn();
                break;
            // 关闭界面
            case R.id.iv_close:
                onBackPressed();
                break;
            case R.id.tv_forget_password:
                Toaster.showShort(getApplicationContext(), "你忘记了我也没办法 :-)");
                break;
            default:
                break;
        }
    }
    
    /**
     * 登录
     */
    private void signIn() {
        // 获取用户输入的信息
        phone = userName.getText().toString();
        pass = userPassword.getText().toString();
        
        if (TextUtils.isEmpty(phone)) {
            Toaster.showShort(getApplicationContext(), "请输入账号");
        } else {
            // 设置用户数据（手机号、密码）
            Map<String, String> user = new HashMap<>(2);
            user.put("uid", phone);
            user.put("pwd", pass);
            
            // 生成对象的Service
            LoginService loginService = retrofit.create(LoginService.class);
            
            // 创建请求对象（结合RxJava使用（Flowable））
            // 去服务器请求登录
            loginService.login(user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<UserIndividualInfoBean>() {
                        Subscription subscription;
                        
                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription = s;
                            subscription.request(1);
                            Logger.d("onSubscribe over");
                        }
                        
                        @Override
                        public void onNext(UserIndividualInfoBean user) {
                            userIndividualInfoBean = user;
                            // 一定要加，否则在第一次获取之后将无法获取
                            // 原因：Subscriber在每次接收数据后会自动取消订阅
                            subscription.request(1);
                            onComplete();
                        }
                        
                        @Override
                        public void onError(Throwable t) {
                        
                        }
                        
                        @Override
                        public void onComplete() {
                            handleSignIn();
                        }
                    });
        }
    }
    
    /**
     * 处理登录
     */
    private void handleSignIn() {
        // 登录成功code
        int successCode = 3;
        // 根据服务器返回的数据进行判断登录结果
        if (userIndividualInfoBean.getError() == successCode) {
            startActivity(new Intent(this, MainActivity.class));
            Toaster.showShort(getApplicationContext(), "登录成功");
            // 保存用户已登录
            CacheUtil.putLoginYet(this, true);
            Config.isLogin = true;
            // 保存用户名
            CacheUtil.putUser(this, phone);
            Config.userAccount = phone;
            // 回调销毁欢迎界面
            mLoginCallBack.loginSuccess();
            finish();
        } else if (userIndividualInfoBean.getError() == 1) {
            Toaster.showShort(getApplicationContext(), "没有此账号");
        } else {
            Toaster.showShort(getApplicationContext(), "密码错误");
        }
    }
    
    private static LoginCallBack mLoginCallBack;
    
    /**
     * 设置回调接口
     */
    public static void setCallBack(LoginCallBack loginCallBack) {
        mLoginCallBack = loginCallBack;
    }
    
    /**
     * 登录回调接口
     */
    public interface LoginCallBack {
        /**
         * 登录成功回调
         */
        void loginSuccess();
    }
    
}