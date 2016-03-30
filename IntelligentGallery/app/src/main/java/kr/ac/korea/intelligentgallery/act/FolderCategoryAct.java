package kr.ac.korea.intelligentgallery.act;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import kr.ac.korea.astuetz.PagerSlidingTabStrip;
import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.fragment.CategoryFragInAlbum;
import kr.ac.korea.intelligentgallery.fragment.FolderFrag;
import kr.ac.korea.intelligentgallery.listener.OnBackPressedListener;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.SharedPreUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;

/**
 * FolderCategoryAct : MainAct에서 폴더를 선택하였을 때 실행되는 Activity
 * 두 개의 탭을 가진 ViewPager로 구성되어있으며 각각은 FolderFrag와 CategoryFragInAlbum이다
 */
public class FolderCategoryAct extends ParentAct implements View.OnClickListener {

    private final static String TAG = "FolderCategoryAct::";

    public Toolbar toolbar;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private ViewPager viewPagerMain;
    private MainPagerAdapter mainPagerAdapter;

    Album albumFromMainAct;
    String albumBucketId;
    String pathFromMainAct;

    private FolderFrag folderFrag;
    private CategoryFragInAlbum categoryFragInAlbum;

    GetImagesInAlbum getImagesInAlbum;
    boolean scaning = false;
    int totalCountInAlbum;
    int totalCountInAlbumHavingGpsInfo;

    int start = 0;
    int limit = 100;

    ArrayList<ImageFile> imageFilesNotClassified = new ArrayList<>();

    public static String imageOrderby = SharedPreUtil.getInstance().getSharedPrefs().getString(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, MediaStore.Images.Media.DATE_TAKEN);

    public final static String ttttt = "InsertingExternal";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_category);

        pathFromMainAct = getIntent().getStringExtra("album_path");
        DebugUtil.showDebug("FolderCategoryAct, pathFromMainAct:: " + pathFromMainAct);

        if (!TextUtil.isNull(pathFromMainAct)) {
            albumFromMainAct = FileUtil.getAlbumsInSepecficLocation(this, pathFromMainAct);
            DebugUtil.showDebug("FolderCategoryAct, pathFromMainAct, albumBucket id :: " + albumFromMainAct.getId());
            totalCountInAlbum = FileUtil.getImagesCount(FolderCategoryAct.this, albumFromMainAct);
            Log.d(ttttt, "totalCountInAlbum:: "+totalCountInAlbum);

            totalCountInAlbumHavingGpsInfo = FileUtil.getImagesHavingGPSInfoInSpecificAlbum(this, albumFromMainAct).size();
            Log.d(ttttt, "totalCountInAlbumHavingGpsInfo:: "+totalCountInAlbumHavingGpsInfo);

            imageFilesNotClassified = FileUtil.getImagesHavingGPSInfoInSpecificAlbum(this, albumFromMainAct);
            for(ImageFile imgfile : imageFilesNotClassified){
                DebugUtil.showDebug(FolderCategoryAct.ttttt +",:: 위치정보가 있는 모든  아이디:: " + imgfile.getId());
            }
            DebugUtil.showDebug(FolderCategoryAct.ttttt + "====================================");




            getImagesInAlbum = new GetImagesInAlbum(FolderCategoryAct.this);
        }

        //최상단의 툴바
        toolbar = (Toolbar) findViewById(R.id.toolbar_folder_category);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));

        //pagerAdapter
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        //viewPager
        viewPagerMain = (ViewPager) findViewById(R.id.view_pager_flodercategoryfrag);
        viewPagerMain.setAdapter(mainPagerAdapter);
        viewPagerMain.setOffscreenPageLimit(2);//2개까지는 지워지지 않도록 함

        pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_sliding_tab_strip_make_event);
        pagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelOffset(R.dimen.dp_3));
        pagerSlidingTabStrip.setIndicatorColor(getResources().getColor(R.color.c_ffffffff));
        pagerSlidingTabStrip.setTextColorResource(R.color.c_ffffffff);
        pagerSlidingTabStrip.setShouldExpand(true);
        pagerSlidingTabStrip.setDividerColor(getResources().getColor(android.R.color.transparent));
        pagerSlidingTabStrip.setViewPager(viewPagerMain);
        pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0://폴더 탭 선택 시
//                        break;
                    case 1://카테고리 탭 선택 시
                        //메인에서 받아온 폴더이름 표시
                        if (!TextUtil.isNull(pathFromMainAct)) {
                            toolbar.setTitle(FileUtil.getFileNameFromPath(pathFromMainAct));
                        }
                        break;
                    default:
                        if (!TextUtil.isNull(pathFromMainAct))
                            toolbar.setTitle(FileUtil.getFileNameFromPath(pathFromMainAct));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getImagesInAlbum != null) {
            getImagesInAlbum.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            folderFrag = new FolderFrag(albumFromMainAct, totalCountInAlbum);
            categoryFragInAlbum = new CategoryFragInAlbum(albumFromMainAct);
        }
    }

    @Override
    protected void onDestroy() {
        if (getImagesInAlbum != null) {
            getImagesInAlbum.cancel(true);
        }
        super.onDestroy();
    }


    // tab PagerAdapter
    public class MainPagerAdapter extends FragmentStatePagerAdapter {
        private String[] titleArray = {
                "사진", "카테고리"
        };

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return titleArray[0];
            else
                return titleArray[1];
        }

        @Override
        public int getCount() {
            return titleArray.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment returnFrag = new Fragment();
            switch (position) {
                case 0:
                    // FolderFrag 이동
                    if (!TextUtil.isNull(pathFromMainAct)) {
                        toolbar.setTitle(FileUtil.getFileNameFromPath(pathFromMainAct));
                        returnFrag = folderFrag;
                        break;
                    }
                case 1:
                    // CategoryFragInAlbum 이동
                    if (!TextUtil.isNull(pathFromMainAct)) {
                        returnFrag = categoryFragInAlbum;
                        break;
                    }
                default: {
                    // FolderFrag 이동
                    if (!TextUtil.isNull(pathFromMainAct)) {
                        toolbar.setTitle(FileUtil.getFileNameFromPath(pathFromMainAct));
                        returnFrag = folderFrag;
                    }
                }
            }
            return returnFrag;
        }

        @Override
        public int getItemPosition(Object object) {
            int position;
            if (object instanceof FolderFrag) {
                position = 0;
            } else
                position = 1;
            return position;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugUtil.showDebug("FolderCategoryAct, onCreateOptionsMenu() : ");
        MenuInflater inflater = getMenuInflater();

        if (!FolderFrag.isLongClicked) {
            inflater.inflate(R.menu.menu_folder, menu);
        }
        if (FolderFrag.isLongClicked) {
            inflater.inflate(R.menu.menu_folder_long_clicked, menu);
        }

//        if (!FolderFrag.isLongClicked) {
//            if (!CategoryFragInAlbum.isLongClicked)
//                inflater.inflate(R.menu.menu_folder, menu);
//            else
//                inflater.inflate(R.menu.menu_category_long_clicked, menu);
//        }
//        if (FolderFrag.isLongClicked) {
//            if (!CategoryFragInAlbum.isLongClicked)
//                inflater.inflate(R.menu.menu_folder_long_clicked, menu);
//            else
//                inflater.inflate(R.menu.menu_folder, menu);
//        }

        return true;
    }

    // 안드로이드 기기의 뒤로가기 버튼
    @Override
    public void onBackPressed() {
        this.overridePendingTransition(0, 0);
        DebugUtil.showDebug("FolderCategoryAct, onBackPressed()");
        List<Fragment> fragments = getSupportFragmentManager().getFragments();


        if (!FolderFrag.isLongClicked) {
            super.onBackPressed();
        }

        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof OnBackPressedListener) {
                    if (FolderFrag.isLongClicked) {
                        ((OnBackPressedListener) fragment).onBackPressed();
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
        DebugUtil.showDebug("FolderCategoryFrag, onClick(), v.getId() :" + v.getId());
        switch (v.getId()) {
            case R.drawable.ic_backkey:
                onBackPressed();
                break;
        }
    }

    public void checkScaning(ArrayList<ImageFile> imageFiles) {
        if (scaning) {
            if (folderFrag != null)
                folderFrag.setImagesInFolder(imageFiles);
            if (categoryFragInAlbum != null)
                categoryFragInAlbum.setImagesInFolder(imageFiles);
            getImagesInAlbum = new GetImagesInAlbum(this);
            getImagesInAlbum.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            if (imageFiles == null)
                return;
            if (folderFrag != null)
                folderFrag.setImagesInFolder(imageFiles);
            if (categoryFragInAlbum != null)
                categoryFragInAlbum.setImagesInFolder(imageFiles);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_IMAGES_REFRESH) {
                ArrayList<ImageFile> images = (ArrayList<ImageFile>) data.getSerializableExtra("imagesInSameFolder");
                Integer imageSize = data.getIntExtra("imagesInSameFoder_Count", 0);
                DebugUtil.showDebug("FolderCategoryAct, images::" + imageSize);
                DebugUtil.showDebug("Bucket id :: " + data.getStringExtra("bucketid"));

                albumFromMainAct = FileUtil.getAlbumsInSepecficLocation(this, pathFromMainAct);
                ArrayList<ImageFile> updatedImageFiles = FileUtil.getImages(FolderCategoryAct.this, albumFromMainAct);

                folderFrag.imageAdapter.addItems(updatedImageFiles);

//                categoryFragInAlbum.imageAdapter.addItems(updatedCategories);
                categoryFragInAlbum.setImagesInFolder(updatedImageFiles);
            }


        }
    }

    public class GetImagesInAlbum extends AsyncTask<Void, Void, ArrayList<ImageFile>> {

        private final static String TAG = "GetImagesInAlbum::";

        private Context context;
        private Exception mLastError = null;

        public GetImagesInAlbum(Context context) {
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
//            DebugUtil.showDebug(TAG + "onPreExecute::");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);
        }

        @Override
        protected ArrayList<ImageFile> doInBackground(Void... params) {
//            DebugUtil.showDebug(TAG + "doInBackground::");

            ArrayList<ImageFile> imageFiles;

            if (totalCountInAlbum <= limit) {
                scaning = false;
                imageFiles = FileUtil.getImages(FolderCategoryAct.this, albumFromMainAct, start, totalCountInAlbum);
            } else {
                if (start == 0) {
                    scaning = true;
                    imageFiles = FileUtil.getImages(FolderCategoryAct.this, albumFromMainAct, start, limit);
                } else if (start >= totalCountInAlbum) {
                    scaning = false;
                    start = 0;
                    return null;
                } else {
                    scaning = true;
                    imageFiles = FileUtil.getImages(FolderCategoryAct.this, albumFromMainAct, start, limit);
                }
                start += limit;
            }

            return imageFiles;

        }

        @Override
        protected void onPostExecute(ArrayList<ImageFile> imageFiles) {
            super.onPostExecute(imageFiles);
//            DebugUtil.showDebug(TAG + "onPostExecute::");
            checkScaning(imageFiles);
        }

        @Override
        protected void onCancelled() {
            DebugUtil.showDebug(TAG + "onCancelled::");
        }
    }


}