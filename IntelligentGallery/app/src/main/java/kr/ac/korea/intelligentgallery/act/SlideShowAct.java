package kr.ac.korea.intelligentgallery.act;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;

/**
 * Created by kiho on 2016. 3. 3..
 */
public class SlideShowAct extends Activity {
    private int whichFragThatStartsSlideShow;
    private Album album;
    private ArrayList<ImageFile> imageFiles;
    private ViewFlipper myViewFlipper;
    private float initialXPoint;
    int[] image = {R.drawable.ic_backkey, R.drawable.ic_launcher,
            R.drawable.ic_error, R.drawable.ic_empty, R.drawable.ic_menu_trash,
            R.drawable.ic_backkey, R.drawable.ic_launcher,
            R.drawable.ic_error, R.drawable.ic_empty, R.drawable.ic_menu_trash, R.drawable.ic_error};

    void SlideShowAct() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_show);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(this));
        }

        myViewFlipper = (ViewFlipper) findViewById(R.id.myflipper);
        myViewFlipper.setAutoStart(true);
        myViewFlipper.setFlipInterval(3000);
        myViewFlipper.startFlipping();

        whichFragThatStartsSlideShow = getIntent().getIntExtra("SlideShow Start Location is FolderFrag or CategoryFragInAlbum", 0);
        album = (Album) getIntent().getSerializableExtra("albumFromFolderFrag");
//        String albumPath = getIntent().getStringExtra("albumFromFolderFrag");
//        album = FileUtil.getAlbumsInSepecficLocation(this, albumPath);

        if (album != null) {
//            imageFiles = FileUtil.getImages(this, album);
            new MakingViewFlipperItemsInSlideShow().execute();

        } else {
            DebugUtil.showDebug("SlideshowAct, onCreate(), album is null:");
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialXPoint = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalx = event.getX();
                if (initialXPoint > finalx) {
                    if (myViewFlipper.getDisplayedChild() == image.length)
                        break;
                    myViewFlipper.showNext();
                } else {
                    if (myViewFlipper.getDisplayedChild() == 0)
                        break;
                    myViewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }


    public class MakingViewFlipperItemsInSlideShow extends AsyncTask<Integer, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DebugUtil.showDebug("MakingViewFlipperItemsInSlideShow, onPreExecute::");
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            ContentResolver mCr;
            Cursor cursor;
            ArrayList<ImageFile> images = new ArrayList<>();

            mCr = SlideShowAct.this.getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String albumID = album.getId();
            String[] projection = {MediaStore.Images.Media._ID};
            String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID;
            String selectionWhenCategoryFragInAlbum = MediaStore.Images.Media.BUCKET_ID + "=" + albumID +" and " + MediaStore.Images.ImageColumns.LATITUDE + " is not null and " +  MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
            String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서 정렬
            if(whichFragThatStartsSlideShow == 0) {
                cursor = mCr.query(uri, projection, selection, null, orderBy + " desc" + " limit 0, 30");
            } else {
                cursor = mCr.query(uri, projection, selectionWhenCategoryFragInAlbum, null, orderBy + " desc" + " limit 0, 30");
            }

            while (cursor.moveToNext()) {
                ImageFile imageFile = new ImageFile();
                final Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                imageFile.setId(viewItemID);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = new ImageView(SlideShowAct.this);
                        Bitmap bmp = ImageLoader.getInstance().loadImageSync(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID);
                        imageView.setImageBitmap(bmp);
                        myViewFlipper.addView(imageView);
                        DebugUtil.showDebug("MakingViewFlipperItemsInSlideShow, onPreExecute, imageView added::" + viewItemID);
                    }
                });


                String imageFileUriPath = FileUtil.getImagePath(SlideShowAct.this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
                imageFile.setPath(imageFileUriPath);

                images.add(imageFile);

            }

            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            DebugUtil.showDebug("MakingViewFlipperItemsInSlideShow, onPostExecute::");
        }

    }
}
