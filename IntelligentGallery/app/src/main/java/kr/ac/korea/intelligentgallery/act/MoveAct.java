package kr.ac.korea.intelligentgallery.act;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.MoveActAdapter;
import kr.ac.korea.intelligentgallery.broadcastReceiver.MediaScannerBroadcastReceiver;
import kr.ac.korea.intelligentgallery.common.ExpandableHeightGridView;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

public class MoveAct extends ParentAct {

    private Toolbar toolbar;
    private int  copyOrMove;

    // 앨범 그리드 뷰
    private ExpandableHeightGridView mGridViewFolder;
    private MoveActAdapter albumAdapter;
    private ArrayList<Album> albums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        toolbar = (Toolbar) findViewById(R.id.toolbar_move_act);
        toolbar.inflateMenu(R.menu.menu_move_act);
        setSupportActionBar(toolbar);

        copyOrMove = getIntent().getIntExtra("moveOrCopy", 0);
        if(copyOrMove == 0) {
            getSupportActionBar().setTitle("이동");
        } else {
            getSupportActionBar().setTitle("복사");
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));

        //앨범 로드
        loadAlbums();
    }

    //앨범 로드하는 부분
    private void loadAlbums() {

        if (!MediaScannerBroadcastReceiver.mMedaiScanning) {
            DebugUtil.showDebug(MainAct.currentMission, "MainAct, loadAlbums()", "현재 풀스캔 중이지 않음, 풀 스캔 시작");
            FileUtil.callBroadCast(this);
        } else {
            DebugUtil.showDebug(MainAct.currentMission, "MainAct, loadAlbums()", "현재 풀스캔 중");
        }

        albums = FileUtil.getAlbums(this, MainAct.albumOrderBy);

        if (albums == null) {
            DebugUtil.showDebug("MainAct, loadAlbums(), albums are null");
            return;
        }

        albumAdapter = new MoveActAdapter(this, this, albums, copyOrMove);

        // Set the grid adapter
        mGridViewFolder = (ExpandableHeightGridView) findViewById(R.id.gridViewMoveAct);

        mGridViewFolder.setNumColumns(MainAct.GridViewFolderNumColumns);
        mGridViewFolder.setExpanded(true);
        mGridViewFolder.setAdapter(albumAdapter);

        // Set the onClickListener
//        mGridViewFolder.setOnItemClickListener(this);
//        mGridViewFolder.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_move_act, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            //뒤로가기
            case android.R.id.home:
                DebugUtil.showDebug("MoveAct, onOptionItemSelected, case android.R.id.home:");
//                finish();
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
