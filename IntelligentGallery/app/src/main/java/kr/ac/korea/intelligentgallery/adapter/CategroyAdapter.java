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

import java.util.List;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.CategoryAct;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;

/**
 * Created by kiho on 2015. 12. 10..
 */
public class CategroyAdapter extends BaseAdapter {

    private Activity mActivity;
    private Context mContext;
    private List<ImageFile> items;

    public CategroyAdapter(Activity activity, Context context, List<ImageFile> items) {
        this.mActivity = activity;
        this.mContext = context;
        this.items = items;

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(mContext));
        }
    }

    public class ViewHolder {
        public ImageView folderThumbnail;
        public CheckBox checkBox;
        public TextView folder_name;
        public TextView file_count;
        public LinearLayout item_list_info;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        if (items != null && items.size() > position)
            return items.get(position);
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
        final ImageFile item = items.get(position);

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_folder, null);

            viewHolder = new ViewHolder();
            viewHolder.folderThumbnail = (ImageView) convertView.findViewById(R.id.imageView_folder_thumbnail);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.item_list_checkbox);
            viewHolder.folder_name = (TextView) convertView.findViewById(R.id.item_list_folder_name);
            viewHolder.file_count = (TextView) convertView.findViewById(R.id.item_list_file_count);
            viewHolder.item_list_info = (LinearLayout) convertView.findViewById(R.id.item_list_info);

            int w = ImageUtil.getDeviceWidth(mContext);
            int widthBetweenImages = mContext.getResources().getDimensionPixelSize(R.dimen.dp_3);

            convertView.setLayoutParams(new AbsListView.LayoutParams(w / MainAct.GridViewFolderNumColumns - widthBetweenImages, w / MainAct.GridViewFolderNumColumns - widthBetweenImages));
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //카테고리명 표시
        viewHolder.item_list_info.setVisibility(View.VISIBLE);
        viewHolder.folder_name.setText("");
        if (!TextUtil.isNull(item.getCategoryName())) {
            viewHolder.folder_name.setText(item.getCategoryName());
        }

        //카테고리 하위 항목 개수 표시
        viewHolder.file_count.setText("");
        if (item.getCategoryId() != null) {
            int numItemsInsideClickedCId = FileUtil.itemsCountInsideCategory(item.getCategoryId());
            viewHolder.file_count.setText("" + numItemsInsideClickedCId);
        }


        viewHolder.folderThumbnail.setBackgroundColor(mContext.getResources().getColor(R.color.c_ff222222));
        if (item.getRecentImageFileID() != null) {
            //폴더의 썸네일 표시
            String imagePath = FileUtil.getImagePath(mContext, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + item.getRecentImageFileID()));
            if (ImageLoader.getInstance().getDiskCache().get("file://" + imagePath) != null) {
                ImageLoader.getInstance().displayImage("file://" + imagePath, viewHolder.folderThumbnail);
            } else {
                ImageLoader.getInstance().displayImage(ImageLoader.getInstance().getDiskCache().get("file://" + imagePath).getAbsolutePath(), viewHolder.folderThumbnail);
            }
        }

        //체크박스 초기화
        viewHolder.checkBox.setChecked(false);
        viewHolder.checkBox.setVisibility(View.GONE);

        if (MainAct.longClicked) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }

//        if (item.getIsChecked()) {
//            viewHolder.checkBox.setChecked(true);
//        } else {
//            viewHolder.checkBox.setChecked(false);
//        }

        //개별 이미지 클릭 리스너
        viewHolder.folderThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategroyAdapter,  viewHolder.folderThumbnail.setOnClickListener() 카테고리 영역 클릭 시  ");
                int clickedCid = item.getCategoryId();

                String cNameOriginal = DiLabClassifierUtil.centroidClassifier.getCategoryName(clickedCid);
                String cName = DiLabClassifierUtil.cNameConverter.convert(cNameOriginal);

                if (!MainAct.longClicked) {

                    //FolderCategoryAct 이동
                    Intent intent = new Intent(mContext, CategoryAct.class);
                    intent.putExtra("clickedCid", clickedCid);
                    intent.putExtra("ClickedCname", cName);

                    MoveActUtil.moveActivity(mActivity, intent, -1, -1, false, false);

                }
            }
        });

        //개별 이미지 롱클릭 리스너
//        viewHolder.folderThumbnail.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                DebugUtil.showDebug("CategoryAdapter, getView, onLongClick() ");
//                MainAct.longClicked = true;
//                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
//                notifyDataSetChanged();
//
//                return false;
//            }
//        });

        //체크박스 클릭 리스너
//        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                DebugUtil.showDebug("position : " + position);
////                DebugUtil.showDebug("true or false : " + viewHolder.checkBox.isChecked());
//                item.setIsChecked(viewHolder.checkBox.isChecked());
//            }
//        });


        return convertView;
    }

    public void addItem(ImageFile item) {

        if (item == null) {
            return;
        }
        if (this.items != null) {
            this.items.add(items.size() - 1, item);
            DebugUtil.showDebug("CategroyAdapter, addItem(), " + item.getPath() + " added");
        }
    }


}
