package kr.ac.korea.intelligentgallery.adapter;

import android.content.Context;
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
import kr.ac.korea.intelligentgallery.act.SearchResultAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.fragment.FolderFrag;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


/**
 * 이미지 어댑터
 */
public class SearchResultActImageAdapter extends BaseAdapter {

    private Context context;
    public ArrayList<ImageFile> items;
    public LayoutInflater inflater;

    public SearchResultActImageAdapter(Context context, ArrayList<ImageFile> items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);

        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(context));
    }

    public void setItems(ArrayList<ImageFile> items) {
        DebugUtil.showDebug("setItems() in imageAdapter");
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
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
            holder = new ViewHolder();

            DebugUtil.showDebug("FolderFrag, getView(), converView == null");
            convertView = inflater.inflate(R.layout.item_list_image, null);
            holder.checkBoxItemList = (CheckBox) convertView.findViewById(R.id.checkBox_item_list_image);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView_item_list_image);
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));

            int w = ImageUtil.getDeviceWidth(context);
            int widthBetweenImages = context.getResources().getDimensionPixelSize(R.dimen.dp_3);

            convertView.setLayoutParams(new AbsListView.LayoutParams(w / MainAct.GridViewFolderNumColumns - widthBetweenImages, w / MainAct.GridViewFolderNumColumns - widthBetweenImages));
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //체크박스 초기화(Visibility)
        holder.checkBoxItemList.setVisibility(View.GONE);
        if (FolderFrag.isLongClicked) {
            holder.checkBoxItemList.setVisibility(View.VISIBLE);
        }

        //개별 이미지 클릭 리스너
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategoryFrag, ImageAdapter, getView, onClick() selected position : " + position);

                /** 갤러리 액티비티(개별 사진 보는 화면) 이동*/
                SearchResultAct.searchResultAct.goToGalleryAct(items, position);
            }
        });



        ImageFile item = getItem(position);
//        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.act_main_image_default, holder.image);
        convertView.setVisibility(View.GONE);

        if (item != null) {
            if (!TextUtil.isNull(item.getPath())) {
                convertView.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage("file://" + item.getPath(), holder.image);
            }
        }

        return convertView;
    }

    class ViewHolder {
        ImageView image;
        CheckBox checkBoxItemList;
    }
}

