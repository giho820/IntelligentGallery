package kr.ac.korea.intelligentgallery.act;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.ItemDecorationMatchingImg;
import kr.ac.korea.intelligentgallery.adapter.MatchingImageRecyclerAdapter;
import kr.ac.korea.intelligentgallery.broadcastReceiver.MediaScannerBroadcastReceiver;
import kr.ac.korea.intelligentgallery.common.Definitions;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.common.ViewPagerFixed;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.dialog.CommonDialog;
import kr.ac.korea.intelligentgallery.intelligence.Sample.Model.ContentScoreData;
import kr.ac.korea.intelligentgallery.listener.AdapterItemClickListener;
import kr.ac.korea.intelligentgallery.listener.CommonDialogListener;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.ExifUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

//import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by kiho on 2015. 12. 22..
 */
//public class GalleryAct extends ParentAct implements OnPhotoViewClickedListener{
public class GalleryAct extends ParentAct {

    public static Context mContext;
    private Toolbar galleryToolbar;

    public Integer selectedPostion;
    public ArrayList<ImageFile> imagesInSameFolder;
    private String currentAlbumBucketId;
    private Integer currentCategoryId;
    public ImageFile currentImageFileImage;
    private ImageAdapter imageAdapter;
    private ViewPagerFixed photoVp;
    public boolean isChecked = false;

    public static String correctTopk = "cccc";

    private FrameLayout totalWrapperFrameLayout;
    //    매칭 영역
    private ArrayList<Integer> matchingResult;
    private ArrayList<ImageFile> matchingResultImages;
    private RecyclerView recyclerView;
    private MatchingImageRecyclerAdapter matchingImageRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_gallery);

        imagesInSameFolder = (ArrayList<ImageFile>) getIntent().getSerializableExtra("imageItems");
        selectedPostion = getIntent().getIntExtra("selectedPostion", 0);
        imageAdapter = new ImageAdapter(this, imagesInSameFolder, selectedPostion);
        imageAdapter.setCount(imagesInSameFolder.size());

        //현재 이미지 항목
        currentImageFileImage = imagesInSameFolder.get(selectedPostion);
        DebugUtil.showDebug("GalleryAct, onCreate(), currentImageFile Id:: " + currentImageFileImage.getId());
        //아이디로부터 그 이미지의 상위 폴더의 id를 알아낸다
        if (currentImageFileImage.getId() != null) {
            currentAlbumBucketId = FileUtil.getBucketIdFromImage(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + currentImageFileImage.getId()));
            DebugUtil.showDebug("GalleryAct, onCreate(), currentAlbumBucketId:: " + currentAlbumBucketId);
        }

        currentCategoryId = currentImageFileImage.getCategoryId();
        if (currentCategoryId == null) {
            currentCategoryId = DatabaseCRUD.getCategoryIdUsingImageId(currentImageFileImage.getId());
        }
        DebugUtil.showDebug("GalleryAct, onCreate(), currentImageFile cid:: " + currentCategoryId);


        galleryToolbar = (Toolbar) findViewById(R.id.toolbar_gallery);
        setSupportActionBar(galleryToolbar);

        //툴바의 상단에 파일의 이름을 표기
        if (!TextUtil.isNull(FileUtil.getFileNameFromPath(imagesInSameFolder.get(selectedPostion).getPath())))
            getSupportActionBar().setTitle(FileUtil.getFileNameFromPath(imagesInSameFolder.get(selectedPostion).getPath()));
        galleryToolbar.setTitleTextColor(getResources().getColor(R.color.c_ffffffff));
        galleryToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backkey));

        //매칭 영역
        Integer ImageFilesCnt = FileUtil.getAllImageFilecount(this);
        new MatchingAsyncTask(this).execute(ImageFilesCnt);

        //뷰페이져
        if (!TextUtil.isNull(imagesInSameFolder.get(selectedPostion).getPath())) {
            photoVp = (ViewPagerFixed) findViewById(R.id.pager_activity_gallery);
            //터치 이벤트를 막아서 뷰페이져 상에서의 스크롤과 포토뷰의 줌인/줌아웃이 원활하게 동작하게 하기 위함
            photoVp.setAdapter(imageAdapter);
            photoVp.setCurrentItem(selectedPostion);

            photoVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    //툴바의 상단에 변경되는 파일의 이름을 표기
                    getSupportActionBar().setTitle(FileUtil.getFileNameFromPath(imagesInSameFolder.get(position).getPath()));
                    currentImageFileImage = imagesInSameFolder.get(position);
                    selectedPostion = position;
                    DebugUtil.showDebug("GalleryAct, onCreate(), onPageSelected(), currentImageFileImage : " + currentImageFileImage.getPath());

                    Integer ImageFilesCnt = FileUtil.getAllImageFilecount(GalleryAct.this);
                    new MatchingAsyncTask(GalleryAct.this).execute(ImageFilesCnt);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        totalWrapperFrameLayout = (FrameLayout) findViewById(R.id.wrapper_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        matchingImageRecyclerAdapter = new MatchingImageRecyclerAdapter(this);
        matchingImageRecyclerAdapter.setAdapterItemClickListener(new AdapterItemClickListener() {
            @Override
            public void onAdapterItemClick(View view, int position) {
                DebugUtil.showDebug("matchedImages : " + matchingResultImages.get(position).getId());
                matchingResultImages.get(position).setPath(GalleryAct.this, matchingResultImages.get(position).getId());
                //galleryAct로 이동해야한다.
                goToGalleryAct(matchingResultImages, position);
                //stack은 쌓이도록 한다
//                Intent intent = new Intent(GalleryAct.this, MatchingAct.class);
//                intent.putExtra("clicked_matching_image", matchingResultImages.get(position).getId());
//                MoveActUtil.moveActivity(GalleryAct.this, intent, -1, -1, false, false);
            }
        });
        recyclerView.setAdapter(matchingImageRecyclerAdapter);
        recyclerView.addItemDecoration(new ItemDecorationMatchingImg(getResources().getDimensionPixelSize(R.dimen.dp_5), 0));


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                galleryToolbar.animate().translationY(-galleryToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) recyclerView.getLayoutParams();
                int recyclerViewMargin = lp.bottomMargin;
                recyclerView.animate().translationY(recyclerView.getHeight() + recyclerViewMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        }, 300);

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("imagesInSameFolder", imagesInSameFolder);
        intent.putExtra("imagesInSameFoder_Count", imagesInSameFolder.size());
        intent.putExtra("bucketid", currentAlbumBucketId);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery_long_clicked, menu);
//        return super.onCreateOptionsMenu(menu);
        return true;
    }

    //메뉴 아이템 클릭
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri currentImageFileUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + currentImageFileImage.getId());
        if (currentImageFileImage.getId() == null) {
            return false;
        }
        int currentOrientation = ExifUtil.getOrientation(this, currentImageFileUri);

        DebugUtil.showDebug("GalleryAct, onOptionsItemSelected : " + item.getItemId());
        switch (item.getItemId()) {
            //뒤로가기
            case android.R.id.home:
                DebugUtil.showDebug("GalleryAct, onOptionItemSelected, case android.R.id.home:");
//                finish();
                onBackPressed();
                return true;

            //공유하기
            case R.id.action_sharing:
                if (imagesInSameFolder != null && imagesInSameFolder.size() > 0) {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    //이렇게 하니 되네
                    //Add attachment
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + currentImageFileImage.getPath()));
                    // replace "Uri.parse" with Uri.fromFile(new File("file:///"+root.getAbsolutePath()+"/"+location))
                    emailIntent.setType("image/*");
                    startActivity(Intent.createChooser(emailIntent, "공유하기"));
                }
                return true;

            //삭제하기
            case R.id.action_delete:

                DebugUtil.showDebug("[Delete] before delete==============");
                DebugUtil.showDebug("[Delete] position::" + selectedPostion);
                DebugUtil.showDebug("[Delete] images size::" + imagesInSameFolder.size());
                DebugUtil.showDebug("[Delete] images id::" + imagesInSameFolder.get(selectedPostion).getId());

                if (imagesInSameFolder != null && imagesInSameFolder.size() > 0) {
                    DatabaseCRUD.deleteSpecificIdQuery(imagesInSameFolder.get(selectedPostion).getId());
                    FileUtil.deleteImage(this, imagesInSameFolder.get(selectedPostion).getPath());
                    imagesInSameFolder.remove(imagesInSameFolder.get(selectedPostion));
                    DebugUtil.showDebug("[Delete] images size::" + imagesInSameFolder.size());

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageAdapter.setItems(imagesInSameFolder);

                                    DebugUtil.showDebug("case R.id.action_delete currentAlbumBucketId::" + currentAlbumBucketId);
                                    onBackPressed();
                                }
                            });
                        }
                    }, 1000);

                    DebugUtil.showDebug("[Delete] After delete==============");
                    DebugUtil.showDebug("[Delete] position::" + selectedPostion);
                    DebugUtil.showDebug("[Delete] images size::" + imagesInSameFolder.size());
                    DebugUtil.showDebug("[Delete] images size::" + imageAdapter.getCount());
                }
                return true;

            //세부정보보기
            case R.id.action_detail:
                AlertDialog alertDialog = new AlertDialog.Builder(GalleryAct.this).create();
                alertDialog.setTitle("세부정보보기");
                alertDialog.setMessage(ImageUtil.getExifInfoOfSelectedPicture(currentImageFileImage.getPath()));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;

            case R.id.action_rotation_left90:
                DebugUtil.showDebug("왼쪽 90도 회전시키기");
                DebugUtil.showDebug("before rotated, rotated orientation ::" + currentOrientation);
                DebugUtil.showDebug("==========================================================");
                ExifUtil.setOrientation(currentImageFileUri, currentImageFileImage.getPath(), currentOrientation + 270, this);
                DebugUtil.showDebug("after rotated, rotated orientation ::" + ExifUtil.getOrientation(this, currentImageFileUri));
                imageAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_rotation_right90:
                DebugUtil.showDebug("오른쪽 90도 회전시키기");
                DebugUtil.showDebug("before rotated, rotated orientation ::" + currentOrientation);
                DebugUtil.showDebug("==========================================================");
                ExifUtil.setOrientation(currentImageFileUri, currentImageFileImage.getPath(), currentOrientation + 90, this);
                DebugUtil.showDebug("after rotated, rotated orientation ::" + ExifUtil.getOrientation(this, currentImageFileUri));
                imageAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_rotation_180:
                DebugUtil.showDebug("180도 회전시키기");
                DebugUtil.showDebug("before rotated, rotated orientation ::" + currentOrientation);
                DebugUtil.showDebug("==========================================================");
                ExifUtil.setOrientation(currentImageFileUri, currentImageFileImage.getPath(), currentOrientation + 180, this);
                DebugUtil.showDebug("after rotated, rotated orientation ::" + ExifUtil.getOrientation(this, currentImageFileUri));
                imageAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_moving:
                if (imagesInSameFolder != null) {

                    Intent intent = new Intent(GalleryAct.this, MoveAct.class);
                    intent.putExtra("moveOrCopy", 0);
                    startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_MOVEACT);

                }

                return true;
            case R.id.action_copying:
                if (imagesInSameFolder != null) {
                    Intent intent = new Intent(GalleryAct.this, MoveAct.class);
                    intent.putExtra("moveOrCopy", 1);
                    startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_COPYACT);
                }

                return true;
            case R.id.action_renaming:

                if (imagesInSameFolder != null) {
                    CommonDialog renamingDialog = new CommonDialog();
                    renamingDialog.setDialogSettings(new CommonDialogListener() {
                        @Override
                        public void onClickCommonDialog(DialogInterface dialog, int which, String newFolderName) {
                            DebugUtil.showDebug("GalleryAct, onOptionsItemSelected(), case R.id.action_renaming : " + which);

                            switch (which) {

                                case CommonDialog.POSITIVE:
                                    DebugUtil.showDebug("[이름바꾸기] 체크된 것의 경로::" + currentImageFileImage.getPath());

                                    File filePre = new File(currentImageFileImage.getPath());
                                    File filePreParent = filePre.getParentFile();

                                    int pos = filePre.getName().lastIndexOf(".");
                                    String ext = filePre.getName().substring(pos + 1);

                                    File fileNow = new File(filePreParent.getAbsolutePath() + "/" + newFolderName);
                                    DebugUtil.showDebug("new fileNow path :: " + fileNow.getAbsolutePath());

                                    if (filePre.renameTo(fileNow)) {
                                        DebugUtil.showToast(GalleryAct.this, "변경 성공 " + filePre.getName() + "->" + fileNow.getName());

                                        DebugUtil.showDebug("GalleryAct, case R.id.action_renaming before update DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(GalleryAct.this, currentImageFileImage.getId(), MediaStore.Images.Media.DATA));//업데이트 이전
                                        ContentResolver mCr = getContentResolver();
                                        ContentValues values = new ContentValues();
                                        values.put(MediaStore.Images.Media.DATA, fileNow.getAbsolutePath());
                                        mCr.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Images.ImageColumns._ID + "=" + currentImageFileImage.getId(), null);
                                        DebugUtil.showDebug("GalleryAct, case R.id.action_renaming after update DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(GalleryAct.this, currentImageFileImage.getId(), MediaStore.Images.Media.DATA));//업데이트 이후

                                        if (!MediaScannerBroadcastReceiver.mMedaiScanning) {
                                            DebugUtil.showDebug(MainAct.currentMission, "GalleryAct, 이름 변경하는 부분", "현재 풀스캔 중이지 않음");
                                            FileUtil.callBroadCast(mContext);
                                        } else {
                                            DebugUtil.showDebug(MainAct.currentMission, "GalleryAct, 이름 변경하는 부분", "현재 풀스캔 중");
                                        }

                                        currentImageFileImage.setPath(fileNow.getPath());
                                        imagesInSameFolder.get(selectedPostion).setName(fileNow.getName());
                                        imageAdapter.addItems(imagesInSameFolder);
                                        imageAdapter.notifyDataSetChanged();

                                        onBackPressed();
                                        onResume();
                                    } else {
                                        DebugUtil.showToast(GalleryAct.this, "변경 실패");
                                        onBackPressed();
                                        onResume();
                                    }
                                    break;
                                case CommonDialog.NEGATIVE:
                                    break;
                            }
                        }
                    }, Definitions.DIALOG_TYPE.RENAMING, true, "이름 변경", new File(currentImageFileImage.getPath()).getName(), "취소", "변경하기");
                    GalleryAct.this.dlgShow(renamingDialog, "renaming folder folderFrag");
                }
                return true;
            case R.id.action_viewing_in_map:
                float[] gpsInfo = ExifUtil.getGPSinfo(currentImageFileImage.getPath(), this);
                Uri geoLocation = Uri.parse("geo:" + gpsInfo[0] + "," + gpsInfo[1]);

                if (gpsInfo[0] == 0 && gpsInfo[1] == 0) {
                    DebugUtil.showToast(this, "해당 이미지에 위치 정보가 존재하지 않습니다");
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(geoLocation);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                return true;
            default:
                break;
        }

        return false;
    }

    private boolean rename(File from, File to) {
        return from.getParentFile().exists() && from.exists() && from.renameTo(to);
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public class MatchingAsyncTask extends AsyncTask<Integer, String, Integer> {

        private Context mContext;

        public MatchingAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DebugUtil.showDebug("GalleryAct, MatchingAsyncTask, onPreExecute() ");
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            final int taskCnt = params[0];
            DebugUtil.showDebug("GalleryAct, MatchingAsyncTask, doInBackground ");

            ArrayList<SampleScoreData> categoryList;
            LinkedHashMap<Integer, ContentScoreData> sampleContentScoreDatas;
            matchingResult = new ArrayList<Integer>();
            matchingResultImages = new ArrayList<ImageFile>();


            DebugUtil.showDebug(correctTopk + " , " + DiLabClassifierUtil.K);
            DebugUtil.showDebug(correctTopk + " , " + currentImageFileImage.getId());


            categoryList = DatabaseCRUD.getScoreDatasUsingDidThatSizeIsK(currentImageFileImage.getId(), DiLabClassifierUtil.K);

            //1. top0 을 제외하고 top1부터 top5까지 들어간다
            //2. top0 k의 경우 고정이 아니라 변경할 수 있도록 한다.
            //3. top1 ~ topk까지 리스트를 받아서 centroidClassifier에 넣어주어야한다.
            //걱정 되는 시나리오 : 사진이 아직 분류가 되지 않았는데 아무것도 안뜨는 경우의 시나리오

            //분류기 객체가 초기화가 이루어졌는데 널이 아니라서 사용을 못하게 되는 경우가 가장 문제일 것 같다고 하심
            //싱글톤 패턴으로 널이 아닌 객체를 한번만 초기화하고 계속 들고디닐 수 있도록 해야한다.
            ContentScoreData[] contentScoreDatas = DiLabClassifierUtil.semanticMatching.getRelevantContents(categoryList, currentImageFileImage.getId());
            if (contentScoreDatas == null) {
                return -1;
            }
            for (ContentScoreData contentScoreData : contentScoreDatas) {
                matchingResult.add(contentScoreData.getContentsID());
            }

            Set<Integer> ids = new HashSet<>();
            ids.addAll(matchingResult);
            for (Integer id : ids) {
                ImageFile imageFile = new ImageFile();
                imageFile.setId(id);
                imageFile.setPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
                DebugUtil.showDebug("GalleryAct, " + imageFile.getPath());
                matchingResultImages.add(imageFile);
            }
            return taskCnt;
        }


        @Override
        protected void onPostExecute(Integer result) {
            DebugUtil.showDebug("GalleryAct, MatchingAsyncTask, onPostExecute ");
            if (matchingResultImages != null && matchingResultImages.size() > 0) {
                // refresh recycler view
                DebugUtil.showDebug("GalleryAct, MatchingAsyncTask, " + matchingResultImages.size());
                if (matchingImageRecyclerAdapter != null) {
                    matchingImageRecyclerAdapter.setAdapterArrayList(matchingResultImages);
                   matchingImageRecyclerAdapter.notifyDataSetChanged();

                }
            } else {
                recyclerView.setVisibility(View.GONE);
            }


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    galleryToolbar.animate().translationY(-galleryToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).setStartDelay(120);

                    if (!matchingResultImages.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) recyclerView.getLayoutParams();
                        int recyclerViewMargin = lp.bottomMargin;
                        recyclerView.animate().translationY(recyclerView.getHeight() + recyclerViewMargin).setInterpolator(new AccelerateInterpolator(2)).start();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }, 300);

        }
    }

    //ImageAdapter
    private class ImageAdapter extends PagerAdapter {

        public ArrayList<ImageFile> items;
        public Integer position;
        private int count = 0;

        private LayoutInflater inflater;

        ImageAdapter(Context context, ArrayList<ImageFile> items, Integer position) {
            inflater = LayoutInflater.from(context);
            this.items = items;
            this.position = position;
            DebugUtil.showDebug("[Delete] ImageAdapter Constructor item.size()::" + items.size() + ", position::" + position);

            if (!ImageLoader.getInstance().isInited()) {
//                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(mContext));
                ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(GalleryAct.this));
            }
        }

        public void addView(ImageFile imageFile, int index) {
            items.add(index, imageFile);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);
        }

        public void addItems(ArrayList<ImageFile> items) {
            if (this.items == null)
                this.items = new ArrayList<>();
            DebugUtil.showDebug("[Delete] before::adapter items size" + items.size());
            this.items.removeAll(this.items);
            this.items.addAll(items);
            count = this.items.size();
            DebugUtil.showDebug("[Delete] after::adapter items size" + this.items.size());

            notifyDataSetChanged();
        }

        public void setItems(ArrayList<ImageFile> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public void setPosition(Integer currentImagePosition) {
            this.position = currentImagePosition;
            DebugUtil.showDebug("us");
        }

        public void setCount(int count) {
            this.count = items.size();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            final PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image_gallery);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            this.position = position;

            imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    isChecked = !isChecked;
                    DebugUtil.showDebug("GalleryAct, ImageAdapter, isChecked::" + isChecked);
                    if (isChecked) {
                        // show
                        galleryToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                        recyclerView.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();
                    } else {
                        // hide
                        galleryToolbar.animate().translationY(-galleryToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) recyclerView.getLayoutParams();
                        int recyclerViewMargin = lp.bottomMargin;
                        recyclerView.animate().translationY(recyclerView.getHeight() + recyclerViewMargin).setInterpolator(new AccelerateInterpolator(2)).start();
                    }
                }
            });

            if (items.get(position).getId() != null) {
                float currentOrientation = ExifUtil.getOrientation(GalleryAct.this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + items.get(position).getId()));
                DebugUtil.showDebug("GalleryAct, currentOrientation::" + currentOrientation);
                if (items.get(position) != null) {
                    if (!TextUtil.isNull(items.get(position).getPath())) {
                        if (currentOrientation == 0) {

                            if (ImageLoader.getInstance().getDiskCache().get("file://" + items.get(position).getPath()) != null) {
                                ImageLoader.getInstance().displayImage("file://" + items.get(position).getPath(), imageView, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        spinner.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                        String message = null;

                                        switch (failReason.getType()) {
                                            case IO_ERROR:
                                                message = "Input/Output error";
                                                break;
                                            case DECODING_ERROR:
                                                message = "Image can't be decoded";
                                                view.setVisibility(View.GONE);
                                                break;
                                            case NETWORK_DENIED:
                                                message = "Downloads are denied";
                                                break;
                                            case OUT_OF_MEMORY:
                                                message = "Out Of Memory error";
                                                break;
                                            case UNKNOWN:
                                                message = "Unknown error";
                                                break;
                                        }
//                                        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

                                        spinner.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        spinner.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                ImageLoader.getInstance().displayImage(ImageLoader.getInstance().getDiskCache().get("file://" + items.get(position).getPath()).getAbsolutePath(), imageView);
                            }

                        } else {
                            imageView.setBackgroundColor(GalleryAct.this.getResources().getColor(R.color.colorMajor));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso
                                            .with(GalleryAct.this)
                                            .load("file://" + items.get(position).getPath())
                                            .resize(ImageUtil.getDeviceHeight(GalleryAct.this), ImageUtil.getDeviceWidth(GalleryAct.this))
                                            .centerInside()
                                            .onlyScaleDown()
                                            .rotate(ExifUtil.getOrientation(GalleryAct.this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + items.get(position).getId())))
                                            .into(imageView);
                                }
                            }, 0);
                        }
                    } else {
                        imageView.setBackgroundColor(GalleryAct.this.getResources().getColor(R.color.colorMajor));
                    }
                }
            } else {
                if (items.get(position) != null) {
                    if (!TextUtil.isNull(items.get(position).getPath())) {
                        if (ImageLoader.getInstance().getDiskCache().get("file://" + items.get(position).getPath()) != null) {
                            ImageLoader.getInstance().displayImage("file://" + items.get(position).getPath(), imageView, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    spinner.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    String message = null;
                                    switch (failReason.getType()) {
                                        case IO_ERROR:
                                            message = "Input/Output error";
                                            break;
                                        case DECODING_ERROR:
                                            message = "Image can't be decoded";
                                            break;
                                        case NETWORK_DENIED:
                                            message = "Downloads are denied";
                                            break;
                                        case OUT_OF_MEMORY:
                                            message = "Out Of Memory error";
                                            break;
                                        case UNKNOWN:
                                            message = "Unknown error";
                                            break;
                                    }
//                                    Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    spinner.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            ImageLoader.getInstance().displayImage(ImageLoader.getInstance().getDiskCache().get("file://" + items.get(position).getPath()).getAbsolutePath(), imageView);
                        }

                    } else {
                        imageView.setBackgroundColor(GalleryAct.this.getResources().getColor(R.color.colorMajor));
                    }
                }
            }


            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_MOVEACT) {
                String destinationFolderPath = data.getStringExtra("pathFromMoveAct");
                DebugUtil.showDebug("GalleryAct, onActivityResult() compare::" + currentImageFileImage.getParentPath() + "==???" + destinationFolderPath);

                if (!currentImageFileImage.getParentPath().equals(destinationFolderPath)) {

                    FileUtil.moveFile(currentImageFileImage.getPath(), destinationFolderPath);
                    DebugUtil.showDebug("GalleryAct, before update DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(this, currentImageFileImage.getId(), MediaStore.Images.Media.DATA));//업데이트 이전
                    ContentResolver mCr = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, destinationFolderPath + "/" + new File(currentImageFileImage.getPath()).getName());
                    mCr.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Images.ImageColumns._ID + "=" + currentImageFileImage.getId(), null);
                    DebugUtil.showDebug("GalleryAct, after update DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(this, currentImageFileImage.getId(), MediaStore.Images.Media.DATA));//업데이트 이후

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 1000);
                } else {
                    DebugUtil.showToast(this, "이동하려는 폴더가 현재 폴더와 같습니다");
                }
            }

            if (requestCode == ConstantUtil.GALLERYACT_REQUESTCODE_FOR_COPYACT) {
                String destinationFolderPath = data.getStringExtra("pathFromMoveAct");
                DebugUtil.showDebug("GalleryAct, onActivityResult() compare::" + currentImageFileImage.getParentPath() + "==???" + destinationFolderPath);

                if (!currentImageFileImage.getParentPath().equals(destinationFolderPath)) {

                    FileUtil.copyFile(currentImageFileImage.getPath(), destinationFolderPath);

                    //insert
                    DebugUtil.showDebug("GalleryAct, before inserted DB _DATA::" + FileUtil.viewColumnInfoOfSpecificImageFile(this, currentImageFileImage.getId(), MediaStore.Images.Media.DATA));//업데이트 이전
                    ContentResolver mCr = getContentResolver();
                    MediaStore.Images.Media media = new MediaStore.Images.Media();
                    String imageName = new File(currentImageFileImage.getPath()).getName();
                    try {
                        media.insertImage(mCr, destinationFolderPath + "/" + imageName, imageName, imageName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        DebugUtil.showDebug(e.getMessage());
                    }

                    //업데이트
                    File newFile = new File(destinationFolderPath + "/" + imageName);
                    if(newFile.exists()){
//                        new SingleMediaScanner(this, newFile);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));
                    }


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageAdapter.setItems(imagesInSameFolder);
                                    onBackPressed();

                                }
                            });
                        }
                    }, 1000);
                    DebugUtil.showDebug("GalleryAct, after inserted DB _DATA::" + FileUtil.viewIdOfSpecificImageFileUsingPath(this, destinationFolderPath + "/" + imageName, MediaStore.Images.Media._ID));//업데이트 이후
                } else {
                    DebugUtil.showDebug("GalleryAct, onActivityResult(), 복사할 파일 이름이 중복됨, 퀵픽과 같이 아무런 동작 안 함");
                }
            }
        }
    }
}
