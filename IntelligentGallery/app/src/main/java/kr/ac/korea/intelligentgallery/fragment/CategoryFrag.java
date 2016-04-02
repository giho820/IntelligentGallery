package kr.ac.korea.intelligentgallery.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.CategoryAct;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.act.SlideShowAct;
import kr.ac.korea.intelligentgallery.adapter.CategoryFragImageAdapter;
import kr.ac.korea.intelligentgallery.common.ParentFrag;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.listener.OnBackPressedListener;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.ExifUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;

// 메인 화면에서 카테고리 영역을 선택했을 시 들어오는 프레그먼트
public class CategoryFrag extends ParentFrag implements OnBackPressedListener {

    public static Context mContext;
    public static CategoryAct categoryAct;
    public static View view;

    private int clickedcId; //카테고리 종류를 클릭시 넘어온 카테고리 아이디
    public ArrayList<ImageFile> imagesInFolder = null;
    public ArrayList<ImageFile> imagesInFolderHavingNotCorrectPath = null;//?
    public static Set<Integer> selectedPositions = null;
    public static ArrayList<Integer> selectedPositionsList = null;
    public GridView gridViewCategoryFrag;

    public static boolean isLongClicked = false;
    public CategoryFragImageAdapter imageAdapter;
    public static ImageLoader imageLoader;

    //Using PauseOnScrollListener To avoid grid scrolling lags
    boolean pauseOnScroll = false; // or true
    boolean pauseOnFling = false; // or false

    public CategoryFrag() {
    }

    @SuppressLint("ValidFragment")
    public CategoryFrag(Integer clickedcId) {
        this.clickedcId = clickedcId;
        imageLoader = ImageLoader.getInstance();
    }

    public void setImagesInFolder(final ArrayList<ImageFile> imagesInFolder) {
        if (this.imagesInFolder == null)
            this.imagesInFolder = new ArrayList<>();
        this.imagesInFolder.addAll(imagesInFolder);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (imageAdapter != null)
                    imageAdapter.setItems(imagesInFolder);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DebugUtil.showDebug("CategoryFrag, onCreate()");
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        categoryAct = (CategoryAct) getActivity();

        selectedPositions = new HashSet<>();
        selectedPositionsList = new ArrayList<>();

        // if this is set true,
        // Activity.onCreateOptionsMenu will call Fragment.onCreateOptionsMenu
        // Activity.onOptionsItemSelected will call Fragment.onOptionsItemSelected
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, null);
        categoryAct.toolbar.inflateMenu(R.menu.menu_category);


        if (clickedcId >= 0) {
            DebugUtil.showDebug("tttt, CategoryFrag, onCreateView(), clickedcId : " + clickedcId);
            imagesInFolderHavingNotCorrectPath = DatabaseCRUD.getViewItemsWithSpecificCId(clickedcId);
            imagesInFolder = FileUtil.createViewItemsHavingPath(mContext, imagesInFolderHavingNotCorrectPath);
        } else {
            DebugUtil.showDebug("tttt, CategoryFrag, onCreateView(), root path is null");
        }

        DebugUtil.showDebug("CategoryFrag, onCreateView(), test ");
        if (imagesInFolder != null && imagesInFolder.size() >= 0) {
            for (ImageFile i : imagesInFolder) {
                DebugUtil.showDebug("i : " + i.getId() + ", " + i.getPath());
            }
        }

        gridViewCategoryFrag = (GridView) view.findViewById(R.id.gridViewCategory);
        gridViewCategoryFrag.setNumColumns(MainAct.GridViewFolderNumColumns);
        if (imagesInFolder != null) {
            imageAdapter = new CategoryFragImageAdapter(categoryAct, imagesInFolder);
            gridViewCategoryFrag.setAdapter(imageAdapter);
        }

//        //Using PauseOnScrollListener To avoid grid scrolling lags
//        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling);
//        gridViewCategoryFrag.setOnScrollListener(listener);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // destroy all menu and re-call onCreateOptionsMenu
        categoryAct.invalidateOptionsMenu();
        DebugUtil.showDebug("categoryAct, onResume(), invalidateOptionMenu ");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        DebugUtil.showDebug("FolderFrag, onCreateOptionMenu() : ");
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
        DebugUtil.showDebug("CategoryAct, onOptionsItemSelected : " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                DebugUtil.showDebug("CategoryFrag, onOptionItemSelected, case android.R.id.home:");
                onBackPressed();
                return true;

            case R.id.action_slide_show:
                Intent intent2SlideShowAct = new Intent(mContext, SlideShowAct.class);
//                intent2SlideShowAct.putExtra("albumFromFolderFrag", album.getPath());
                intent2SlideShowAct.putExtra("SlideShow Start Location is FolderFrag or CategoryFragInAlbum", 3);
                intent2SlideShowAct.putExtra("imagesInFolderFromCategoryFrag", imagesInFolder);
                MoveActUtil.moveActivity(categoryAct, intent2SlideShowAct, -1, -1, false, false);
                return true;

            case R.id.action_select_mode:
                DebugUtil.showToast(categoryAct, "다중 선택하기");

                DebugUtil.showDebug("FolderFrag, onOptionsItemSelected, onOptionsItemSelected() ");
                CategoryFrag.isLongClicked = true;
                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
                imageAdapter.notifyDataSetChanged();

                //FolderCategory 액티비티의 메뉴를 변경해야한다
                categoryAct.toolbar.getMenu().clear();
                categoryAct.toolbar.inflateMenu(R.menu.menu_folder_long_clicked);

                return true;
//            case R.id.action_arranging_floder_category:
//                DebugUtil.showToast(folderCategoryAct, "정렬하기");
//                return true;
//            case R.id.action_concealing:
//                DebugUtil.showToast(folderCategoryAct, "숨기기");
//                return true;

            //long Clicked
            case R.id.action_sharing:

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    ArrayList<Uri> imageUris = new ArrayList<Uri>();
                    for (int i = imagesInFolder.size() - 1; i >= 0; i--) {
                        if (selectedPositions.contains(i)) {
                            imageUris.add(Uri.parse("file:///" + imagesInFolder.get(i).getPath())); // Add your image URIs here
                        }
                    }
                    if (imageUris != null && imageUris.size() > 0) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        shareIntent.setType("image/*");
                        startActivity(Intent.createChooser(shareIntent, "Share images"));
                    }
                }

                return true;
            case R.id.action_delete:
                DebugUtil.showToast(categoryAct, "삭제하기");
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
                    imageAdapter = new CategoryFragImageAdapter(categoryAct, imagesInFolder);
                    gridViewCategoryFrag.setAdapter(imageAdapter);
                    DebugUtil.showToast(categoryAct, "지워진 개수 : " + removedCount);
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        DebugUtil.showDebug("deleting Process, 삭제 이후 : imagesInFolder.get(" + i + ").path : " + FileUtil.getFileNameFromPath(imagesInFolder.get(i).getPath()));
                    }
                }
                return true;
            case R.id.action_detail:

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                    sortedSelectedPositions.addAll(CategoryFrag.selectedPositions);
                    Collections.sort(sortedSelectedPositions);
                    CategoryFrag.selectedPositionsList = sortedSelectedPositions;

                    if (sortedSelectedPositions.size() == 1) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setTitle("세부정보보기");
                        alertDialog.setMessage(ImageUtil.getExifInfoOfSelectedPicture(imagesInFolder.get(selectedPositionsList.get(0)).getPath()));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    } else {
                        DebugUtil.showToast(categoryAct, "세부정보를 볼 파일을 하나만 선택");
                    }
                }


                return true;
//            case R.id.action_rotation:
//                return true;

            case R.id.action_moving:
                DebugUtil.showToast(categoryAct, "이동하기");

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        if (selectedPositionsList.contains(i)) {
                            final String currentPath = imageAdapter.getItem(i).getPath();
                            DebugUtil.showDebug("i : " + i + ", currentPath: " + currentPath);

                            imagesInFolder.remove(i);
                        }
                    }
                }

                //임시방안
                imageAdapter = new CategoryFragImageAdapter(categoryAct, imagesInFolder);
                gridViewCategoryFrag.setAdapter(imageAdapter);

                return true;
            case R.id.action_copying:
                DebugUtil.showToast(categoryAct, "복사하기");

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    for (int i = 0; i < imagesInFolder.size(); i++) {
                        if (selectedPositionsList.contains(i)) {
                            final String currentPath = imageAdapter.getItem(i).getPath();
                            DebugUtil.showDebug("i : " + i + ", currentPath: " + currentPath);
//                            FileUtil.fileCopy(currentPath, MainAct.root + "/new/"+FileUtil.getFileNameFromPath(currentPath));
                        }
                    }
                }

                //임시방안
                imageAdapter = new CategoryFragImageAdapter(categoryAct, imagesInFolder);
                gridViewCategoryFrag.setAdapter(imageAdapter);

                return true;
            case R.id.action_renaming:
                DebugUtil.showToast(categoryAct, "이름바꾸기");
                return true;

            case R.id.action_viewing_in_map:
                DebugUtil.showDebug("CategoryFrag.selectedPositions ::" + CategoryFrag.selectedPositions);

                ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                sortedSelectedPositions.addAll(CategoryFrag.selectedPositions);
                Collections.sort(sortedSelectedPositions);
                CategoryFrag.selectedPositionsList = sortedSelectedPositions;
                DebugUtil.showDebug("ddd::" + CategoryFrag.selectedPositions.size());

                if (sortedSelectedPositions.size() == 1) {
                    float[] gpsInfo = ExifUtil.getGPSinfo(imagesInFolder.get(selectedPositionsList.get(0)).getPath(), categoryAct);
                    Uri geoLocation = Uri.parse("geo:" + gpsInfo[0] + "," + gpsInfo[1]);
                    if (gpsInfo[0] == 0 && gpsInfo[1] == 0) {
                        DebugUtil.showToast(categoryAct, "해당 이미지에 위치 정보가 존재하지 않습니다");
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(geoLocation);
                        if (intent.resolveActivity(categoryAct.getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                } else {
                    DebugUtil.showToast(categoryAct, "지도에서 위치보기를 할 파일을 하나만 선택");
                }


                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DebugUtil.showDebug("CategoryFrag, onBackPressed::" + imagesInFolder.size());
        if (!CategoryFrag.isLongClicked) {
            categoryAct.finish();
//            folderCategoryAct.onBackPressed();
        } else {
            CategoryFrag.isLongClicked = false;

            //체크 해제
            for (ImageFile imageFile : imagesInFolder) {
                imageFile.setIsChecked(false);
                DebugUtil.showDebug("imageFile:::" + imageFile.getIsChecked());
            }

            selectedPositions.clear();
            selectedPositionsList.clear();
            imageAdapter.notifyDataSetChanged();

            //FolderCategory 액티비티의 메뉴를 변경해야한다
            categoryAct.toolbar.getMenu().clear();
            categoryAct.toolbar.inflateMenu(R.menu.menu_category);
        }
    }


}
