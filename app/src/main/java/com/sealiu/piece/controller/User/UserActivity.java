package com.sealiu.piece.controller.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sealiu.piece.R;
import com.sealiu.piece.controller.Piece.PieceAdapter;
import com.sealiu.piece.model.LoginUser;
import com.sealiu.piece.model.Piece;

import java.util.List;
import java.util.Set;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * 展示用户发送过的小纸条（类似朋友圈）
 * 点击用户头像进入查看页面
 */
public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private static final String LAYOUT_MANAGER_FLAG = "LayoutManager";
    Menu menu;
    int[] drawableArray = new int[]{
            R.drawable.ic_view_stream_white_24dp,
            R.drawable.ic_view_module_white_24dp,
            R.drawable.ic_view_quilt_white_24dp
    };
    int index;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_piece_recycler_view);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, EditActivity.class));
            }
        });

        // set RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // set LayoutManager
        index = savedInstanceState != null ? savedInstanceState.getInt(LAYOUT_MANAGER_FLAG) : 0;

        initUI();
        // fetch data, and set adapter
        setAdapter();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> piecesSet = SP.getStringSet("pref_piece_nearby_key", null);
        if (piecesSet != null && piecesSet.contains("5")) {
            // 广告
            AdView mAdView = (AdView) findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        initUI();
    }

    /**
     * 初始化界面显示
     */
    private void initUI() {
        loginUser = UserInfoSync.getLoginInfo(UserActivity.this);
        String nickName = loginUser.getNickname();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (nickName != null && !nickName.equals("")) {
                getSupportActionBar().setTitle(nickName);
            } else {
                getSupportActionBar().setTitle(loginUser.getUsername());
            }
        }
    }

    private void setAdapter() {
        String userObjectId = loginUser.getObjectId();
        if (userObjectId != null) {
            BmobQuery<Piece> query = new BmobQuery<>();
            query.addWhereEqualTo("authorID", userObjectId);
            query.setLimit(1000);
            query.order("-createdAt,-updatedAt");
            query.findObjects(new FindListener<Piece>() {
                @Override
                public void done(List<Piece> list, BmobException e) {
                    mAdapter = new PieceAdapter(list);
                    mRecyclerView.setAdapter(mAdapter);
                }//done
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAYOUT_MANAGER_FLAG, index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pieces, menu);
        this.menu = menu;
        setLayoutManager();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_layout:
                index = (index + 1) % 3;
                setLayoutManager();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLayoutManager() {
        menu.findItem(R.id.menu_layout).setIcon(drawableArray[index]);
        switch (index) {
            case 0:
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                break;
            case 1:
                mLayoutManager = new GridLayoutManager(this, 2);
                mRecyclerView.setLayoutManager(mLayoutManager);
                break;
            case 2:
                int orientation = this.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mLayoutManager = new StaggeredGridLayoutManager(3, 1);
                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = new StaggeredGridLayoutManager(2, 1);
                }
                mRecyclerView.setLayoutManager(mLayoutManager);
                break;
        }
    }
}
