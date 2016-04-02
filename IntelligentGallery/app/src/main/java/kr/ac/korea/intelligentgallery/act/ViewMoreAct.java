package kr.ac.korea.intelligentgallery.act;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.ViewMoreActImageAdapter;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;

/**
 * CategoryAct : MainAct에서 카테고리를 선택하였을 때 실행되는 Activity
 * CategoryFrag를 가지고 있음
 */
public class ViewMoreAct extends ParentAct implements View.OnClickListener {

    public Toolbar toolbar;
    public GridView gridView;
    public ArrayList<ImageFile> imageFiles = null;
    public ViewMoreActImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmore);
        gridView = (GridView) findViewById(R.id.gridViewViewMore);

        //카테고리 명
        String categoryName = getIntent().getStringExtra("categoryName");
        //최상단의 툴바
        toolbar = (Toolbar) findViewById(R.id.toolbar_view_more);
        if(!TextUtil.isNull(categoryName)) {
            toolbar.setTitle(categoryName);
        } else{
            toolbar.setTitle("카테고리명");
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));
        setSupportActionBar(toolbar);

        imageFiles = (ArrayList<ImageFile>) getIntent().getSerializableExtra("categroyImages");

        if (imageFiles == null) {
            return;
        }

        if (imageFiles != null) {
            imageAdapter = new ViewMoreActImageAdapter(this, imageFiles);
            gridView.setAdapter(imageAdapter);
            gridView.setNumColumns(MainAct.GridViewFolderNumColumns);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugUtil.showDebug("ViewMoreAct, onCreateOptionsMenu() : ");
//        MenuInflater inflater = getMenuInflater();
//
//        if (!CategoryFrag.isLongClicked)
//            inflater.inflate(R.menu.menu_category, menu);
//        else
//            inflater.inflate(R.menu.menu_category_long_clicked, menu);

        return true;
    }

    /**
     * 안드로이드 기기의 뒤로가기 버튼
     */


    /**
     * 네이게이션 버튼에 대한 동작?
     */
    @Override
    public void onClick(View v) {
        DebugUtil.showDebug("ViewMoreAct, onClick(), v.getId() :" + v.getId());
        switch (v.getId()) {
            case R.drawable.ic_backkey:
                onBackPressed();
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}