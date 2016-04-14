package kr.ac.korea.intelligentgallery.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.FolderCategoryAct;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.act.MoveAct;
import kr.ac.korea.intelligentgallery.act.SlideShowAct;
import kr.ac.korea.intelligentgallery.adapter.FolderFragImageAdapter;
import kr.ac.korea.intelligentgallery.common.Definitions;
import kr.ac.korea.intelligentgallery.common.ParentFrag;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.dialog.CommonDialog;
import kr.ac.korea.intelligentgallery.listener.CommonDialogListener;
import kr.ac.korea.intelligentgallery.listener.OnBackPressedListener;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.ExifUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;
import kr.ac.korea.intelligentgallery.util.SharedPreUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;

/**
 * FolderCategoryAct 안에 있는 FolderFrag
 */
public class FolderFrag extends ParentFrag implements OnBackPressedListener {

    public static Context mContext;
    public static View view;
    public static FolderCategoryAct folderCategoryAct;

    public String path; //이전 액티비티에서 넘겨준 앨범의 경로,
    public Album album;
    public ArrayList<ImageFile> imagesInFolder = null;//뷰 아이템의 리스트, 시간 순서대로 정렬해야함
    public static Set<Integer> selectedPositions = null; //그리드 뷰에서 선택된 것의 포지션을 중복하지 않기 위해 집합으로 저장(순서 저장이 안되는 단점 해결할 것 )
    public static List<Integer> selectedPositionsList = null; //그리드 뷰에서 선택된 것들을 중복을하지 않고 담기위한 자료형
    public GridView gridViewFolderFrag; //사진들이 담겨진 그리드 뷰

    //롱클릭 여부를 확인하는 변수
    public static boolean isLongClicked = false;
    public FolderFragImageAdapter imageAdapter;
    public static ImageLoader imageLoader;

    int totalCountInAlbum;

    public FolderFrag() {
    }

    /**
     * 이전 액티비티로부터 넘겨받은 앨범
     *
     * @param album
     */
    @SuppressLint("ValidFragment")
    public FolderFrag(Album album, int totalCountInAlbum) {
        this.album = album;
        this.path = album.getPath();
        this.totalCountInAlbum = totalCountInAlbum;
        imageLoader = ImageLoader.getInstance();
    }

    public void setImagesInFolder(final ArrayList<ImageFile> _imagesInFolder) {
        if (this.imagesInFolder == null)
            this.imagesInFolder = new ArrayList<>();
        this.imagesInFolder.addAll(_imagesInFolder);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (imageAdapter != null)
                    imageAdapter.setItems(_imagesInFolder);
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DebugUtil.showDebug("FolderFrag, onCreate()");
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        folderCategoryAct = (FolderCategoryAct) getActivity();

        selectedPositions = new HashSet<>();
        selectedPositionsList = new ArrayList<>();

        if (!TextUtil.isNull(path)) {
            DebugUtil.showDebug("FolderFrag, onCreateView(), root path is " + path);

        } else {
            DebugUtil.showDebug("FolderFrag, onCreateView(), root path is null");
        }

        // if this is set true,
        // Activity.onCreateOptionsMenu will call Fragment.onCreateOptionsMenu
        // Activity.onOptionsItemSelected will call Fragment.onOptionsItemSelected
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_folder, null);
        folderCategoryAct.toolbar.getMenu().clear();
        folderCategoryAct.toolbar.inflateMenu(R.menu.menu_folder);

        gridViewFolderFrag = (GridView) view.findViewById(R.id.gridViewFolder);
        imageAdapter = new FolderFragImageAdapter(folderCategoryAct);
        imageAdapter.setCount(totalCountInAlbum);
        gridViewFolderFrag.setNumColumns(MainAct.GridViewFolderNumColumns);
        gridViewFolderFrag.setAdapter(imageAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // destroy all menu and re-call onCreateOptionsMenu
        folderCategoryAct.invalidateOptionsMenu();
        DebugUtil.showDebug("FolderFrag, onResume(), invalidateOptionMenu ");
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
        DebugUtil.showDebug("FolderFrag, onOptionsItemSelected : " + item.getItemId());

        ArrayList<ImageFile> updatedImageFiles = new ArrayList<>();
        switch (item.getItemId()) {
            case android.R.id.home:
                DebugUtil.showDebug("FolderFrag, onOptionItemSelected, case android.R.id.home:");
                onBackPressed();
                return true;

            case R.id.action_slide_show:

                Intent intent2SlideShowAct = new Intent(mContext, SlideShowAct.class);
                intent2SlideShowAct.putExtra("SlideShow Start Location is FolderFrag or CategoryFragInAlbum", 0);
                intent2SlideShowAct.putExtra("albumFromFolderFrag", album);
                MoveActUtil.moveActivity(folderCategoryAct, intent2SlideShowAct, -1, -1, false, false);

                return true;
            case R.id.action_select_mode:

                DebugUtil.showDebug("FolderFrag, onOptionsItemSelected, onOptionsItemSelected() ");
                FolderFrag.isLongClicked = true;
                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
                imageAdapter.notifyDataSetChanged();

                //FolderCategory 액티비티의 메뉴를 변경해야한다
                folderCategoryAct.toolbar.getMenu().clear();
                folderCategoryAct.toolbar.inflateMenu(R.menu.menu_folder_long_clicked);

                return true;

            //정렬하기
            case R.id.action_arranging_orderby_abc:
                FolderCategoryAct.imageOrderby = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, FolderCategoryAct.imageOrderby);
                updatedImageFiles = FileUtil.getImages(folderCategoryAct, album);
                imageAdapter.addItems(updatedImageFiles);
                return true;

            case R.id.action_arranging_orderby_size:
                FolderCategoryAct.imageOrderby = MediaStore.Images.Media.SIZE;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, FolderCategoryAct.imageOrderby);
                updatedImageFiles = FileUtil.getImages(folderCategoryAct, album);
                imageAdapter.addItems(updatedImageFiles);
                return true;

            case R.id.action_arranging_orderby_data:
                FolderCategoryAct.imageOrderby = MediaStore.Images.Media.DATA;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, FolderCategoryAct.imageOrderby);
                updatedImageFiles = FileUtil.getImages(folderCategoryAct, album);
                imageAdapter.addItems(updatedImageFiles);
                return true;

            case R.id.action_arranging_orderby_date_added:
                FolderCategoryAct.imageOrderby = MediaStore.Images.Media.DATE_ADDED;
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, FolderCategoryAct.imageOrderby);
                updatedImageFiles = FileUtil.getImages(folderCategoryAct, album);
                imageAdapter.addItems(updatedImageFiles);
                return true;

            case R.id.action_arranging_orderby_date_taken:
                FolderCategoryAct.imageOrderby = MediaStore.Images.Media.DATE_TAKEN + " desc";
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, FolderCategoryAct.imageOrderby);
                updatedImageFiles = FileUtil.getImages(folderCategoryAct, album);
                imageAdapter.addItems(updatedImageFiles);
                return true;

//            //숨기기
//            case R.id.action_concealing:
//                DebugUtil.showToast(folderCategoryAct, "숨기기");
//                return true;

            case R.id.action_renaming:
                DebugUtil.showDebug("이름 변경하기");

                CommonDialog renamingDialog = new CommonDialog();
                renamingDialog.setDialogSettings(new CommonDialogListener() {
                    @Override
                    public void onClickCommonDialog(DialogInterface dialog, int which, String newFolderName) {
                        DebugUtil.showDebug("MainAct, onOptionsItemSelected(), case R.id.action_renaming : " + which);
                        switch (which) {
                            case CommonDialog.POSITIVE:
                                if (album == null)
                                    break;

                                DebugUtil.showDebug("체크된 것의 이름은 " + album.getName());
                                File filePre = new File(album.getPath());
                                String parentPath = filePre.getParentFile().getAbsolutePath();
                                File fileNow = new File(parentPath + "/" + newFolderName);

                                if (filePre.renameTo(fileNow)) {
                                    DebugUtil.showToast(folderCategoryAct, "변경 성공 " + filePre.getName() + "->" + newFolderName);
                                    DebugUtil.showDebug(filePre.getAbsolutePath());

                                    DebugUtil.showDebug("rename folder:: FolderFrag, before inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificAlbum(folderCategoryAct, album.getId()));//업데이트 이전
                                    FileUtil.updateAlbumName(folderCategoryAct, album.getId(), fileNow.getName());
                                    DebugUtil.showDebug("rename folder:: FolderFrag, after inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificAlbum(folderCategoryAct, album.getId()));//업데이트 이후

                                    FileUtil.callBroadCast(folderCategoryAct);
                                    DebugUtil.showDebug("FolderFrag, case R.id.action_renaming:, sendBroadcast 시작");

                                    imageAdapter.addItems(FileUtil.getImages(folderCategoryAct, album));
                                    imageAdapter.notifyDataSetChanged();

                                    onBackPressed();
                                    onResume();
                                } else {
                                    DebugUtil.showToast(mContext, "변경 실패");
                                }
                                break;
                            case CommonDialog.NEGATIVE:
                                break;
                        }
                    }
                }, Definitions.DIALOG_TYPE.RENAMING, true, "이름 변경", album.getName(), "취소", "변경하기");
                folderCategoryAct.dlgShow(renamingDialog, "renaming folder main");

                return true;

            //long Clicked
            //공유하기
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
                DebugUtil.showDebug("삭제하기");
                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                    sortedSelectedPositions.addAll(FolderFrag.selectedPositions);
                    Collections.sort(sortedSelectedPositions);
                    FolderFrag.selectedPositionsList = sortedSelectedPositions;
                    int totalCount = imagesInFolder.size();
                    DebugUtil.showDebug("before::totalCount::" + imagesInFolder.size());
                    for (int i = totalCount - 1; i >= 0; i--) {
                        if (imagesInFolder.get(i).getIsChecked()) {
                            DebugUtil.showDebug("지울 대상 : " + imagesInFolder.get(i).getPath());
                            DatabaseCRUD.deleteSpecificIdQuery(imagesInFolder.get(i).getId());
                            FileUtil.deleteImage(mContext, imagesInFolder.get(i).getPath());
                            imagesInFolder.remove(imagesInFolder.get(i));
                        }
                    }
                    DebugUtil.showDebug("after::totalCount::" + imagesInFolder.size());
                    selectedPositionsList.clear();
                    selectedPositions.clear();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (FolderFrag.isLongClicked)
                                imageAdapter.addItems(imagesInFolder);

                            onResume();
//                                imageAdapter.notifyDataSetChanged();
                            onBackPressed();
                        }
                    }, 1000);
                }
                return true;

            case R.id.action_detail:

                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                    sortedSelectedPositions.addAll(FolderFrag.selectedPositions);
                    Collections.sort(sortedSelectedPositions);
                    FolderFrag.selectedPositionsList = sortedSelectedPositions;

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
                        DebugUtil.showToast(folderCategoryAct, "세부정보를 볼 파일을 하나만 선택");
                    }
                }

                return true;

            //회전하기
            case R.id.action_rotation_left90:
            rotateFolderFragImages(270);

                return true;

            case R.id.action_rotation_right90:
                rotateFolderFragImages(90);
                return true;

            case R.id.action_rotation_180:
                rotateFolderFragImages(180);
                return true;

            //이동하기
            case R.id.action_moving:
                if (imagesInFolder != null) {
                    Intent intent = new Intent(folderCategoryAct, MoveAct.class);
                    intent.putExtra("moveOrCopy", 0);
                    startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_MOVEACT);

                }


                return true;
            case R.id.action_copying:
                if (imagesInFolder != null) {
                    Intent intent = new Intent(folderCategoryAct, MoveAct.class);
                    intent.putExtra("moveOrCopy", 1);
                    startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_COPYACT);

                }

                return true;

            case R.id.action_viewing_in_map:
//                DebugUtil.showToast(folderCategoryAct, "지도에서 위치보기");
                if (imagesInFolder != null && imagesInFolder.size() > 0) {
                    ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                    sortedSelectedPositions.addAll(FolderFrag.selectedPositions);
                    Collections.sort(sortedSelectedPositions);
                    FolderFrag.selectedPositionsList = sortedSelectedPositions;

                    if (sortedSelectedPositions.size() == 1) {
                        float[] gpsInfo = ExifUtil.getGPSinfo(imagesInFolder.get(selectedPositionsList.get(0)).getPath(), folderCategoryAct);
                        Uri geoLocation = Uri.parse("geo:" + gpsInfo[0] + "," + gpsInfo[1]);

                        if (gpsInfo[0] == 0 && gpsInfo[1] == 0) {
                            DebugUtil.showToast(folderCategoryAct, "해당 이미지에 위치 정보가 존재하지 않습니다");
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(geoLocation);
                            if (intent.resolveActivity(folderCategoryAct.getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    } else {
                        DebugUtil.showToast(folderCategoryAct, "지도에서 위치보기를 할 파일을 하나만 선택");
                    }
                }
                return true;
            default:
                break;
        }

        return false;
    }

    private void rotateFolderFragImages(final int rotateDegree) {
        ArrayList<Integer> sortedSelectedPositionsInRotate = new ArrayList<>();
        sortedSelectedPositionsInRotate.addAll(FolderFrag.selectedPositions);
        Collections.sort(sortedSelectedPositionsInRotate);
        FolderFrag.selectedPositionsList = sortedSelectedPositionsInRotate;

        if (imagesInFolder != null && imagesInFolder.size() > 0) {
            for (int i = 0; i < imagesInFolder.size(); i++) {
                if (selectedPositionsList.contains(i)) {

                    final int finalI = i;
                    final Uri currentImageFileUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + imagesInFolder.get(finalI).getId());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int currentOrientation = ExifUtil.getOrientation(folderCategoryAct, currentImageFileUri);
                            DebugUtil.showDebug(rotateDegree + "도 회전시키기 : 현재 orientation :: " + currentOrientation);
                            DebugUtil.showDebug(rotateDegree + "도 회전시키기 업데이트 이전 GalleryAct, before inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(folderCategoryAct, imagesInFolder.get(finalI).getId(), MediaStore.Images.Media.ORIENTATION));//업데이트 이전
                            DebugUtil.showDebug(rotateDegree + "도 회전시키기 변경함 ");
                            DebugUtil.showDebug("==========================================================");
                            ExifUtil.setOrientation(currentImageFileUri, imagesInFolder.get(finalI).getPath(), currentOrientation + rotateDegree, folderCategoryAct);
                        }
                    }, 300);
                    DebugUtil.showDebug(rotateDegree + "도 회전시키기 업데이트 이후 GalleryAct, after inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(folderCategoryAct, imagesInFolder.get(finalI).getId(), MediaStore.Images.Media.ORIENTATION));//업데이트 이후
                    DebugUtil.showDebug("after rotated, rotated orientation ::" + ExifUtil.getOrientation(folderCategoryAct, currentImageFileUri));

                    imageAdapter.setItems(imagesInFolder);
                    imageAdapter.notifyDataSetChanged();
                    onBackPressed();
                }
            }
        }

    }


    @Override
    public void onBackPressed() {
        DebugUtil.showDebug("FolderFrag, onBackPressed");
        if (!FolderFrag.isLongClicked) {
            folderCategoryAct.finish();
        } else {
            FolderFrag.isLongClicked = false;

            //체크 해제
            for (ImageFile imageFile : imagesInFolder) {
                imageFile.setIsChecked(false);
            }
            selectedPositions.clear();
            selectedPositionsList.clear();
            imageAdapter.notifyDataSetChanged();

            //FolderCategory 액티비티의 메뉴를 변경해야한다
            folderCategoryAct.toolbar.getMenu().clear();
            folderCategoryAct.toolbar.inflateMenu(R.menu.menu_folder);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == folderCategoryAct.RESULT_OK) {
            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_MOVEACT) {
                String destinationFolderPath = data.getStringExtra("pathFromMoveAct");
                DebugUtil.showDebug("FolderFrag, onActivityResult() compare::" + album.getPath() + "==???" + destinationFolderPath);

                ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                sortedSelectedPositions.addAll(FolderFrag.selectedPositions);
                Collections.sort(sortedSelectedPositions);
                FolderFrag.selectedPositionsList = sortedSelectedPositions;

                if (!album.getPath().equals(destinationFolderPath)) {

                    for (Integer i : selectedPositionsList) {
                        FileUtil.moveFile(imagesInFolder.get(i).getPath(), destinationFolderPath);

                        DebugUtil.showDebug("FolderFrag, before inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(folderCategoryAct, imagesInFolder.get(i).getId(), MediaStore.Images.Media.DATA));//업데이트 이전
                        ContentResolver mCr = folderCategoryAct.getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, destinationFolderPath + "/" + new File(imagesInFolder.get(i).getPath()).getName());
                        mCr.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Images.ImageColumns._ID + "=" + imagesInFolder.get(i).getId(), null);
                        DebugUtil.showDebug("FolderFrag, after inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(folderCategoryAct, imagesInFolder.get(i).getId(), MediaStore.Images.Media.DATA));//업데이트 이후
                    }

                    for (int i = selectedPositionsList.size() - 1; i >= 0; i--) {
                        imagesInFolder.remove(imagesInFolder.get(i));
                    }
                    // update
                    imageAdapter.addItems(imagesInFolder);
                    imageAdapter.notifyDataSetChanged();
                    onBackPressed();

                } else {
                    DebugUtil.showToast(folderCategoryAct, "이동하려는 폴더가 현재 폴더와 같습니다");
                }
            }

            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_COPYACT) {
                String destinationFolderPath = data.getStringExtra("pathFromMoveAct");
                DebugUtil.showDebug("FolderFrag, onActivityResult() compare::" + album.getPath() + "==???" + destinationFolderPath);

                ArrayList<Integer> sortedSelectedPositions = new ArrayList<>();
                sortedSelectedPositions.addAll(FolderFrag.selectedPositions);
                Collections.sort(sortedSelectedPositions);
                FolderFrag.selectedPositionsList = sortedSelectedPositions;

                if (!album.getPath().equals(destinationFolderPath)) {
                    ContentResolver mCr = folderCategoryAct.getContentResolver();
                    MediaStore.Images.Media media = new MediaStore.Images.Media();

                    for (Integer i : selectedPositionsList) {
                        //파일들 복사
                        FileUtil.copyFile(imagesInFolder.get(i).getPath(), destinationFolderPath);
                        File copiedFile = new File(imagesInFolder.get(i).getPath());
                        folderCategoryAct.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copiedFile)));
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            folderCategoryAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onBackPressed();
                                    imageAdapter.addItems(FileUtil.getImages(folderCategoryAct, album));
                                    imageAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }, 1500);
                } else {
                    DebugUtil.showDebug("GalleryAct, onActivityResult(), 복사할 파일 이름이 중복됨, 퀵픽과 같이 아무런 동작 안 함");
                }
            }
        }
    }
}

