package kr.ac.korea.intelligentgallery.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleClassification;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleResourceInitializer;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.FolderCategoryAct;
import kr.ac.korea.intelligentgallery.act.SlideShowAct;
import kr.ac.korea.intelligentgallery.adapter.CategoryFragInAlbumImageAdapter;
import kr.ac.korea.intelligentgallery.common.ParentFrag;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.Category;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.listener.OnBackPressedListener;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;


// FolderCategoryAct 안에 있는 CategroyFrag
public class CategoryFragInAlbum extends ParentFrag implements OnBackPressedListener {

    private final static String TAG = "CategoryFragInAlbum";

    public static Context mContext;
    public static FolderCategoryAct folderCategoryAct;
    public static View view;

    public Album album; //메인 -> 앨범 영역에서 선택한 앨범
    public ArrayList<ImageFile> imagesInFolder = null; //앨범에 있는 이미지들의 정보가 담긴 리스트
    public ArrayList<Category> categories;

    public Set<Integer> selectedPositions = null;
    public List<Integer> selectedPositionsList = null;

    //    public GridView gridViewCategoryFrag;
    public ListView gridViewCategoryFrag;
    public CategoryFragInAlbumImageAdapter imageAdapter;
    public static ImageLoader imageLoader;

    private GetImagesInCategory getImagesInCategory;

    public CategoryFragInAlbum() {
    }

    public static boolean isLongClicked = false;

    @SuppressLint("ValidFragment")
    public CategoryFragInAlbum(Album album) {
        this.album = album;
        mContext = getContext();
        imageLoader = ImageLoader.getInstance();
    }

    public void setImagesInFolder(ArrayList<ImageFile> imagesInFolder) {
        if (imagesInFolder == null)
            return;
        if (this.imagesInFolder == null)
            this.imagesInFolder = new ArrayList<>();
        this.imagesInFolder.removeAll(this.imagesInFolder);
        this.imagesInFolder.addAll(imagesInFolder);
        startGetImagesInCategory();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DebugUtil.showDebug("CategoryFragInAlbum, onCreate()");
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        folderCategoryAct = (FolderCategoryAct) getActivity();

        selectedPositions = new HashSet<>();
        selectedPositionsList = new ArrayList<>();

        categories = new ArrayList<>();


        imageAdapter = new CategoryFragInAlbumImageAdapter(mContext, album);

        // if this is set true,
        // Activity.onCreateOptionsMenu will call Fragment.onCreateOptionsMenu
        // Activity.onOptionsItemSelected will call Fragment.onOptionsItemSelected
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category_in_album, null);
//        folderCategoryAct.toolbar.inflateMenu(R.menu.menu_category);

        gridViewCategoryFrag = (ListView) view.findViewById(R.id.listViewCategory);
        gridViewCategoryFrag.setAdapter(imageAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // destroy all menu and re-call onCreateOptionsMenu
        DebugUtil.showDebug("CategoryFragInAlbum, onResume(), invalidateOptionMenu ");
        folderCategoryAct.invalidateOptionsMenu();
    }

    @Override
    public void onDestroy() {

        stopGetImagesInCategory();

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        DebugUtil.showDebug("CategoryFragInAlbum, onCreateOptionMenu() : ");
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 메뉴 아이템 클릭
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DebugUtil.showDebug("FolderCategoryAct, onOptionsItemSelected : " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                DebugUtil.showDebug("FolderFrag, onOptionItemSelected, case android.R.id.home:");
                onBackPressed();
                return true;

            case R.id.action_slide_show:
//                DebugUtil.showToast(folderCategoryAct, "슬라이드쇼 하기");
//                ArrayList<ImageFile> ImagesThatHaveGPSInfo =FileUtil.getImagesHavingGPSInfoInSpecificAlbum(mContext, album);
//                DebugUtil.showDebug("ImagesThatHaveGPSInfo in folder ::" + ImagesThatHaveGPSInfo.size());
//                for(ImageFile imageFile : ImagesThatHaveGPSInfo){
//                    DebugUtil.showDebug("imageFile::" + imageFile.getPath());
//                }

                Intent intent2SlideShowAct = new Intent(mContext, SlideShowAct.class);
                intent2SlideShowAct.putExtra("SlideShow Start Location is FolderFrag or CategoryFragInAlbum", 1);
                intent2SlideShowAct.putExtra("albumFromFolderFrag", album);
                MoveActUtil.moveActivity(folderCategoryAct, intent2SlideShowAct, -1, -1, false, false);

                return true;
            case R.id.action_select_mode:
                DebugUtil.showToast(folderCategoryAct, "다중 선택하기");

                CategoryFragInAlbum.isLongClicked = true;
                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
                imageAdapter.notifyDataSetChanged();

                //FolderCategory 액티비티의 메뉴를 변경해야한다
                folderCategoryAct.toolbar.getMenu().clear();
                folderCategoryAct.toolbar.inflateMenu(R.menu.menu_folder_long_clicked);

                return true;
//            case R.id.action_arranging_floder_category:
//                DebugUtil.showToast(folderCategoryAct, "정렬하기");
//                return true;
//            case R.id.action_concealing:
//                DebugUtil.showToast(folderCategoryAct, "숨기기");
//                return true;

            //long Clicked
            case R.id.action_sharing:
                DebugUtil.showToast(folderCategoryAct, "공유하기");
                return true;
            case R.id.action_delete:
                DebugUtil.showToast(folderCategoryAct, "삭제하기");
                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    int removedCount = 0;
                    for (int i = imagesInFolder.size() - 1; i >= 0; i--) {
                        if (selectedPositionsList.contains(i)) {
                            removedCount++;
                            DebugUtil.showDebug("deleting Process, selectedPositions : " + selectedPositions.toString());
                            DebugUtil.showDebug("deleting Process, removedCount : " + removedCount);
                            selectedPositions.remove(i);
                            DebugUtil.showDebug("deleting Process, 삭제 이전 : imagesInFolder.get(i).path : " + imagesInFolder.get(i).getPath());
                            FileUtil.removeDir(mContext, imagesInFolder.get(i).getPath());
                            imagesInFolder.remove(i);
                        }
                    }

                    //UI refresh 임시방안
                    selectedPositionsList.addAll(selectedPositions);
                    imageAdapter.notifyDataSetChanged();
                    DebugUtil.showToast(folderCategoryAct, "지워진 개수 : " + removedCount);
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        DebugUtil.showDebug("deleting Process, 삭제 이후 : imagesInFolder.get(" + i + ").path : " + FileUtil.getFileNameFromPath(imagesInFolder.get(i).getPath()));
                    }
                }
                return true;
            case R.id.action_detail:
                DebugUtil.showToast(folderCategoryAct, "세부정보보기");

                return true;

            case R.id.action_moving:
                DebugUtil.showToast(folderCategoryAct, "이동하기");

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        if (selectedPositionsList.contains(i)) {
//                            final String currentPath = imageAdapter.getItem(i).getPath();
//                            DebugUtil.showDebug("i : " + i + ", currentPath: " + currentPath);
//                            FileUtil.moveFile(currentPath, MainAct.root + "/new");

                            imagesInFolder.remove(i);
                        }
                    }
                }

                return true;
            case R.id.action_copying:
                DebugUtil.showToast(folderCategoryAct, "복사하기");

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        if (selectedPositionsList.contains(i)) {
//                            final String currentPath = imageAdapter.getItem(i).getPath();
//                            DebugUtil.showDebug("i : " + i + ", currentPath: " + currentPath);
//                            FileUtil.copyFile(currentPath, MainAct.root + "/new/" + FileUtil.getFileNameFromPath(currentPath));
                        }
                    }
                }

                //임시방안
                imageAdapter.notifyDataSetChanged();

                return true;
            case R.id.action_renaming:
                DebugUtil.showToast(folderCategoryAct, "이름바꾸기");
                return true;
            case R.id.action_viewing_in_map:
                DebugUtil.showToast(folderCategoryAct, "지도에서 위치보기");
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DebugUtil.showDebug("CategoryFrag, onBackPressed");
        if (CategoryFragInAlbum.isLongClicked) {
            CategoryFragInAlbum.isLongClicked = false;

            //체크 해제
            for (ImageFile imageFile : imagesInFolder) {
                imageFile.setIsChecked(false);
            }
            selectedPositions.clear();
            imageAdapter.notifyDataSetChanged();

            //FolderCategory 액티비티의 메뉴를 변경해야한다
            folderCategoryAct.toolbar.getMenu().clear();
//            folderCategoryAct.toolbar.inflateMenu(R.menu.menu_category);

        } else {
            folderCategoryAct.finish();
        }
    }

    public class GetImagesInCategory extends AsyncTask<Void, Void, ArrayList<Category>> {

        private final static String TAG = "GetImagesInCategory::";

        private Context context;
        private Exception mLastError = null;

        public GetImagesInCategory(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
//            DebugUtil.showDebug(TAG + "onPreExecute::");
        }

        @Override
        protected ArrayList<Category> doInBackground(Void... params) {
//            DebugUtil.showDebug(TAG + "doInBackground::");

            ArrayList<Category> tempCategories;
            ArrayList<Category> newCategories = new ArrayList<>();
            if (imagesInFolder != null) {
//                DebugUtil.showDebug(TAG + "imagesInFolder::size::" + imagesInFolder.size());

                tempCategories = DatabaseCRUD.selectCategoryFragInAlbumCategoryList(imagesInFolder);//특정 폴더 내에 위치한 대표적인 카테고리 id의 리스트
                if (tempCategories != null && tempCategories.size() > 0) {
//                    DebugUtil.showDebug(TAG + "tempCategories::size::" + tempCategories.size());
//                    if (categories == null) {
//                        categories = tempCategories;
//                    } else {
//                        categories.addAll(tempCategories);
//                    }
                    categories = tempCategories;
                } else {
                    return null;
                }

                if (categories != null && categories.size() > 0) {
                    int currentCID;

                    DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(mContext);
                    DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
                    DiLabClassifierUtil.luceneKoInitializer.initialize(mContext);
                    DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
                    DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
                    DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");

                    SampleClassification.initialize();

                    while (categories.size() != 0) {
                        int i = 0;
                        currentCID = categories.get(i).getcID();
//                        DebugUtil.showDebug(TAG + "doInBackground::currentCID::" + currentCID);
                        int countImages = 0;
                        ArrayList<ImageFile> imageFiles = new ArrayList<>();
                        imageFiles.add(categories.get(i).getContainingImages().get(0));
                        Category newCategory = new Category();
                        newCategory.setcID(currentCID);
                        //카테고리 아이디를 이름으로 변환

                        String cNameOriginal = DiLabClassifierUtil.centroidClassifier.getCategoryName(currentCID);
                        String cName = DiLabClassifierUtil.cNameConverter.convert(cNameOriginal);
//                        DebugUtil.showDebug(TAG + "doInBackground::cName::" + cName);

                        newCategory.setcName(cName);
                        countImages++;
//                        DebugUtil.showDebug(TAG + "doInBackground::before::categories::size::" + categories.size());
                        for (int j = i + 1; j < categories.size(); j++) {
//                            DebugUtil.showDebug(TAG + "doInBackground::currentCID::" + currentCID);
//                            DebugUtil.showDebug(TAG + "doInBackground::categories.get(j).getcID()::" + categories.get(j).getcID());
//                            DebugUtil.showDebug(TAG + "doInBackground::images::id::" + categories.get(j).getContainingImages().get(0).getId());
                            if (currentCID == categories.get(j).getcID()) {
                                countImages++;
//                                DebugUtil.showDebug(TAG + "images::size::" + categories.get(j).getContainingImages().size());
                                imageFiles.add(categories.get(j).getContainingImages().get(0));
                                categories.remove(j);
                            }
                        }
                        newCategory.setContainingImages(imageFiles);
                        newCategory.setCount(countImages);
                        newCategories.add(newCategory);
                        categories.remove(i);
                    }
                    for (int i = 0; i < categories.size(); i++) {

                    }
//                    DebugUtil.showDebug(TAG + "doInBackground::after::categories::size::" + categories.size());
//                    DebugUtil.showDebug(TAG + "doInBackground::newCategories::size::" + newCategories.size());

                }
            }

            //위치정보를 가지고 있으나 inverted Index db로 분류되지 않는 사진들을 분류하는 부분
            Log.d(FolderCategoryAct.ttttt, "" + FileUtil.getImagesHavingGPSInfoNotInInvertedIndex(mContext).size());
            return newCategories;

        }

        @Override
        protected void onPostExecute(ArrayList<Category> categories) {
            super.onPostExecute(categories);
//            DebugUtil.showDebug(TAG + "onPostExecute::");
            if (categories == null) {
                startGetImagesInCategory();
            } else {
                if (imageAdapter != null) {
                    imageAdapter.setItems(categories);
                }
            }
        }

        @Override
        protected void onCancelled() {
//            DebugUtil.showDebug(TAG + "onCancelled::");

        }
    }

    private void startGetImagesInCategory() {

        if (getImagesInCategory != null && !getImagesInCategory.isCancelled())
            getImagesInCategory.cancel(true);

//        DebugUtil.showDebug(TAG + "onPostExecute::");
        getImagesInCategory = new GetImagesInCategory(mContext);
        getImagesInCategory.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stopGetImagesInCategory() {
        if (getImagesInCategory != null && !getImagesInCategory.isCancelled())
            getImagesInCategory.cancel(true);
    }

}
