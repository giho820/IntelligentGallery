package kr.ac.korea.intelligentgallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.FolderCategoryAct;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;

/**
 * Created by kiho on 2015. 12. 10..
 */
public class AlbumAdapter extends BaseAdapter {

    private Activity mActivity;
    private Context mContext;
    private List<Album> albums;
    private int count;

    public AlbumAdapter(Activity activity, Context context, List<Album> _albums) {
        this.mActivity = activity;
        this.mContext = context;
        this.albums = _albums;

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(mContext));
        }
    }

    public class ViewHolder {
        public ImageView folderThumbnail;
        public CheckBox checkBox;
        public LinearLayout item_list_info;
        public TextView folder_name;
        public TextView file_count;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        if (albums != null && albums.size() > position)
            return albums.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        final Album album = albums.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_folder, null);

            viewHolder = new ViewHolder();
            viewHolder.item_list_info = (LinearLayout) convertView.findViewById(R.id.item_list_info);
            viewHolder.folderThumbnail = (ImageView) convertView.findViewById(R.id.imageView_folder_thumbnail);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.item_list_checkbox);
            viewHolder.folder_name = (TextView) convertView.findViewById(R.id.item_list_folder_name);
            viewHolder.file_count = (TextView) convertView.findViewById(R.id.item_list_file_count);

            int w = ImageUtil.getDeviceWidth(mContext);
            int widthBetweenImages = mContext.getResources().getDimensionPixelSize(R.dimen.dp_3);

            convertView.setLayoutParams(new AbsListView.LayoutParams(w / MainAct.GridViewFolderNumColumns - widthBetweenImages, w / MainAct.GridViewFolderNumColumns - widthBetweenImages));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //폴더 숨기기 및 보이기
        if(album.isHide()){
            convertView.setVisibility(View.GONE);
            albums.remove(album);
            setAlbums(albums);
            notifyDataSetChanged();
        } else {
            convertView.setVisibility(View.VISIBLE);
        }

        //파일 및 폴더명 표시
        viewHolder.item_list_info.setVisibility(View.VISIBLE);
        viewHolder.folder_name.setText("");
        if (!TextUtil.isNull(album.getName())) {
            viewHolder.folder_name.setText(album.getName());
        }

        //파일 및 폴더의 하위 항목 개수 표시
        viewHolder.file_count.setText("");
        if (album.getPath() != null) {
            viewHolder.file_count.setText("" + album.count);
        }


        //폴더의 썸네일 표시
        String albumCoverImagePath = album.getCoverImagePath();

        //사용자가 선택한 표지 이미지가 있으면 적용할 것
        Integer userSelectedCoverImageId = DatabaseCRUD.getCoverImageIDOfSpecificAlbum(album);
        String userSelectedCoverImagePath = FileUtil.getImagePath(mContext, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + userSelectedCoverImageId));
        if(!TextUtil.isNull(userSelectedCoverImagePath)) {
//            DebugUtil.showDebug("AlbumAdapter, 해당 앨범에는 대표 이미지가 있음 :: " + userSelectedCoverImagePath);
            albumCoverImagePath = userSelectedCoverImagePath; //이 이미지로 적용
        } else {
//            DebugUtil.showDebug("AlbumAdapter, 해당 앨범에는 대표 이미지가 없음" + userSelectedCoverImagePath);
        }

        if(ImageLoader.getInstance().getDiskCache().get("file://"+albumCoverImagePath) != null){
            ImageLoader.getInstance().displayImage("file://" + albumCoverImagePath, viewHolder.folderThumbnail);
        } else {
            ImageLoader.getInstance().displayImage(ImageLoader.getInstance().getDiskCache().get("file://" + albumCoverImagePath).getAbsolutePath(), viewHolder.folderThumbnail);
        }


        //체크박스 초기화
        viewHolder.checkBox.setChecked(false);
        viewHolder.checkBox.setVisibility(View.GONE);
        if (MainAct.longClicked) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }

        //체크박스 체크 초기화
//        if (album.isChecked()) {
//        } else {
//            viewHolder.checkBox.setChecked(false);
//        }
        viewHolder.checkBox.setChecked(album.isChecked());

        //체크박스 클릭 리스너
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DebugUtil.showDebug(album.getName() + "position : " + position);
//                DebugUtil.showDebug("true or false : " + viewHolder.checkBox.isChecked());
                album.setIsChecked(viewHolder.checkBox.isChecked());
            }
        });

        //개별 이미지 클릭 리스너
        viewHolder.folderThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainAct.longClicked) {
                    //FolderCategoryAct 이동
                    Intent intent = new Intent(mContext, FolderCategoryAct.class);
//                    intent.putExtra("album", album);
                    intent.putExtra("album_path", album.getPath());

                    MoveActUtil.moveActivity(mActivity, intent, -1, -1, false, false);
                } else {
                    viewHolder.checkBox.setChecked(!album.isChecked());
                    album.setIsChecked(!album.isChecked());
                }
            }
        });

        //개별 이미지 롱클릭 리스너
        viewHolder.folderThumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DebugUtil.showDebug("AlbumAdapter, getView, onLongClick() ");
                MainAct.longClicked = true;
                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
                notifyDataSetChanged();

                return false;
            }
        });
        return convertView;
    }

    public void addItem(Album item) {

        if (item == null) {
            return;
        }
        if (this.albums != null) {
            this.albums.add(albums.size() - 1, item);
            DebugUtil.showDebug("CategroyAdapter, addItem(), " + item.getPath() + " added");
        }

    }
    public void addItems(ArrayList<Album> items) {
        if(this.albums == null){
            this.albums = new ArrayList<>();
            this.albums.addAll(items);
            notifyDataSetChanged();
        }
    }
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
        this.count = albums.size();
    }

    public void removeAllAlbums(){
        if(this.albums != null) {
            this.albums.removeAll(this.albums);
            notifyDataSetChanged();
        }


    }

}
