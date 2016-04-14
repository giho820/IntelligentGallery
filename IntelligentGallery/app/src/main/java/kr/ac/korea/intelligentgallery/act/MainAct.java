package kr.ac.korea.intelligentgallery.act;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.AlbumAdapter;
import kr.ac.korea.intelligentgallery.adapter.CategroyAdapter;
import kr.ac.korea.intelligentgallery.asynctask.ClassifyingWhenExternalImagesExistAsyncTask;
import kr.ac.korea.intelligentgallery.broadcastReceiver.MediaScannerBroadcastReceiver;
import kr.ac.korea.intelligentgallery.common.Definitions;
import kr.ac.korea.intelligentgallery.common.ExpandableHeightGridView;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;
import kr.ac.korea.intelligentgallery.dialog.CommonDialog;
import kr.ac.korea.intelligentgallery.dialog.FileDialog;
import kr.ac.korea.intelligentgallery.intelligence.Sample.Model.ContentScoreData;
import kr.ac.korea.intelligentgallery.listener.CommonDialogListener;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.MediaScanFile;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;
import kr.ac.korea.intelligentgallery.util.SharedPreUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


public class MainAct extends ParentAct implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    // 데이터베이스 헬퍼
    private DatabaseHelper databaseHelper;

    private Toolbar toolbar;

    private TextView tvFoldersNum;
    private TextView tvCategorysNum;
    private TextView tvCategoryProgrssCount;

    // 롱클릭 여부를 확인하는 변수
    public static boolean longClicked = false;
    private LinearLayout linearLayoutMainCategorySection;

    // 앨범 그리드 뷰
    private ExpandableHeightGridView mGridViewFolder;
    private AlbumAdapter albumAdapter;
    private ArrayList<Album> albums;
    public static int GridViewFolderNumColumns = 3; //앨범 그리드 뷰 에서 한 줄에 보여줄 앨범의 갯수

    public static String albumOrderBy = "";

    // 카테고리 그리드뷰
    private ExpandableHeightGridView mGridViewCategory;
    private CategroyAdapter mCategroyAdapterCategory;
    private ArrayList<ImageFile> gridItemsCategory;

    public BroadcastReceiver mBroadcastReceiver;
    private Integer updateTermOfLoadingCategory = 0;


    //폴더 추가 다이얼로그
    FileDialog fileDialog;

    //커버이미지 변경 대상
    int selectedIdxInCoverSelect = 0;

    // 방금 찍은 사진에 대한 처리
    public static String currentMission = "카메라 바로 나오기";
    public static Uri mImageCaptureUri = null;

    private MediaScanFile mediaScanFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.showDebug("MainAct, onCreate()");

        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getInstacnce(this);

        SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.DATE_TAKEN + " desc");
        albumOrderBy = SharedPreUtil.getInstance().getSharedPrefs().getString(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.DATE_TAKEN + " desc");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("사진");

        tvFoldersNum = (TextView) findViewById(R.id.textViewFolderCount); // 폴더 영역의 폴더의 개수를 나타내주는 TextView
        tvCategorysNum = (TextView) findViewById(R.id.textViewCategoryFolderCount);// 카테고리 영역의 폴더의 개수를 나타내주는 TextView
        tvCategoryProgrssCount = (TextView) findViewById(R.id.textViewCurrentClassifiedCount);

        // 롱클릭 시 사라질 카테고리 영역을 감싸는 LinearLayout
        linearLayoutMainCategorySection = (LinearLayout) findViewById(R.id.linearLayoutHidenSectionWhenSelectionMode);

        // set CommonLoadingDialog to show loading progressbar
        setLoading(this);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTermOfLoadingCategory++;
                tvCategoryProgrssCount.setText(DatabaseCRUD.getImagesIdsInInvertedIndexDb().size() + " / " + FileUtil.getAllImageFilesThatHaveGPSInfoCount(MainAct.this));
                if (updateTermOfLoadingCategory % 4 == 0) {
                    loadCategory();
                    mCategroyAdapterCategory.notifyDataSetChanged();
                } else {
                    if (SharedPreUtil.getInstance().getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == true) {
                        tvCategoryProgrssCount.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.classifying");
        registerReceiver(mBroadcastReceiver, intentFilter);

        //기타 사진이 아닌 파일들에 대한 적용
        FileUtil.updateMediaStorageQuery(this);
        //사진은 적용이 된다
        if (!MediaScannerBroadcastReceiver.mMedaiScanning) {
            DebugUtil.showDebug(MainAct.currentMission, "mainAct, onResume()", "현재 풀스캔 중이지 않음, 풀스캔 시작");
            FileUtil.callBroadCast(this);
            //Full Scan 강제 시작
//            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()))); //킷캣 부터는 다른 코드를 써야한다 SecurityException: Permission Denial: not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
            DebugUtil.showDebug("MainAct, onActivityResult(), ACTION_MEDIA_MOUNTED, sendBroadcast 시작");

            //kitkat 적용 테스트
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.parse("file://" + Environment.getExternalStorageDirectory()); //out is your output file
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }


        } else {
            DebugUtil.showDebug(MainAct.currentMission, "mainAct, onResume()", "현재 풀스캔 중");
        }

        //앨범 로드
        loadAlbums();

//                Integer fixedCoverImageId = SharedPreUtil.getInstance().getIntPreference(SharedPreUtil.ALBUM_COVER_IMAGE_ID);
//                if(fixedCoverImageId != 0){
//                    DebugUtil.showDebug("FileUtil, getAlbums, 고정된 앨범 커버 이미지 아이디 : " + fixedCoverImageId);
//                    album.setCoverID(fixedCoverImageId);
//                    albumCoverImagePath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + fixedCoverImageId));
//                    album.setCoverImagePath(albumCoverImagePath);
//                }

        //카테고리 로드
        loadCategory();

        //최초의 분류가 진행이 되고나서
        if (SharedPreUtil.getInstance().getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == true) {

            ArrayList<Integer> dbImages = new ArrayList<>();
            ArrayList<ImageFile> mediaImages = new ArrayList<>();
            ArrayList<Integer> uselessDids = new ArrayList<>();
            dbImages = DatabaseCRUD.getImagesIdsInInvertedIndexDb();
            mediaImages = FileUtil.getImagesHavingGPSInfo(this);
            int dbImageCnt = dbImages.size();
            int mediaImageCnt = mediaImages.size();
            if (dbImageCnt > mediaImageCnt) {
                DebugUtil.showDebug("MainAct, onResume(), 디비에 있는 사진 > 미디어 디비에 있는 사진, 디비 삭제 진행");
                //외부에서 사진이 지워져서 필요없는 사진이 DB에 남아았다면 이를 제거하는 함수
                DiLabClassifierUtil.deleteUselessDB(this);
                loadCategory();
            } else if (dbImageCnt < mediaImageCnt) {
                DebugUtil.showDebug("MainAct, onResume(), 디비에 있는 사진 < 미디어 디비에 있는 사진, 분류 안한 이미지 찾아서 분류 진행");
                ArrayList<ImageFile> imageFilesThatHaveGPSInfo = new ArrayList<>();
                ArrayList<ImageFile> imageFilesThatNeedToBeClassified = new ArrayList<>();
                imageFilesThatHaveGPSInfo = FileUtil.getImagesHavingGPSInfo(this);
                if (imageFilesThatHaveGPSInfo != null && imageFilesThatHaveGPSInfo.size() > 0) {
                    imageFilesThatNeedToBeClassified = FileUtil.getImagesHavingGPSInfoButNotInInvertedIndex(imageFilesThatHaveGPSInfo);
                    if (imageFilesThatNeedToBeClassified != null && imageFilesThatNeedToBeClassified.size() > 0) {
                        new ClassifyingWhenExternalImagesExistAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageFilesThatNeedToBeClassified);
                    }
                }
            } else {
                DebugUtil.showDebug("MainAct, onResume(), 디비에 있는 사진 == 미디어 디비에 있는 사진, 분류기 동작 안함");
            }
        }
    }


    //앨범 로드하는 부분
    private void loadAlbums() {

        albums = FileUtil.getAlbums(this, albumOrderBy);

        if (albums == null) {
            DebugUtil.showDebug("MainAct, loadAlbums(), albums are null");
            return;
        }

        tvFoldersNum.setText("" + albums.size());
        tvFoldersNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("gg, MainAct, loadAlbum(), CoverImage DB : " + DatabaseCRUD.selectCoverImageDBQuery());
            }
        });
        albumAdapter = new AlbumAdapter(this, this, albums);

        // Set the grid adapter
        mGridViewFolder = (ExpandableHeightGridView) findViewById(R.id.gridViewMainFolder);
        mGridViewFolder.setNumColumns(GridViewFolderNumColumns);
        mGridViewFolder.setExpanded(true);
        mGridViewFolder.setAdapter(albumAdapter);

        // Set the onClickListener
        mGridViewFolder.setOnItemClickListener(this);
        mGridViewFolder.setOnItemLongClickListener(this);
    }


    //카테고리 로드하는 부분
    private void loadCategory() {

        ArrayList<ImageFile> imageFileCategoryList = new ArrayList<>(); // 카테고리 앨범부분에 사용될 이미지들과 앨범의 정보(이름)가 담긴 이미지 파일들의 리스트

        //카테고리 리스트 만드는 쿼리
        ArrayList<Integer> categoryIDList = DatabaseCRUD.selectCategoryList();
        if (categoryIDList == null) {
            return;
        }

        if (categoryIDList != null && categoryIDList.size() >= 0) {
            //카테고리의 아이디를 카테고리 대표 이름으로 변환하는 과정

            for (int cID : categoryIDList) {
                ImageFile imageFileCategory = new ImageFile();

                String cNameOriginal = DiLabClassifierUtil.centroidClassifier.getCategoryName(cID);
                imageFileCategory.setCategoryId(cID);

                String cName = DiLabClassifierUtil.cNameConverter.convert(cNameOriginal);
                imageFileCategory.setCategoryName(cName);

                Integer representImageID = DatabaseCRUD.queryForCategoryRepresentImageIdUsingCID(cID); //카테고리 아이디를 통해서 카테고리의 대표 이미지를 가져오는 쿼리
                imageFileCategory.setRecentImageFileID(representImageID);

                //대표 이미지의 경로
                if (representImageID != null && !TextUtil.isNull(representImageID.toString())) {
                    String imageFileUriPath = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + representImageID));
                    imageFileCategory.setPath(imageFileUriPath);
                    imageFileCategory.setIsDirectory(true);
                    imageFileCategoryList.add(imageFileCategory);
                } else {
//                    DebugUtil.showDebug("MainAct, loadCategory(), representImageID, " + representImageID + ", cName::" + cName);
                }
            }
        }

        if (imageFileCategoryList != null && imageFileCategoryList.size() >= 0) {
            gridItemsCategory = imageFileCategoryList;

            if (gridItemsCategory == null) {
                return;
            }

            tvCategorysNum.setText("" + gridItemsCategory.size());
            tvCategorysNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DebugUtil.showDebug("MainAct, loadCategory(), inverted index db check ::\n");
                    DebugUtil.showDebug(DatabaseCRUD.selectQuery());
                }
            });

            mCategroyAdapterCategory = new CategroyAdapter(this, this, gridItemsCategory);

            // Set the grid adapter
            mGridViewCategory = (ExpandableHeightGridView) findViewById(R.id.gridViewMainCategory);
            mGridViewCategory.setNumColumns(GridViewFolderNumColumns);
            mGridViewCategory.setExpanded(true);
            mGridViewCategory.setAdapter(mCategroyAdapterCategory);
            // Set the onClickListener
            mGridViewCategory.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DebugUtil.showDebug("MainAct, onItemClick(), view.getId : " + parent.getId());

//        Album data = (Album) parent.getAdapter().getItem(position);

        switch (parent.getId()) {
            //폴더 항목
            case R.id.gridViewMainFolder:
                String path = albums.get(position).getPath();

                if (!MainAct.longClicked) {
                    //FolderCategoryAct 이동
                    super.goToFolderCategoryAct(albums.get(position));
                }
//                else {
//                    albums.get(position).setIsChecked(!albums.get(position).isChecked());
//                    if (albumAdapter != null) {
//                        albumAdapter.setAlbums(albums);
//                        albumAdapter.notifyDataSetChanged();
//                    }
//                }
                break;

            //카테고리 항목
            case R.id.gridViewMainCategory:
                DebugUtil.showDebug("MainAct, onItemClick(), case R.id.gridViewMainCategory, 카테고리 영역 클릭 시  ");
                int clickedCid = gridItemsCategory.get(position).getCategoryId();
                DebugUtil.showDebug("MainAct, onItemClick(), clickedCid, : " + clickedCid);
                String cNameOriginal = DiLabClassifierUtil.centroidClassifier.getCategoryName(clickedCid);
                String cName = DiLabClassifierUtil.cNameConverter.convert(cNameOriginal);

                if (!MainAct.longClicked) {
                    super.goToCategoryAct(clickedCid, cName);
                    DebugUtil.showDebug("not longClicked");
                }
                break;

        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        DebugUtil.showDebug("MainAct, onItemLongClick(), " + parent.getId());

        switch (parent.getId()) {
            case R.id.gridViewMainFolder:
                //enable checkbox
                DebugUtil.showDebug("MainAct, onItemLongClick(), enable checkbox");
                MainAct.longClicked = true;

                //hide category folders
                DebugUtil.showDebug("MainAct, onItemLongClick(), hide Category Folders");
                linearLayoutMainCategorySection.setVisibility(View.GONE);

                //change menu
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_main_long_clicked);
                getSupportActionBar().setTitle("사진 선택"); //액션 바의 타이틀을 설정하는 방법

                break;

            case R.id.gridViewMainCategory:
                DebugUtil.showDebug("MainAct, onItemLongClick(), case R.id.gridViewMainCategory");
                MainAct.longClicked = true;

                break;

        }

        return false;
    }


    @Override
    public void onBackPressed() {

        if (MainAct.longClicked) {
            MainAct.longClicked = false;

            //체크 해제
            if (albums != null) {
                for (int i = 0; i < albums.size(); i++) {
                    albums.get(i).setIsChecked(false);
                }
            }
            albumAdapter.notifyDataSetChanged();

            linearLayoutMainCategorySection.setVisibility(View.VISIBLE);

            //카테고리영역 체크해제
            if (gridItemsCategory != null) {
                for (int i = 0; i < gridItemsCategory.size(); i++) {
//                    categories.get(i).setIsChecked(false);
                }
            }
            if (mCategroyAdapterCategory != null) {
                mCategroyAdapterCategory.notifyDataSetChanged();
            }

            //change menu
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_main);
            getSupportActionBar().setTitle("폴더"); //액션바의 타이틀을 설정하는 방법

        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (MainAct.longClicked) {
            DebugUtil.showDebug("MainAct.onCreateOptionsMenu(), when item longClicked");
            getMenuInflater().inflate(R.menu.menu_main_long_clicked, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            /** normal states */

            /** 카메라 사진 */
            case R.id.action_camera:
                DebugUtil.showDebug("move to camera");
//                MoveActUtil.cameraIntent(MainAct.this, ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Date now = new Date();
                SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_kkmmssss");
                String uri = "/DCIM/Camera/IG_" + String.valueOf(fileNameFormat.format(now)) + ".jpg";
                mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), uri));
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                DebugUtil.showDebug(MainAct.currentMission, "MoveActUtil.java, cameraIntent()", String.valueOf(mImageCaptureUri));

                int requestCode = ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT;
                startActivityForResult(cameraIntent, requestCode);
                break;

            /** 검색하기 */
            case R.id.action_searching:
                DebugUtil.showDebug("MainAct, onOptionsItemSelected(), case R.id.action_searching, Searching Dialog");

                CommonDialog newSearchDialog = new CommonDialog();
                newSearchDialog.setDialogSettings(new CommonDialogListener() {

                    @Override
                    public void onClickCommonDialog(DialogInterface dialog, int which, final String inputText) {
                        DebugUtil.showDebug("MainAct, onOptionItemSelected(), case R.id.action_searching : " + which);
                        String textViewString = "";

                        ArrayList<SampleScoreData> categoryList;
//                        SampleContentScoreData[] sampleContentScoreDatas;
                        ArrayList<LinkedHashMap> sampleContentScoreDataMaps;
                        switch (which) {
                            case CommonDialog.POSITIVE:

                                showLoading();
                                // inputText : 사용자가 검색 창에 입력한 텍스트
                                if (inputText == null || inputText.equals("")) {
                                    textViewString = "Please write input text";
                                    DebugUtil.showToast(MainAct.this, textViewString);
                                    hideLoading();
                                    return;
                                }

                                DebugUtil.showDebug("MainAct, onOptionItemSelected(), case R.id.action_searching, case Positive ");
                                //시맨틱 검색
                                // 분류기 초기화

                                categoryList = DiLabClassifierUtil.centroidClassifier.topK(DiLabClassifierUtil.K, inputText);
                                ContentScoreData[] contentScoreDatas = DiLabClassifierUtil.semanticMatching.getRelevantContents(categoryList);

                                String resultString = "사용자께서 입력하신 검색어인 " + inputText + "\n와 유사한 이미지들은 다음과 같습니다  \n\n";
                                final ArrayList<Integer> searchResult = new ArrayList<Integer>();
                                for (ContentScoreData contentScoreData : contentScoreDatas) {
                                    searchResult.add(contentScoreData.getContentsID());
                                }

                                //키워드 검색
                                final ArrayList<Integer> keywordSearchResult = FileUtil.findImagesInSearchResultAct(MainAct.this, inputText);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        DebugUtil.showToast(MainAct.this, "검색어 " + inputText + "에 대한 검색결과입니다");
                                        Intent searchResultIntent = new Intent(MainAct.this, SearchResultAct.class);
                                        searchResultIntent.putIntegerArrayListExtra("keywordSearchResult", keywordSearchResult);
                                        searchResultIntent.putIntegerArrayListExtra("searchResult", searchResult);
                                        hideLoading();
                                        MoveActUtil.moveActivity(MainAct.this, searchResultIntent, -1, -1, false, false);
                                    }
                                }, 1500);
                                break;

                            case CommonDialog.NEGATIVE:
                                DebugUtil.showDebug("MainAct, onOptionItemSelected(), case R.id.action_searching, case Negative ");
                                hideLoading();
                                break;
                        }
                    }
                }, Definitions.DIALOG_TYPE.SEARCHING, true, "검색", "", "취소", "확인");

                dlgShow(newSearchDialog, "searching folder main");
                break;

            // 한 줄에 보기
            case R.id.action_column_num_2:
                MainAct.GridViewFolderNumColumns = 2;
                mGridViewFolder.setNumColumns(GridViewFolderNumColumns);
                mGridViewCategory.setNumColumns(GridViewFolderNumColumns);
                break;

            case R.id.action_column_num_3:
                MainAct.GridViewFolderNumColumns = 3;
                mGridViewFolder.setNumColumns(GridViewFolderNumColumns);
                mGridViewCategory.setNumColumns(GridViewFolderNumColumns);
                break;

            case R.id.action_column_num_4:
                MainAct.GridViewFolderNumColumns = 4;
                mGridViewFolder.setNumColumns(GridViewFolderNumColumns);
                mGridViewCategory.setNumColumns(GridViewFolderNumColumns);

                break;

            //정렬하기
            case R.id.action_arranging_orderby_abc:
                albumOrderBy = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_size:
                albumOrderBy = MediaStore.Images.Media.SIZE;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.SIZE);
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_data:
                albumOrderBy = MediaStore.Images.Media.DATA;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.DATA);
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_date_added:
                albumOrderBy = MediaStore.Images.Media.DATE_ADDED + " desc";
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.DATE_ADDED);
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_date_taken:
                albumOrderBy = MediaStore.Images.Media.DATE_TAKEN + " desc";
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_ORDER_BY, MediaStore.Images.Media.DATE_TAKEN);
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            // 폴더 추가하기
            case R.id.action_adding:
                DebugUtil.showDebug("adding");

                File mPath = new File(Environment.getExternalStorageDirectory() + "/");
                fileDialog = new FileDialog(this, mPath);

                fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                    public void directorySelected(File directory) {
                        DebugUtil.showDebug("selected Dir : " + directory.getName());
                    }
                });

                fileDialog.setSelectDirectoryOption(true);
                fileDialog.showDialog();

                fileDialog.setDialogResult(new FileDialog.OnMyDialogResult() {
                    @Override
                    public void finish(final String result) {
                        DebugUtil.showDebug("result ::" + result);

                        CommonDialog newFolderDialog = new CommonDialog();
                        newFolderDialog.setDialogSettings(new CommonDialogListener() {
                            @Override
                            public void onClickCommonDialog(DialogInterface dialog, int which, String newFolderName) {
                                DebugUtil.showDebug("MainAct, onOptionsItemSelected(), case R.id.action_renaming : " + which);
                                switch (which) {
                                    case CommonDialog.POSITIVE:
                                        File file = new File(result + "/" + newFolderName);
                                        if (!file.exists()) { // 원하는 경로에 폴더가 있는지 확인
                                            file.mkdirs();
                                            DebugUtil.showToast(MainAct.this, result + "/" + newFolderName + " 생성");
                                        } else {
                                            DebugUtil.showToast(MainAct.this, "폴더 생성 실패");
                                        }
                                        break;
                                    case CommonDialog.NEGATIVE:
                                        break;
                                }
                            }
                        }, Definitions.DIALOG_TYPE.ADDING, true, result + "/", "", "취소", "생성하기");

                        dlgShow(newFolderDialog, "new folder main");

                    }
                });
                break;


            /** states when long clicked */

            /** 항목 삭제하기 */
            case R.id.action_trash_confirm:
                DebugUtil.showDebug("before delete:: album size(), " + albums.size());

                if (albums != null) {
                    int removedCount = 0;
                    String removedFileName = "";
                    for (int i = 0; i < albums.size(); i++) {
                        DebugUtil.showDebug("deleting::" + albums.get(i).getId() + ", isCheck::" + albums.get(i).isChecked());
                        if (albums.get(i).isChecked()) {
                            DebugUtil.showDebug("deleting::" + albums.get(i).getPath());
                            removedCount++;
                            removedFileName += albums.get(i).getName() + ", ";
                            FileUtil.removeDir(this, albums.get(i).getPath());
                            albums.remove(albums.get(i));
                            DebugUtil.showDebug("after delete::album size(), " + albums.size());
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (MainAct.longClicked) {
                                albumAdapter.addItems(albums);
                                onBackPressed();
                                onResume();
                            }
                        }
                    }, 1000);

//                    DebugUtil.showToast(this, "지워진 항목 : " + removedFileName + "지워진 개수 : " + removedCount);
                }

                break;

            /** 숨기기 */
            case R.id.action_concealing:
                final ArrayList<Album> concealedAlbum = new ArrayList<>();

                if (albums != null && albums.size() > 0) {
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).isChecked()) {
                            albums.get(i).setIsHide(true);
                            DebugUtil.showDebug("[Hide] MainAct, onOptionsItemSelected(), case R.id.action_concealing:::" + albums.get(i).getPath());
                            concealedAlbum.add(albums.get(i));
                        }
                    }

                    DebugUtil.showDebug("concealedAlbum size ::" + concealedAlbum.size());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            albums.removeAll(concealedAlbum);
                            albumAdapter.setAlbums(albums);
                            albumAdapter.notifyDataSetChanged();
                            tvFoldersNum.setText("" + albums.size());
                        }
                    }, 2000);
                }
                break;

            //숨겨진 항목 보이기
            case R.id.action_revealing:
                DebugUtil.showDebug("revealing");
                loadAlbums();
//                albumAdapter.notifyDataSetChanged();
//                mCategroyAdapterCategory.notifyDataSetChanged();

                break;


            // 이름 변경하기
            case R.id.action_renaming:
                DebugUtil.showDebug("renaming");
                int checkedFolderNum = 0;
                int selectedIndex = 0;
                if (albums != null) {
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).isChecked()) {
                            checkedFolderNum++;
                            selectedIndex = i;
                        }
                    }

                    if (checkedFolderNum == 0 || checkedFolderNum > 1) {
                        DebugUtil.showToast(this, "이름을 변경할 폴더를 하나만 선택해주세요");
                        break;
                    } else if (checkedFolderNum == 1) {
                        final int tempSelectedIndex = selectedIndex;
                        CommonDialog renamingDialog = new CommonDialog();
                        renamingDialog.setDialogSettings(new CommonDialogListener() {
                            @Override
                            public void onClickCommonDialog(DialogInterface dialog, final int which, String newFolderName) {
                                DebugUtil.showDebug("MainAct, onOptionsItemSelected(), case R.id.action_renaming : " + which);
                                switch (which) {
                                    case CommonDialog.POSITIVE:
                                        String tag = "rename folder";
                                        DebugUtil.showDebug(tag + ":: 체크된 것의 이름은 " + albums.get(tempSelectedIndex).getName());
                                        final File folderPre = new File(albums.get(tempSelectedIndex).getPath());
                                        final File folderNow = new File(folderPre.getParentFile().getPath() + "/" + newFolderName);

                                        DebugUtil.showDebug(tag + ":: folderPre " + folderPre.getPath());
                                        DebugUtil.showDebug(tag + ":: folderNow " + folderNow.getPath());

                                        if (folderPre.renameTo(folderNow)) {
                                            DebugUtil.showToast(getApplicationContext(), "변경 성공 " + folderPre.getName() + "->" + newFolderName);
                                            DebugUtil.showDebug(folderNow.getAbsolutePath());

                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                            DebugUtil.showDebug("rename folder:: GalleryAct, before inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificAlbum(MainAct.this, albums.get(tempSelectedIndex).getId()));//업데이트 이전
                                            DebugUtil.showDebug("folderPre :: " + folderPre.getName() +",, folderNow:: " + folderNow.getName());
                                                    while(folderPre.getAbsolutePath().equals(folderNow.getAbsolutePath())){
                                                        showLoading();
                                                    }
                                                    if(!folderPre.getAbsolutePath().equals(folderNow.getAbsolutePath())){
                                                        FileUtil.updateAlbumName(MainAct.this, albums.get(tempSelectedIndex).getId(), folderNow.getName());
                                                    }
                                            DebugUtil.showDebug("rename folder:: GalleryAct, after inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificAlbum(MainAct.this, albums.get(tempSelectedIndex).getId()));//업데이트 이후
                                                }
                                            });
//                                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filePre)));

                                            albums.get(tempSelectedIndex).setName(newFolderName);
                                            albums.get(tempSelectedIndex).setPath(folderNow.getAbsolutePath());
                                            albumAdapter.setAlbums(albums);

                                            if (!MediaScannerBroadcastReceiver.mMedaiScanning) {
                                                DebugUtil.showDebug(currentMission, "MainAct, 이름 변경하는 부분", "현재 풀스캔 중이지 않음");
                                                FileUtil.callBroadCast(MainAct.this);
                                            } else {
                                                DebugUtil.showDebug(currentMission, "MainAct, 이름 변경하는 부분", "현재 풀스캔 중");
                                            }


                                            onBackPressed();
                                            onResume();
                                        } else {
                                            DebugUtil.showToast(getApplicationContext(), "변경 실패");
                                        }
                                        break;
                                    case CommonDialog.NEGATIVE:
//                                        DebugUtil.showToast(MainAct.this, "이름변경 취소");
                                        break;
                                }
                            }
                        }, Definitions.DIALOG_TYPE.RENAMING, true, "이름 변경", albums.get(tempSelectedIndex).getName(), "취소", "변경하기");
                        dlgShow(renamingDialog, "renaming folder main");
                    }
                }
                break;

            case R.id.action_cover_basic:
                DebugUtil.showDebug("action_cover_basic");
                int checkedNum = 0;
                int selectedIdx = 0;
                if (albums != null) {
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).isChecked()) {
                            checkedNum++;
                            selectedIdxInCoverSelect = i;
                        }
                    }
                    if (checkedNum == 0 || checkedNum > 1) {
                        DebugUtil.showToast(this, "커버 이미지를 변경할 폴더를 하나만 선택해주세요");
                        break;
                    } else if (checkedNum == 1) {

                        //기본을 선택했을 경우 커버 이미지 DB에서 해당 bucket_id가 있다면 지워주도록 한다
                        //그렇게 하면 가장 최근에 찍힌 사진이 커버 이미지가 된다
                        if(DatabaseCRUD.doesAlbumBucketIdExist(albums.get(selectedIdxInCoverSelect))){ //디비에 해당 앨범 버킷아이디가 있다면 delete
                            String deleteQuery = "delete from " + DatabaseConstantUtil.TABLE_ALBUM_COVER + " where "
                                    + DatabaseConstantUtil.COLUMN_ALBUM_BUCKET_ID +"="+ albums.get(selectedIdxInCoverSelect).getId() + ";";
                            DatabaseCRUD.execRawQuery(deleteQuery);
                            DebugUtil.showDebug("gg, MainAct, onActivityResult(), deleteQuery :: " + deleteQuery + " \ndelete 완료");
                        }

                        DebugUtil.showDebug("coverImages path:::" + albums.get(selectedIdxInCoverSelect).getCoverImagePath());
                        int basicAlbumCoverId = FileUtil.getAlbumCoverImageIds(this, albums.get(selectedIdxInCoverSelect));
                        DebugUtil.showDebug("coverImages id ::" + basicAlbumCoverId);
                        String newfile = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + basicAlbumCoverId));
                        albums.get(selectedIdxInCoverSelect).setCoverImagePath(newfile);
                        albumAdapter.addItems(albums);
                        albumAdapter.notifyDataSetChanged();


                    }
                }
                break;

            case R.id.action_cover_select:
                DebugUtil.showDebug("action_cover_select");
                int checkedNum2 = 0;

                if (albums != null) {
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).isChecked()) {
                            checkedNum2++;
                            selectedIdxInCoverSelect = i;
                        }
                    }
                    if (checkedNum2 == 0 || checkedNum2 > 1) {
                        DebugUtil.showToast(this, "커버 이미지를 변경할 폴더를 하나만 선택해주세요");
                        break;
                    } else if (checkedNum2 == 1) {
                        DebugUtil.showDebug("coverImages path:::" + albums.get(selectedIdxInCoverSelect).getCoverImagePath());

                        Intent intent = new Intent(MainAct.this, SelectCoverAct.class);
                        intent.putExtra("coverImages change album", albums.get(selectedIdxInCoverSelect).getPath());
                        startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE);
//                        DebugUtil.showDebug("coverImages id ::" + basicAlbumCoverId);

                        //유지하려면 미디어 디비에 앨범의 썸네일을 저장할 방법을 찾아야하나..66
                        //혹은 뒤로가기 할때 onResume에서 앨범을 다시 불러오지 않게 해야할 라나.
                        //아예 계속 유지되어야하는 값이니까..
                    }
                }


                break;

            // 모두 선택하기
            case R.id.action_select_all:
                if (albums != null) {
                    for (int i = 0; i < albums.size(); i++) {
                        albums.get(i).setIsChecked(true);
                    }
                    albumAdapter.notifyDataSetChanged();
                }
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugUtil.showDebug("MainAct, onDestroy");
//        DatabaseHelper.getInstacnce(this).close();
        DebugUtil.showDebug("MainAct, onDestroy DatabaseHelper closed");

    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //data
        DebugUtil.showDebug(currentMission, "MainAct, onActivityResult()", "resultCode;; " + resultCode);
        DebugUtil.showDebug(currentMission, "MainAct, onActivityResult()", "requestCode;; " + requestCode);

        if (resultCode == RESULT_OK) {
            // 사진을 찍고 다시 MainAct로 돌아왔을 때
            if (requestCode == ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT) {
//                Uri uri = mImageCaptureUri;
//                DebugUtil.showDebug(currentMission, "MainAct, onActivityResult()", "RESULT_OK Uri:::" + uri);

//                msc.connect();

//                //풀 스캔
//                if (!MediaScannerBroadcastReceiver.mMedaiScanning) {
//                    DebugUtil.showDebug(MainAct.currentMission, "MainAct, onActivityResult()", "현재 풀스캔 중이지 않음, 풀스캔 시작");
//                    FileUtil.callBroadCast(this);
//                } else {
//                    DebugUtil.showDebug(MainAct.currentMission, "MainAct, onActivityResult()", "현재 풀스캔 중, 개별 사진 스캔 시작");
//                    //개별 사진 스캔
////                    String path = FileUtil.getRealPathFromURI(this, uri);
////                    DebugUtil.showDebug("스캔하기", "MainAct, onActivityResult() FileUtil.getRealPathFromURI() 호출", "path::" + path);
////                    File file = new File(path);
////                    new SingleMediaScanner(this, file);
//                }

                //스캔 다시
//                File f = new File("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
//                mediaScanFile = MediaScanFile.getInstance(this, f.getAbsolutePath());


                //Full Scan 강제 시작
//                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory()))); //킷캣부터, Permission Denial: not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
                FileUtil.callBroadCast(this);
                DebugUtil.showDebug("MainAct, onActivityResult(), ACTION_MEDIA_MOUNTED, sendBroadcast 시작");


                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();
                }

            }

            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE) {
                int receivedId = data.getIntExtra("seletedCoverImageId", 0);
                String receivedPath = data.getStringExtra("seletedCoverImagePath");

                DebugUtil.showDebug("GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE:: received id??::" + receivedId + ", " + receivedPath);

                albums.get(selectedIdxInCoverSelect).setCoverID(receivedId);
                String newfile = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + receivedId));
                albums.get(selectedIdxInCoverSelect).setCoverImagePath(newfile);

//                SharedPreUtil.getInstance().putPreference(SharedPreUtil.ALBUM_COVER_IMAGE_ID, receivedId);
                if(DatabaseCRUD.doesAlbumBucketIdExist(albums.get(selectedIdxInCoverSelect))){ //디비에 해당 앨범 버킷아이디가 있다면 update
                    String updateQuery = "update " + DatabaseConstantUtil.TABLE_ALBUM_COVER + " set " + DatabaseConstantUtil.COLUMN_ALBUM_COVER_IMAGE_ID + "=" + receivedId +" where "
                            + DatabaseConstantUtil.COLUMN_ALBUM_BUCKET_ID +"="+ albums.get(selectedIdxInCoverSelect).getId() + ";";
                    DatabaseCRUD.execRawQuery(updateQuery);
                    DebugUtil.showDebug("MainAct, onActivityResult(), updateQuery :: " + updateQuery + " \nupdate 완료");
                } else { //디비에 해당 앨범 버킷아이디가 없다면 insert
                    String insertQuery = "insert or ignore into " + DatabaseConstantUtil.TABLE_ALBUM_COVER + " (" + DatabaseConstantUtil.COLUMN_AUTO_INCREMENT_KEY_TABLE_ALBUM_COVER+ ", " +
                            DatabaseConstantUtil.COLUMN_ALBUM_BUCKET_ID+ ", " + DatabaseConstantUtil.COLUMN_ALBUM_COVER_IMAGE_ID +") values " +
                            "(null, "+ albums.get(selectedIdxInCoverSelect).getId() +", "+ receivedId +");";
                    DatabaseCRUD.execRawQuery(insertQuery);
                    DebugUtil.showDebug("MainAct, onActivityResult(), insertQuery :: " + insertQuery + " \ninsert 완료");
                }


                albumAdapter.addItems(albums);
                albumAdapter.notifyDataSetChanged();

                onBackPressed();
            }
        }
        if (resultCode != RESULT_OK) {
            return;
        }
    }


}