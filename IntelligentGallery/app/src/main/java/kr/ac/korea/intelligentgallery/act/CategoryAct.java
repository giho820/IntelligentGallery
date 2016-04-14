package kr.ac.korea.intelligentgallery.act;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.List;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.fragment.CategoryFrag;
import kr.ac.korea.intelligentgallery.listener.OnBackPressedListener;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

/**
 * CategoryAct : MainAct에서 카테고리를 선택하였을 때 실행되는 Activity
 * CategoryFrag를 가지고 있음
 */
public class CategoryAct extends ParentAct implements View.OnClickListener {

    public Toolbar toolbar;
    private CategoryFrag categoryFrag;
    public List<ImageFile> imagesInCategory = null;

    Album albumFromMainAct;
    String pathFromMainAct;
    int cIdFromMainAct = -1;
    String CnameFromMainAct ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        //MainAct에서 클릭한 카테고리의 cID를 받아옴
        cIdFromMainAct = getIntent().getIntExtra("clickedCid", 0);
        CnameFromMainAct = getIntent().getStringExtra("ClickedCname");

        if (cIdFromMainAct >= 0) {

            DebugUtil.showDebug("CategoryAct, onCreate(), received cid : " + cIdFromMainAct);

            //최상단의 툴바
            toolbar = (Toolbar) findViewById(R.id.toolbar_folder_category);
            toolbar.setTitle("" + CnameFromMainAct);
            toolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));
            setSupportActionBar(toolbar);
        }

        // CategoryFrag 보여주기
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        categoryFrag = new CategoryFrag(cIdFromMainAct);
        fragmentTransaction.add(R.id.view_pager_flodercategoryfrag, categoryFrag);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugUtil.showDebug("CategoryAct, onCreateOptionsMenu() : ");
        MenuInflater inflater = getMenuInflater();

        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_category);
        if(CategoryFrag.isLongClicked){
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_category_long_clicked);
        }
//        if (!CategoryFrag.isLongClicked)
//            inflater.inflate(R.menu.menu_category, menu);
//        else
//            inflater.inflate(R.menu.menu_category_long_clicked, menu);

        return true;
    }

    /**
     * 안드로이드 기기의 뒤로가기 버튼
     */
    @Override
    public void onBackPressed() {
        this.overridePendingTransition(0, 0); //에니메이션 없앰

        DebugUtil.showDebug("CategoryAct, onBackPressed()");
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

//        if (!FolderFrag.isLongClicked) {
//            super.onBackPressed();
//        }
//
//        if (fragments != null) {
//            for (Fragment fragment : fragments) {
//                if (fragment instanceof OnBackPressedListener) {
//
//
//                    if(CategoryFrag.isLongClicked) {
//                        DebugUtil.showDebug("CategoryAct, onBackPressed() CategoryFrag.isLongClicked::" + CategoryFrag.isLongClicked +", 바꿔줘야함");
//                    }
//                }
//            }
//        }

        if (!CategoryFrag.isLongClicked) {
            super.onBackPressed();
        } else {
            CategoryFrag.isLongClicked = false;

//            //체크 해제
            for (int i = 0; i<categoryFrag.imageAdapter.items.size(); i++){
                categoryFrag.imageAdapter.getItem(i).setIsChecked(false);
            }
            categoryFrag.imageAdapter.notifyDataSetChanged();


            //change menu
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_category);

            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof OnBackPressedListener) {
                        if (CategoryFrag.isLongClicked) {
                            ((OnBackPressedListener) fragment).onBackPressed();
                        }
                    }
                }
            }
        }
    }

    /**
     * 네이게이션 버튼에 대한 동작
     */
    @Override
    public void onClick(View v) {
        DebugUtil.showDebug("CategoryFrag, onClick(), v.getId() :" + v.getId());
        switch (v.getId()) {
            case R.drawable.ic_backkey:
                onBackPressed();
                break;
        }
    }


}