package com.sealiu.piece.controller.User;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.sealiu.piece.R;
import com.sealiu.piece.model.Constants;
import com.sealiu.piece.model.LoginUser;
import com.sealiu.piece.model.User;
import com.sealiu.piece.utils.ImageLoader.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;

import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.listener.VerifySMSCodeListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 展示用户的个人信息（头像，昵称，简介）
 * 点击相应信息弹出FragmentDialog，进行设置。
 */
public class EditActivity extends AppCompatActivity implements
        EditNameFragment.EditNameDialogListener,
        EditBioFragment.EditBioDialogListener,
        EditPhoneFragment.EditPhoneDialogListener,
        EditEmailFragment.EditEmailDialogListener,
        EditBirthFragment.EditBirthDialogListener,
        EditPwdFragment.EditPwdDialogListener,
        PickPictureFragment.PickPictureListener,
        View.OnClickListener {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_PHOTO = 3;
    private static final String TAG = "EditActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 111;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 222;
    public static NestedScrollView layoutScroll;
    private EditText usernameET, bioET, birthET, phoneET, emailET;
    private ImageView headPictureIV;
    private String objectId;
    private Uri previewUri;
    private String realPath;
    private LoginUser loginUser;

    private boolean isNotAskAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        loginUser = UserInfoSync.getLoginInfo(EditActivity.this);

        layoutScroll = (NestedScrollView) findViewById(R.id.scroll_view);

        objectId = loginUser.getObjectId();

        if (objectId == null || objectId.equals("")) {
            //获取当前用户
            BmobUser bmobUser = User.getCurrentUser();
            //获取objectId
            objectId = bmobUser.getObjectId();
            loginUser.setObjectId(objectId);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_user_info);
        }

        // 将sp文件中存储的用户信息显示出来，并设置监听
        displayContent();
    }

    /**
     * 显示个人资料内容，并设置监听函数修改资料
     */
    private void displayContent() {
        String nickname = loginUser.getNickname();
        String sex = loginUser.getSex();
        String bio = loginUser.getBio();
        String birth = loginUser.getBirth();
        String phone = loginUser.getMobilePhone();
        String email = loginUser.getEmail();
        String headPicture = loginUser.getAvatar();

        usernameET = (EditText) findViewById(R.id.user_name);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.user_sex);
        bioET = (EditText) findViewById(R.id.user_bio);
        headPictureIV = (ImageView) findViewById(R.id.head_picture);
        birthET = (EditText) findViewById(R.id.user_birth);
        phoneET = (EditText) findViewById(R.id.user_phone);
        emailET = (EditText) findViewById(R.id.user_email);
        Button changePwdBTN = (Button) findViewById(R.id.user_pwd);

        //显示昵称
        if (nickname == null || nickname.equals("")) {
            usernameET.setText("点击设置");
        } else {
            usernameET.setText(nickname);
        }

        //显示个人简介
        if (bio == null || bio.equals("")) {
            bioET.setText("点击设置");
        } else {
            bioET.setText(bio);
        }

        //显示生日
        if (birth == null) {
            birthET.setText("点击设置");
        } else {
            birthET.setText(birth);
        }

        //显示头像
        try {
            if (headPicture != null) {
                BitmapUtils bitmapUtils = new BitmapUtils();
                bitmapUtils.disPlay(headPictureIV, headPicture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //显示手机号
        if (phone == null || phone.equals("")) {
            phoneET.setText("点击设置");
        } else {
            phoneET.setText(phone);
        }

        //显示邮箱
        if (email == null) {
            emailET.setText("点击设置");
        } else {
            emailET.setText(email);
        }

        //显示手机/邮箱的验证状态
        updateVerifiedStatus();

        //修改昵称,个人简介,生日,头像,手机号,邮箱,密码
        usernameET.setOnClickListener(this);
        bioET.setOnClickListener(this);
        birthET.setOnClickListener(this);
        headPictureIV.setOnClickListener(this);
        phoneET.setOnClickListener(this);
        emailET.setOnClickListener(this);
        changePwdBTN.setOnClickListener(this);

        //显示性别
        if (sex != null) {
            switch (sex) {
                case "1":
                    radioGroup.check(R.id.user_sex_male);
                    break;
                case "2":
                    radioGroup.check(R.id.user_sex_female);
                    break;
                case "3":
                    radioGroup.check(R.id.user_sex_secret);
                    break;
                default:
                    break;
            }
        } else {
            radioGroup.check(R.id.user_sex_secret);
        }

        //修改性别
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.user_sex_male:
                        loginUser.setSex("1");
                        break;
                    case R.id.user_sex_female:
                        loginUser.setSex("2");
                        break;
                    case R.id.user_sex_secret:
                        loginUser.setSex("3");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 初始化手机/邮箱的验证状态
     * 如果修改了手机/邮箱，那么是否验证就会改变
     */
    private void updateVerifiedStatus() {
        boolean isValidPhone = loginUser.isMobilePhoneNumberVerified();
        boolean isValidEmail = loginUser.isEmailVerified();

        ImageView phoneIsValidIV = (ImageView) findViewById(R.id.phone_is_valid);
        ImageView emailIsValidIV = (ImageView) findViewById(R.id.email_is_valid);

        if (isValidPhone) {
            phoneIsValidIV.setVisibility(View.VISIBLE);
        } else {
            phoneIsValidIV.setVisibility(View.INVISIBLE);
        }
        if (isValidEmail) {
            emailIsValidIV.setVisibility(View.VISIBLE);
        } else {
            emailIsValidIV.setVisibility(View.INVISIBLE);
        }
    }

    //实现控件的监听，打开对应的对话框
    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.user_name:
                EditNameFragment editNameFragment = new EditNameFragment();
                bundle.putString("nickname", loginUser.getNickname());
                editNameFragment.setArguments(bundle);
                editNameFragment.show(getSupportFragmentManager(), "Edit_Name");
                break;
            case R.id.user_bio:
                EditBioFragment editBioFragment = new EditBioFragment();
                bundle.putString("bio", loginUser.getBio());
                editBioFragment.setArguments(bundle);
                editBioFragment.show(getSupportFragmentManager(), "Edit_Bio");
                break;
            case R.id.user_birth:
                EditBirthFragment editBirthFragment = new EditBirthFragment();
                bundle.putString("birth", loginUser.getBirth());
                editBirthFragment.setArguments(bundle);
                editBirthFragment.show(getSupportFragmentManager(), "Edit_Birth");
                break;
            case R.id.head_picture:
                PickPictureFragment ppFragment = new PickPictureFragment();
                ppFragment.show(getSupportFragmentManager(), "Pick_Picture");
                break;
            case R.id.user_phone:
                EditPhoneFragment editPhoneFragment = new EditPhoneFragment();
                bundle.putString("phone", loginUser.getMobilePhone());
                editPhoneFragment.setArguments(bundle);
                editPhoneFragment.show(getSupportFragmentManager(), "Edit_Phone");
                break;
            case R.id.user_email:
                EditEmailFragment editEmailFragment = new EditEmailFragment();
                bundle.putString("email", loginUser.getEmail());
                editEmailFragment.setArguments(bundle);
                editEmailFragment.show(getSupportFragmentManager(), "Edit_Email");
                break;
            case R.id.user_pwd:
                new EditPwdFragment().show(getSupportFragmentManager(), "Edit_Password");
                break;
            default:
                break;
        }
    }

    // 修改昵称对话框（确定修改）
    @Override
    public void onEditNameDialogPositiveClick(DialogFragment dialog, String newNickname, final String oldNickname) {
        loginUser.setNickname(newNickname);
        usernameET.setText(newNickname);

        Snackbar.make(layoutScroll, "昵称修改成功", Snackbar.LENGTH_LONG)
                .setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginUser.setNickname(oldNickname);
                        if (oldNickname.equals(""))
                            usernameET.setText("点击设置");
                        else
                            usernameET.setText(oldNickname);
                    }
                }).show();
    }

    // 修改昵称对话框（取消修改）
    @Override
    public void onEditNameDialogNegativeClick(DialogFragment dialog) {
        //取消修改
    }

    // 修改个人简介对话框（确定修改）
    @Override
    public void onEditBioDialogPositiveClick(DialogFragment dialog, String newBio, final String oldBio) {
        loginUser.setBio(newBio);
        bioET.setText(newBio);

        Snackbar.make(layoutScroll, "个人简介修改成功", Snackbar.LENGTH_LONG)
                .setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginUser.setBio(oldBio);
                        if (oldBio.equals(""))
                            bioET.setText("点击设置");
                        else
                            bioET.setText(oldBio);
                    }
                }).show();
    }

    // 修改个人简介对话框（取消修改）
    @Override
    public void onEditBioDialogNegativeClick(DialogFragment dialog) {

    }

    // 修改电话对话框（确定修改）
    @Override
    public void onEditPhoneDialogPositiveClick(DialogFragment dialog, final String phone, String smsCode) {

        BmobSMS.verifySmsCode(this, phone, smsCode, new VerifySMSCodeListener() {
            @Override
            public void done(cn.bmob.sms.exception.BmobException e) {
                if (e == null) {
                    loginUser.setMobilePhoneNumberVerified(true);
                    loginUser.setMobilePhone(phone);
                    //改变手机号的验证状态
                    updateVerifiedStatus();
                    Snackbar.make(layoutScroll, "手机号验证成功", Snackbar.LENGTH_LONG).show();
                    phoneET.setText(phone);
                } else {
                    loginUser.setMobilePhoneNumberVerified(false);
                    loginUser.setMobilePhone(phone);
                    //改变手机号的验证状态
                    updateVerifiedStatus();
                    String content = Constants.createErrorInfo(e.getErrorCode());
                    Snackbar.make(layoutScroll, "手机号验证失败:" + content, Snackbar.LENGTH_LONG).show();
                    phoneET.setText(phone);
                }
            }
        });

    }

    // 修改电话对话框（取消修改）
    @Override
    public void onEditPhoneDialogNegativeClick(DialogFragment dialog) {

    }

    // 修改邮箱对话框（确定修改）
    @Override
    public void onEditEmailDialogPositiveClick(DialogFragment dialog, String email) {
        loginUser.setEmail(email);
        loginUser.setEmailVerified(false);
        //改变邮箱的验证状态
        updateVerifiedStatus();
        emailET.setText(email);
    }

    // 修改邮箱对话框（取消修改）
    @Override
    public void onEditEmailDialogNegativeClick(DialogFragment dialog) {

    }

    // 修改生日对话框（确定修改）
    @Override
    public void onEditBirthDialogPositiveClick(DialogFragment dialog, String birthAfter, final String birthBefore) {
        loginUser.setBirth(birthAfter);
        birthET.setText(birthAfter);

        Snackbar.make(layoutScroll, "生日修改成功", Snackbar.LENGTH_LONG)
                .setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginUser.setBirth(birthBefore);
                        if (birthBefore.equals(""))
                            birthET.setText("点击设置");
                        else
                            birthET.setText(birthBefore);
                    }
                }).show();
    }

    // 修改生日对话框（取消修改）
    @Override
    public void onEditBirthDialogNegativeClick(DialogFragment dialog) {

    }

    // 修改密码对话框（确定修改）
    @Override
    public void onEditPwdDialogPositiveClick(DialogFragment dialog, String pwd) {
        loginUser.setPassword(pwd);

    }

    // 修改密码对话框（取消修改）
    @Override
    public void onEditPwdDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    protected void onPause() {
        try {
            UserInfoSync.saveLoginInfo(EditActivity.this, loginUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            UserInfoSync.upload(this, objectId, Constants.SP_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri uri;

                    if (requestCode == 1) {
                        uri = data.getData();
                    } else {
                        uri = previewUri;
                    }

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(uri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra("crop", "true");
                    // 裁剪框的比例，1：1
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 500);
                    intent.putExtra("outputY", 500);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, previewUri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 3);
                    } else {
                        Snackbar.make(layoutScroll, "没有裁剪图片程序", Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    String path = previewUri.toString();
                    realPath = path.substring(7, path.length());

                    try {
                        uploadHeadPicture(realPath);
                        Bitmap bitmap = BitmapFactory
                                .decodeStream(getContentResolver().openInputStream(previewUri));
                        headPictureIV.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCameraClick() {
        //启动相机
        File outputImage = new File(Environment.getExternalStorageDirectory(), "cameraImage.jpg");

        previewUri = Uri.fromFile(outputImage);

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, previewUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 2);
        } else {
            Snackbar.make(layoutScroll, "没有相机程序", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAlbumClick() {
        //启动相册
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File outputImage = new File(Environment.getExternalStorageDirectory(), "chooseImage.jpg");

            previewUri = Uri.fromFile(outputImage);

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, previewUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 1);
            } else {
                Snackbar.make(layoutScroll, "没有相册程序", Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            } else {
                Snackbar.make(layoutScroll, "你拒绝了存储空间权限申请", Snackbar.LENGTH_LONG)
                        .setAction("授予权限", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 123);
                            }
                        }).show();
            }
            if (isNotAskAgain) {
                Snackbar.make(layoutScroll, "你拒绝了存储空间权限申请", Snackbar.LENGTH_LONG)
                        .setAction("授予权限", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 123);
                            }
                        }).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED &&
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                boolean showRationale = shouldShowRequestPermissionRationale(permission);
                if (!showRationale) {
                    // 用户拒绝了带有“不再询问”的权限申请
                    isNotAskAgain = true;
                } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                    // 用户第一次拒绝了权限申请
                    // 向用户解释我们为什么要申请这个权限
                    showRationale(permission, R.string.permission_denied_storage);
                }
            }
        }
    }

    private void showRationale(String permission, int permissionDenied) {

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(permission)
                .setMessage(getString(permissionDenied) + "。请重新授权！")
                .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(EditActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                    }
                })
                .setNegativeButton("仍然拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    /**
     * 上传头像
     *
     * @param path 要上传头像的绝对地址
     */
    public void uploadHeadPicture(String path) {
        final ProgressDialog progressDialog = new ProgressDialog(layoutScroll.getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("图片上传中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();

        final BmobFile bmobFile = new BmobFile(new File(path));

        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                String headPictureUrl = bmobFile.getFileUrl();
                //SPUtils.putString(EditActivity.this, Constants.SP_FILE_NAME, Constants.SP_HEAD_PICTURE, headPictureUrl);
                loginUser.setAvatar(headPictureUrl);
                progressDialog.dismiss();
                Snackbar.make(layoutScroll, "上传成功 ", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(Integer value) {
                // TODO Auto-generated method stub
                progressDialog.setProgress(value);
            }
        });
    }

}