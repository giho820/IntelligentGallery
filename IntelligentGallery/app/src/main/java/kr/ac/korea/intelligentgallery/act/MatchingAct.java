package kr.ac.korea.intelligentgallery.act;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MatchingAct extends Activity {

    public PhotoViewAttacher mAttacher;

    Integer matchedImageId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        matchedImageId = getIntent().getIntExtra("clicked_matching_image", 0);
        DebugUtil.showDebug("matchedImgeId : " + matchedImageId);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        }

        PhotoView imageView = (PhotoView) findViewById(R.id.imageView_mathching);
        String imagePath = FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + matchedImageId));
        DebugUtil.showDebug("imagePath" + imagePath);
        ImageLoader.getInstance().displayImage("file://" + imagePath, imageView);

        // 사진 확대/축소 기능 추가
//        mAttacher = new PhotoViewAttacher(imageView);
//        mAttacher.setMaximumScale(6.0f);
//        mAttacher.setMediumScale(IPhotoView.DEFAULT_MID_SCALE);
//        mAttacher.setMinimumScale(IPhotoView.DEFAULT_MIN_SCALE);

    }


}
