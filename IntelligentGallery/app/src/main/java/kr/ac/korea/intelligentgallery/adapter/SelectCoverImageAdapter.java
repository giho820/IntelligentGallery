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

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


/**
 * 이미지 어댑터
 */
public class SelectCoverImageAdapter extends BaseAdapter {

    public ArrayList<ImageFile> items;
    public LayoutInflater inflater;
    private Activity mActivity;
    private Context context;
    private int count = 0;


    public SelectCoverImageAdapter(Activity activity, Context context, ArrayList<ImageFile> items) {
        this.mActivity = activity;
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(context));
        }
    }

    public SelectCoverImageAdapter(Activity activity, Context context) {
        this.mActivity = activity;
        this.context = context;
        inflater = LayoutInflater.from(context);

        if (ImageLoader.getInstance() != null && !ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(this.context));
        }
    }

    public void addItems(ArrayList<ImageFile> items) {
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.removeAll(this.items);
        this.items.addAll(items);
        count = this.items.size();
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<ImageFile> items) {
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public ImageFile getItem(int position) {
        if (items != null && items.size() > position) {
            return items.get(position);
        } else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_image, null);

            holder = new ViewHolder();
            holder.checkBoxItemList = (CheckBox) convertView.findViewById(R.id.checkBox_item_list_image);
            holder.checkBoxItemList.setVisibility(View.GONE);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView_item_list_image);
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));

            int w = ImageUtil.getDeviceWidth(context);
            int widthBetweenImages = context.getResources().getDimensionPixelSize(R.dimen.dp_3);

//            convertView.setLayoutParams(new FrameLayout.LayoutParams(w / MainAct.GridViewFolderNumColumns - widthBetweenImages, w / MainAct.GridViewFolderNumColumns - widthBetweenImages));
            convertView.setLayoutParams(new AbsListView.LayoutParams(w / MainAct.GridViewFolderNumColumns - widthBetweenImages, w / MainAct.GridViewFolderNumColumns - widthBetweenImages));
            convertView.setTag(holder);

        } else {
//            DebugUtil.showDebug("FolderFrag, getView(), converView != null");
            holder = (ViewHolder) convertView.getTag();
        }
        if (items == null || items.size() < position)
            return convertView;

        ImageFile item = getItem(position);
        if (item != null) {
            if (!TextUtil.isNull(item.getPath())) {
                String imagePath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + item.getId()));
                if (ImageLoader.getInstance().getDiskCache().get("file://" + imagePath) != null) {
                    ImageLoader.getInstance().displayImage("file://" + imagePath, holder.image);
                } else {
                    ImageLoader.getInstance().displayImage(ImageLoader.getInstance().getDiskCache().get("file://" + imagePath).getAbsolutePath(), holder.image);
                }
            } else {
                holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));
            }
        }

        //개별 이미지 클릭 리스너
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("FolderFrag, ImageAdapter, getView, onClick() selected position : " + position);
                /** 갤러리 액티비티(개별 사진 보는 화면) 이동*/

                DebugUtil.showDebug("선택된 것 :: " + items.get(position).getPath());

                Intent intent = new Intent(mActivity, MainAct.class);
                intent.putExtra("seletedCoverImageId", items.get(position).getId());
                intent.putExtra("seletedCoverImagePath", items.get(position).getPath());
                DebugUtil.showDebug("items.get(position).getId()::" + items.get(position).getId());
                DebugUtil.showDebug("items.get(position).getPath()::" + items.get(position).getPath());
                mActivity.setResult(Activity.RESULT_OK, intent);
                mActivity.finish();
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView image;
        CheckBox checkBoxItemList;
    }
}

