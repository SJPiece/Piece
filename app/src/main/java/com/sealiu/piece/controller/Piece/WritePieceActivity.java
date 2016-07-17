package com.sealiu.piece.controller.Piece;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sealiu.piece.R;
import com.sealiu.piece.model.Constants;
import com.sealiu.piece.model.Piece;
import com.sealiu.piece.utils.ImageLoader.BitmapUtils;
import com.sealiu.piece.utils.SPUtils;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class WritePieceActivity extends AppCompatActivity implements
        UrlFragment.UrlListener, View.OnClickListener {

    private static final String TAG = "WritePieceActivity";
    private static final int ERROR = 4;
    ImageButton addLinkBtn;
    ImageButton addImageBtn;
    CardView linkCard;
    TextView linkContent;
    ImageButton linkDeleteBtn;
    CardView imageCard;
    private Double mLatitude, mLongitude;
    private String mLocationName;
    private TextView myLocationTV;
    private ImageView headPictureIV;
    private TextView nickNameTV;
    private Spinner visibilitySpinner;
    private EditText pieceContentET;
    private NestedScrollView snackBarHolderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_piece);

        snackBarHolderView = (NestedScrollView) findViewById(R.id.layout_holder);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.write_piece);

        myLocationTV = (TextView) findViewById(R.id.my_location_name);
        nickNameTV = (TextView) findViewById(R.id.user_nickname);
        headPictureIV = (ImageView) findViewById(R.id.user_head_picture);
        visibilitySpinner = (Spinner) findViewById(R.id.visibility);
        pieceContentET = (EditText) findViewById(R.id.piece_content);

        linkCard = (CardView) findViewById(R.id.link_card);
        linkContent = (TextView) findViewById(R.id.link_content);
        linkDeleteBtn = (ImageButton) findViewById(R.id.delete_link);

        imageCard = (CardView) findViewById(R.id.image_card);

        addImageBtn = (ImageButton) findViewById(R.id.add_image_btn);
        addLinkBtn = (ImageButton) findViewById(R.id.add_link_btn);

        addImageBtn.setOnClickListener(this);
        addLinkBtn.setOnClickListener(this);

        linkDeleteBtn.setOnClickListener(this);
        initUI();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_piece, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_piece:
                // 发送编写的Piece

                final String objectId = SPUtils.getString(this, Constants.SP_FILE_NAME, Constants.SP_USER_OBJECT_ID, "");
                final String pieceContent = pieceContentET.getText().toString();
                final int visibilityPosition = visibilitySpinner.getSelectedItemPosition();

                if (pieceContent.equals("")) {
                    Snackbar.make(snackBarHolderView, "请填写内容", Snackbar.LENGTH_LONG).show();
                    break;
                }

                //向Bmob后台写数据
                Log.i(TAG, "用户ID：" + objectId + "; 可见范围：" + visibilityPosition + "; 纸条内容：" + pieceContent);
                Piece piece = new Piece(objectId, pieceContent, mLatitude, mLongitude, visibilityPosition);
                piece.setType(1);
                piece.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("errorCode", e.getErrorCode());
                            setResult(ERROR, intent);
                        }
                        finish();
                    }
                });

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 初始化界面显示（头像，昵称，位置信息）
     */
    private void initUI() {

        mLatitude = getIntent().getDoubleExtra("LAT", 0);
        mLongitude = getIntent().getDoubleExtra("LNG", 0);
        mLocationName = getIntent().getStringExtra("LOC");

        String detailPosition = mLocationName + " (" + mLatitude + " ," + mLongitude + ")";
        myLocationTV.setText(detailPosition);

        String nickName = SPUtils.getString(this, Constants.SP_FILE_NAME, Constants.SP_NICKNAME, "");
        final String headPicture = SPUtils.getString(this, Constants.SP_FILE_NAME, Constants.SP_HEAD_PICTURE, "");


        if (!nickName.equals("")) {
            nickNameTV.setText(nickName);
        }

        if (headPicture != null) {
            BitmapUtils bitmapUtils = new BitmapUtils();
            bitmapUtils.disPlay(headPictureIV, headPicture);
        }
    }

    @Override
    public void onUrlPositiveClick(String url) {
        Log.i(TAG, url);
        linkCard.setVisibility(View.VISIBLE);
        linkContent.setText(url);
    }

    @Override
    public void onUrlNegativeClick() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_image_btn:
                break;
            case R.id.add_link_btn:
                new UrlFragment().show(getSupportFragmentManager(), "url");
                break;
            case R.id.delete_link:
                linkContent.setText("");
                linkCard.setVisibility(View.GONE);
                break;
        }
    }
}
