package kr.ac.korea.intelligentgallery.act;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.SelectCoverImageAdapter;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

/**
 * Created by kiho on 2016. 3. 22..
 */
public class SelectCoverAct extends ParentAct{
    private Toolbar toolbar;

    public String path; //이전 액티비티에서 넘겨준 앨범의 경로,
    public Album album;
    public ArrayList<ImageFile> imagesInFolder = null;//뷰 아이템의 리스트, 시간 순서대로 정렬해야함
    public static Set<Integer> selectedPositions = null; //그리드 뷰에서 선택된 것의 포지션을 중복하지 않기 위해 집합으로 저장(순서 저장이 안되는 단점 해결할 것 )
    public static List<Integer> selectedPositionsList = null; //그리드 뷰에서 선택된 것들을 중복을하지 않고 담기위한 자료형
    public GridView gridViewSelectCoverImage; //사진들이 담겨진 그리드 뷰
    public SelectCoverImageAdapter imageAdapter;
    public static Integer selectedPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selct_cover_act);

        toolbar = (Toolbar) findViewById(R.id.toolbar_select_cover_act);
        toolbar.inflateMenu(R.menu.menu_move_act); //빈 것
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));

        path = getIntent().getStringExtra("coverImages change album");
        album = FileUtil.getAlbumsInSepecficLocation(this, path);

        DebugUtil.showDebug("SelectCoverAct, onCreate::" + album.getPath());

        getSupportActionBar().setTitle(album.getName());

        gridViewSelectCoverImage = (GridView) findViewById(R.id.gridViewSelectCoverAct);
        imageAdapter = new SelectCoverImageAdapter(this, this);
        gridViewSelectCoverImage.setAdapter(imageAdapter);

        ArrayList<ImageFile> updatedImageFiles = FileUtil.getImages(this, album);
        imageAdapter.addItems(updatedImageFiles);

        imageAdapter.getItem(selectedPos);
        DebugUtil.showDebug("");


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
                DebugUtil.showDebug("SelectCoverAct, onOptionItemSelected, case android.R.id.home:");
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
