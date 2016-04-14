package kr.ac.korea.intelligentgallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.MainAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.fragment.CategoryFrag;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


/**
 * 이미지 어댑터
 */
public class CategoryFragImageAdapter extends BaseAdapter {

    private Context context;
    public ArrayList<ImageFile> items;
    public LayoutInflater inflater;
    public DisplayImageOptions options;
    public ImageLoaderConfiguration config;

    public CategoryFragImageAdapter(Context context, ArrayList<ImageFile> items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);

        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(context));
    }

    public void addItems(ArrayList<ImageFile> items) {
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.removeAll(this.items);
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<ImageFile> items) {
        DebugUtil.showDebug("setItems() in imageAdapter");
        this.items = items;
//            imageLoader.init(config);
//            notifyDataSetChanged();
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
        final ImageFile item = items.get(position);

        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

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
        holder.checkBoxItemList.setChecked(false);
        holder.checkBoxItemList.setVisibility(View.GONE);
        if (CategoryFrag.isLongClicked) {
            holder.checkBoxItemList.setVisibility(View.VISIBLE);
        }
        //체크박스 체크 초기화
//        if (getItem(position).getIsChecked()) {
//            holder.checkBoxItemList.setChecked(true);
//        } else {
//            holder.checkBoxItemList.setChecked(false);
//        }
//
        holder.checkBoxItemList.setChecked(item.getIsChecked());

        //개별 이미지 클릭 리스너
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategoryFrag, ImageAdapter, getView, onClick() selected position : " + position);

                if(!CategoryFrag.isLongClicked){
                    /** 갤러리 액티비티(개별 사진 보는 화면) 이동*/
                    CategoryFrag.categoryAct.goToGalleryAct(items, position);
                } else {
//                    holder.checkBoxItemList.setChecked(!items.get(position).getIsChecked());
//                    item.setIsChecked(!items.get(position).getIsChecked());
                    DebugUtil.showDebug("getItem(" + position + ").isChecked : " + getItem(position).getIsChecked());
                    if (getItem(position).getIsChecked()) { //원래 항목이 체크 상태였다면 체크해제 상태로 변경
                        CategoryFrag.selectedPositions.remove(position);
                        getItem(position).setIsChecked(false);
                        holder.checkBoxItemList.setChecked(false);
                        DebugUtil.showDebug("getItem(" + position + ").isChecked became: false!!");
                    } else {
                        CategoryFrag.selectedPositions.add(position);//원래 항목이 체크 해제 상태였다면 체크상태로 변경
                        getItem(position).setIsChecked(true);
                        holder.checkBoxItemList.setChecked(true);
                        DebugUtil.showDebug("getItem(" + position + ").isChecked became: true!!");
                    }
                    //selectedPosition : 선택 된 것들의 position을 가진 집합
                    DebugUtil.showDebug("selectedPosition : " + CategoryFrag.selectedPositions.toString());

                }
            }
        });

        //개별 이미지 롱클릭 리스너
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DebugUtil.showDebug("FolderFrag, ImageAdapter, getView, onLongClick() ");
                CategoryFrag.isLongClicked = true;
                //어댑터 갱신을 통해 화면에 체크박스가 나타나도록 함
                notifyDataSetChanged();


                //FolderCategory 액티비티의 메뉴를 변경해야한다
                CategoryFrag.categoryAct.toolbar.getMenu().clear();
                CategoryFrag.categoryAct.toolbar.inflateMenu(R.menu.menu_category_long_clicked);

//                //FolderCategory 액티비티의 메뉴를 변경해야한다
//                if(FolderFrag.folderCategoryAct.toolbar != null) {
//                    FolderFrag.folderCategoryAct.toolbar.getMenu().clear();
//                    FolderFrag.folderCategoryAct.toolbar.inflateMenu(R.menu.menu_folder_long_clicked);
//                }

                return false;
            }
        });

        //체크박스 클릭 리스너
        holder.checkBoxItemList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //체크박스에 대해서
                    case R.id.checkBox_item_list_image:
                        DebugUtil.showDebug("getItem(" + position + ").isChecked : " + getItem(position).getIsChecked());
                        if (getItem(position).getIsChecked()) { //원래 항목이 체크 상태였다면 체크해제 상태로 변경
                            CategoryFrag.selectedPositions.remove(position);
                            getItem(position).setIsChecked(false);
                            DebugUtil.showDebug("getItem(" + position + ").isChecked became: false!!");
                        } else {
                            CategoryFrag.selectedPositions.add(position);//원래 항목이 체크 해제 상태였다면 체크상태로 변경
                            getItem(position).setIsChecked(true);
                            DebugUtil.showDebug("getItem(" + position + ").isChecked became: true!!");
                        }

                        //selectedPosition : 선택 된 것들의 position을 가진 집합
                        DebugUtil.showDebug("selectedPosition : " + CategoryFrag.selectedPositions.toString());

                        //sortedSelectedPositons : 집합을 정렬하여 생성한 리스트
                        ArrayList<Integer> sortedSelectedPositons = new ArrayList<>();
                        sortedSelectedPositons.addAll(CategoryFrag.selectedPositions);
                        Collections.sort(sortedSelectedPositons);
                        CategoryFrag.selectedPositionsList = sortedSelectedPositons;
                        DebugUtil.showDebug("selectedPositionsList : " + CategoryFrag.selectedPositionsList.toString());

                        break;
                }
            }
        });

//        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.act_main_image_default, holder.image);
        holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));
        if (item != null) {
            if (!TextUtil.isNull(item.getPath())) {
                String imagePath = item.getPath();
                if(CategoryFrag.imageLoader.getDiskCache().get("file://" + imagePath) != null) {
                    CategoryFrag.imageLoader.displayImage("file://" + item.getPath(), holder.image);
                } else {
                    CategoryFrag.imageLoader.displayImage(CategoryFrag.imageLoader.getDiskCache().get("file://" +imagePath).getAbsolutePath(), holder.image);
                }
            } else {
                holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));
            }
        }

        return convertView;
    }

    class ViewHolder {
        ImageView image;
        CheckBox checkBoxItemList;
    }
}

