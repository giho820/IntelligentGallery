package kr.ac.korea.intelligentgallery.act;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleContentScoreData;
import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
import com.example.dilab.sampledilabapplication.Sample.Ranker.SampleSemanticMatching;
import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleClassification;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleResourceInitializer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.AlbumAdapter;
import kr.ac.korea.intelligentgallery.adapter.CategroyAdapter;
import kr.ac.korea.intelligentgallery.asynctask.ClassifyingWhenExternalImagesExistAsyncTask;
import kr.ac.korea.intelligentgallery.common.Definitions;
import kr.ac.korea.intelligentgallery.common.ExpandableHeightGridView;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.Category;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.dialog.CommonDialog;
import kr.ac.korea.intelligentgallery.dialog.FileDialog;
import kr.ac.korea.intelligentgallery.listener.CommonDialogListener;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


public class MainAct extends ParentAct implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final static String TAG = "MainAct::";

    // 데이터베이스 헬퍼
    private DatabaseHelper databaseHelper;

    public static String root = null; // 시스템 전체의 root dir
    public static String currentPath = null;

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

    public static String albumOrderBy = MediaStore.Images.Media.SIZE;

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
    /**
     * onCreate()
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.showDebug("MainAct, onCreate()");
        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getInstacnce(this);

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


        //category section renewal
        final ArrayList<Category> categories = new ArrayList<>();

        SampleClassification.initialize();
        final ArrayList<Integer> allCategoriesID = SampleClassification.allCategoriesID;
        ArrayList<String> allCategories = SampleClassification.allCategories;

        if (allCategoriesID != null && allCategoriesID.size() > 0) {
            for (int i = 0; i < allCategoriesID.size(); i++) {
                Category category = new Category();
                ArrayList<ImageFile> imageFilesInCategory = new ArrayList<>();
                category.setcID(allCategoriesID.get(i));
                category.setcName(allCategories.get(i));
                category.setCount(0);
                category.setContainingImages(imageFilesInCategory);
                categories.add(category);
            }
        }


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ImageFile newImageFile = (ImageFile) intent.getSerializableExtra("newImageFile");
                updateTermOfLoadingCategory++;
                if (updateTermOfLoadingCategory % 4 == 0) {
                    loadCategory();
                    tvCategoryProgrssCount.setText(FileUtil.allItemCountInInvertedIndexTable() + " / " + FileUtil.getAllImageFilecount(MainAct.this));
                    mCategroyAdapterCategory.notifyDataSetChanged();
                }
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (categories == null)
//                            return;
//
//                        if (categories.size() == 0) {
//                            DebugUtil.showDebug(TAG + "1.size::" + categories.size());
//                            DebugUtil.showDebug(TAG + "newImageFile::cId" + newImageFile.getCategoryId());
//                        } else {
//                            for (int i = 0; i < categories.size(); i++) {
//                                if (categories.get(i).getcID() == newImageFile.getCategoryId()) {
//                                    DebugUtil.showDebug(TAG + "2.size::" + categories.size());
//                                    DebugUtil.showDebug(TAG + "newImageFile::cId" + newImageFile.getCategoryId());
//                                }
//                            }
//                        }
//
//                    }
//                });
            }
        };
//        registerReceiver(mBroadcastReceiver, intentFilter);


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.classifying");
        registerReceiver(mBroadcastReceiver, intentFilter);

        FileUtil.updateMediaStorageQuery(this);

        //앨범 로드
        loadAlbums();

        //카테고리 로드
        loadCategory();

        if(FileUtil.getImagesHavingGPSInfoNotInInvertedIndex(this) != null){
            //분류가 진행되지 않은 사진을 분류한다.
            Integer notClassifiedImageCount = FileUtil.getImagesHavingGPSInfoNotInInvertedIndex(this).size();
            DebugUtil.showDebug("MainAct, onResume(), notClassifiedImageCount:: " + notClassifiedImageCount);
            if(notClassifiedImageCount >0){
                new ClassifyingWhenExternalImagesExistAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, notClassifiedImageCount);
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

                DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(MainAct.this);
                DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
                DiLabClassifierUtil.luceneKoInitializer.initialize(MainAct.this);
                DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
                DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
                DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");
                SampleClassification.initialize();
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
                    DebugUtil.showDebug("MainAct, loadCategory(), representImageID, " + representImageID);
                }

            }
        }

        if (imageFileCategoryList != null && imageFileCategoryList.size() >= 0) {
            gridItemsCategory = imageFileCategoryList;

            if (gridItemsCategory == null) {
                return;
            }
//
            tvCategorysNum.setText("" + gridItemsCategory.size());
            mCategroyAdapterCategory = new CategroyAdapter(this, this, gridItemsCategory);
//
//            // Set the grid adapter
            mGridViewCategory = (ExpandableHeightGridView) findViewById(R.id.gridViewMainCategory);
            mGridViewCategory.setNumColumns(GridViewFolderNumColumns);
            mGridViewCategory.setExpanded(true);
            mGridViewCategory.setAdapter(mCategroyAdapterCategory);
//
//            // Set the onClickListener
            mGridViewCategory.setOnItemClickListener(this);
//            mGridViewCategory.setOnItemLongClickListener(this);
//        } else {
//            DebugUtil.showDebug("MainAct, onCreate(), imageFileCategoryList null");
//        }
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
                MoveActUtil.cameraIntent(MainAct.this, ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT);
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
                        LinkedHashMap<Integer, SampleContentScoreData> sampleContentScoreDatas;


                        switch (which) {
                            case CommonDialog.POSITIVE:

                                // inputText : 사용자가 검색 창에 입력한 텍스트
                                if (inputText == null || inputText.equals("")) {
                                    textViewString = "Please write input text";
                                    DebugUtil.showToast(MainAct.this, textViewString);
                                    return;
                                }

                                DebugUtil.showDebug("MainAct, onOptionItemSelected(), case R.id.action_searching, case Positive ");
                                //시맨틱 검색
                                // 분류기 초기화
                                DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(MainAct.this);
                                DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
                                DiLabClassifierUtil.luceneKoInitializer.initialize(MainAct.this);
                                DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
                                DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
                                DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");
                                SampleClassification.initialize();
                                DiLabClassifierUtil.K = 5; //const로 해서 변경할 수 없도록 처리해야한다.

                                categoryList = DiLabClassifierUtil.centroidClassifier.topK(DiLabClassifierUtil.K, inputText); //classification.getCentroidClassifier().topK -> centroidClassifier.topK 로 바꾸시면 됩니다.
                                sampleContentScoreDataMaps = SampleSemanticMatching.getRelevantContents(categoryList);
                                String resultString = "사용자께서 입력하신 검색어인 " + inputText + "\n와 유사한 이미지들은 다음과 같습니다  \n\n";
                                final ArrayList<Integer> searchResult = new ArrayList<Integer>();
                                for (int i = 0; i < sampleContentScoreDataMaps.size(); i++) {
                                    sampleContentScoreDatas = sampleContentScoreDataMaps.get(i);
                                    for (Integer key : sampleContentScoreDatas.keySet()) {
                                        SampleContentScoreData sampleContentScoreData = sampleContentScoreDatas.get(key);
                                        resultString += "카테고리 : " + key + ", 유사 이미지 : " + sampleContentScoreData.getContentsID() + "(" + sampleContentScoreData.calculateFinalScores(1.0) + ")\n";
                                        searchResult.add(sampleContentScoreData.getContentsID());
                                    }
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
                                        MoveActUtil.moveActivity(MainAct.this, searchResultIntent, -1, -1, false, false);
                                    }
                                }, 1500);
                                break;

                            case CommonDialog.NEGATIVE:
                                DebugUtil.showDebug("MainAct, onOptionItemSelected(), case R.id.action_searching, case Negative ");
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
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_size:
                albumOrderBy = MediaStore.Images.Media.SIZE;
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_data:
                albumOrderBy = MediaStore.Images.Media.DATA;
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_date_added:
                albumOrderBy = MediaStore.Images.Media.DATE_ADDED;
                albums = FileUtil.getAlbums(this, albumOrderBy);
                albumAdapter.removeAllAlbums();
                albumAdapter.setAlbums(albums);
                albumAdapter.notifyDataSetChanged();
                break;

            case R.id.action_arranging_orderby_date_taken:
                albumOrderBy = MediaStore.Images.Media.DATE_TAKEN;
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
                            }
                        }
                    }, 1000);

                    DebugUtil.showToast(this, "지워진 항목 : " + removedFileName + "지워진 개수 : " + removedCount);
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
                            public void onClickCommonDialog(DialogInterface dialog, int which, String newFolderName) {
                                DebugUtil.showDebug("MainAct, onOptionsItemSelected(), case R.id.action_renaming : " + which);
                                switch (which) {
                                    case CommonDialog.POSITIVE:
                                        DebugUtil.showDebug("체크된 것의 이름은 " + albums.get(tempSelectedIndex).getName());
                                        File filePre = new File(albums.get(tempSelectedIndex).getPath());
                                        File fileNow = new File(filePre.getParentFile().getPath() + "/" + newFolderName);

                                        if (filePre.renameTo(fileNow)) {
                                            DebugUtil.showToast(getApplicationContext(), "변경 성공 " + filePre.getName() + "->" + newFolderName);
                                            DebugUtil.showDebug(filePre.getAbsolutePath());
                                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filePre)));
                                            onBackPressed();
                                            onResume();
                                        } else {
                                            DebugUtil.showToast(getApplicationContext(), "변경 실패");
                                        }
                                        break;
                                    case CommonDialog.NEGATIVE:
                                        DebugUtil.showToast(MainAct.this, "이름변경 취소");
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
                if(albums != null){
                    for(int i = 0; i < albums.size(); i++) {
                        if(albums.get(i).isChecked()){
                            checkedNum++;
                            selectedIdx = i;
                        }
                    }
                    if (checkedNum == 0 || checkedNum > 1) {
                        DebugUtil.showToast(this, "커버 이미지를 변경할 폴더를 하나만 선택해주세요");
                        break;
                    } else if(checkedNum == 1){
                        DebugUtil.showDebug("coverImages path:::" + albums.get(selectedIdx).getCoverImagePath());
                        int basicAlbumCoverId = FileUtil.getAlbumCoverImageIds(this, albums.get(selectedIdx));
                        DebugUtil.showDebug("coverImages id ::" + basicAlbumCoverId);
                        String newfile = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + basicAlbumCoverId));
                        albums.get(selectedIdx).setCoverImagePath(newfile);
                        albumAdapter.addItems(albums);
                        albumAdapter.notifyDataSetChanged();
                        //유지하려면 미디어 디비에 앨범의 썸네일을 저장할 방법을 찾아야하나..66
                        //혹은 뒤로가기 할때 onResume에서 앨범을 다시 불러오지 않게 해야할 라나.
                        //아예 계속 유지되어야하는 값이니까..
                    }
                }
                break;

            case R.id.action_cover_select:
                DebugUtil.showDebug("action_cover_select");
                int checkedNum2 = 0;

                if(albums != null){
                    for(int i = 0; i < albums.size(); i++) {
                        if(albums.get(i).isChecked()){
                            checkedNum2++;
                            selectedIdxInCoverSelect = i;
                        }
                    }
                    if (checkedNum2 == 0 || checkedNum2 > 1) {
                        DebugUtil.showToast(this, "커버 이미지를 변경할 폴더를 하나만 선택해주세요");
                        break;
                    } else if(checkedNum2 == 1){
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
        if (resultCode == RESULT_OK) {
            /** 사진을 찍고 다시 MainAct로 돌아왔을 때 */
            if (requestCode == ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT) {
                DebugUtil.showDebug("MainAct, onActivityResult, requestcode : ConstantUtil.MAINACT_REQUESTCODE_FOR_CAMERA_INTENT");

//                새로 촬영한 데이터의 id를 미디어스토리이지에서 가져와야한다
                Integer lastImageId = FileUtil.getLastImageId(this);
                DebugUtil.showDebug("MainAct, onActivityResult(), lastImageId : " + lastImageId);

                FileUtil.updateMediaStorageQuery(this);

//                새로 촬영을 한 사진을 분류해야한다
                //여기서 분류기 초기화
                DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(this);
                DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
                DiLabClassifierUtil.luceneKoInitializer.initialize(this);
                DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
                DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
                DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");
                SampleClassification.initialize();
                DiLabClassifierUtil.K = 5; //const로 해서 변경할 수 없도록 처리해야한다.
                //inverted index table insert
                /** 특정 한 개의 이미지에 대해서 K 개의 카테고리를 생성하여 분류하는 프로세스를 진행한다 */
                DiLabClassifierUtil.classifySpecificImageFile("helloworld", lastImageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열
                //분류기 진행 에러 해결할 것
//                DiLabClassifierUtil.classifySpecificImageFile("helloworld", lastImageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열

                albumAdapter.notifyDataSetChanged();

//                mViewItemAdapter.notifyDataSetChanged();
//                Uri curImageURI = data.getData();
//                DebugUtil.showDebug("curImageURI : " + getRealPathFromURI(curImageURI));


//                if (data != null) {
//                    DebugUtil.showDebug("result = " + data.getExtras().get(android.provider.MediaStore.EXTRA_OUTPUT));
//                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//                    if (thumbnail != null) {
////                        ImageView Imageview = (ImageView) findViewById(R.id.imgview);
////                        Imageview.setImageBitmap(thumbnail);
//                    }
//                } else {
//                    DebugUtil.showDebug("data : null");
//                }
            }

            if(requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE){
//                int receivedId = data.getIntExtra("seletedCoverImageId", 0);
//                String receivedPath = data.getStringExtra("seletedCoverImagePath");
//
//                DebugUtil.showDebug("GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE:: received id??::" +receivedId +", " + receivedPath);
//
//                String newfile = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + receivedId));
//                albums.get(selectedIdxInCoverSelect).setCoverID(receivedId);
//                albums.get(selectedIdxInCoverSelect).setCoverImagePath(receivedPath);
//                albumAdapter.addItems(albums);
//                albumAdapter.notifyDataSetChanged();
            }
        }
        if (resultCode != RESULT_OK) {
            return;
        }
    }


}