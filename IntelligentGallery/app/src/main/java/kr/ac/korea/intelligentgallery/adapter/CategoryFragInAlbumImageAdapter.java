package kr.ac.korea.intelligentgallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.Category;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.fragment.CategoryFragInAlbum;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.ImageUtil;
import kr.ac.korea.intelligentgallery.util.TextUtil;


/**
 * 이미지 어댑터
 */
public class CategoryFragInAlbumImageAdapter extends BaseAdapter {

    public Album album;
    public ArrayList<Category> items;
    public LayoutInflater inflater;
    public DisplayImageOptions options;
    public ImageLoaderConfiguration config;
    private Context context;

    public CategoryFragInAlbumImageAdapter(Context context, Album album) {
        this.context = context;
        if (album != null) {
            this.album = album;
        }
        inflater = LayoutInflater.from(context);

        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(context));
    }

    public CategoryFragInAlbumImageAdapter(Context context, Album album, ArrayList<Category> items) {
        this.context = context;
        if (album != null) {
            this.album = album;
            this.items = items;
        }
        inflater = LayoutInflater.from(context);

        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageUtil.intelligentGalleryGlobalImageLoaderConfiguration(context));
    }

    public void addItems(ArrayList<Category> items) {
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.removeAll(this.items);
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<Category> items) {
//        if (items == null)
//            return;
//        if (this.items == null)
//            this.items = new ArrayList<>();
//        this.items.addAll(items);
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items == null)
            return 0;
        return items.size();
    }

    @Override
    public Category getItem(int position) {
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
        final Category category = items.get(position);

        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.layout_listitem_category_in_album, null);
            holder.category_name = (TextView) convertView.findViewById(R.id.txt_categroy_name_in_category_album);
            holder.category_count = (TextView) convertView.findViewById(R.id.txt_categroy_count_in_category_album);
            holder.view_more = (TextView) convertView.findViewById(R.id.txt_view_more_in_category_album);
            holder.image_containing_linear_layout = (LinearLayout) convertView.findViewById(R.id.image_containing_linear_layout);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView_category_in_album_image1);
            holder.image2 = (ImageView) convertView.findViewById(R.id.imageView_category_in_album_image2);
            holder.image3 = (ImageView) convertView.findViewById(R.id.imageView_category_in_album_image3);
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));
            holder.image2.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));
            holder.image3.setBackgroundColor(context.getResources().getColor(R.color.c_ff222222));

            int w = ImageUtil.getDeviceWidth(context);
            int widthBetweenImages = context.getResources().getDimensionPixelSize(R.dimen.dp_3);
            holder.image_containing_linear_layout.setLayoutParams(new LinearLayout.LayoutParams(w, w / 3 - widthBetweenImages));

            convertView.setTag(holder);
        } else {
//            DebugUtil.showDebug("FolderFrag, getView(), converView != null");
            holder = (ViewHolder) convertView.getTag();
        }

        //파일 및 폴더명 표시
        holder.category_name.setVisibility(View.VISIBLE);
        holder.category_name.setText("");
        if (!TextUtil.isNull(category.getcName())) {
            holder.category_name.setText(category.getcName());
        }

        holder.category_count.setText("");
        //파일 및 폴더의 하위 항목 개수 표시
//        ArrayList<Integer> dIDsInsideCategoryFragInAlbum = DatabaseCRUD.dIDsInsideCategoryFragInAlbumUsingCId(category.getcID());
//        int numItemsInsideItem = FileUtil.getCountOfCategoryInCategoryFragInAlbum(context, category.getcID(), dIDsInsideCategoryFragInAlbum, album);
        holder.category_count.setText("" + category.getCount());

        //더보기 클릭 리스너
        holder.view_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> dIDsInsideCategoryFragInAlbum = DatabaseCRUD.dIDsInsideCategoryFragInAlbumUsingCId(category.getcID()); //전체중에 카테고리 아이디 같은 did (서브쿼리 결과)
                ArrayList<Integer> dIDsInSameCategoryAndSameDir = FileUtil.getDidsCategoryInCategoryFragInAlbum(context, category.getcID(), dIDsInsideCategoryFragInAlbum, album); //그 중에서 같은 앨범에 있는 did(조건 추가)
                ArrayList<ImageFile> imageFiles = FileUtil.getImagesUsingDids(context, dIDsInSameCategoryAndSameDir, album);//해당 did로 이미지 뽑아냄

                CategoryFragInAlbum.folderCategoryAct.goToViewMoreAct(imageFiles, category.getcName());
            }
        });

        //이미지 할당하기
        final ImageView[] imgHolderArr = {holder.image, holder.image2, holder.image3};
        holder.image.setVisibility(View.INVISIBLE);
        holder.image2.setVisibility(View.INVISIBLE);
        holder.image3.setVisibility(View.INVISIBLE);
//        ImageLoader.getInstance().displayImage("drawable://" + R.color.c_ff222222, holder.image);
//        ImageLoader.getInstance().displayImage("drawable://" + R.color.c_ff222222, holder.image2);
//        ImageLoader.getInstance().displayImage("drawable://" + R.color.c_ff222222, holder.image3);

        if (category != null) {

            for (int i = 0; i < 3; i++) {
                final int j = i;
                if (category.getContainingImages().size() > i && category.getContainingImages().get(i) != null) {
                    if (!TextUtil.isNull(category.getContainingImages().get(i).getPath())) {
                        if(ImageLoader.getInstance().getDiskCache().get("file://" + category.getContainingImages().get(j).getPath()) != null){
                            imgHolderArr[j].setVisibility(View.VISIBLE);
                            ImageLoader.getInstance().displayImage("file://" + category.getContainingImages().get(j).getPath(), imgHolderArr[j]);
                        } else {
                            ImageLoader.getInstance().displayImage(String.valueOf(ImageLoader.getInstance().getDiskCache().get("file://" + category.getContainingImages().get(j).getPath())), imgHolderArr[j]);
                        }
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                imgHolderArr[j].setVisibility(View.VISIBLE);
//                                ImageLoader.getInstance().displayImage("file://" + category.getContainingImages().get(j).getPath(), imgHolderArr[j]);
//                            }
//                        }, (j + 1) * 100);
                    }
                }
//                DebugUtil.showDebug("i : " + i +", category.getContainingImage : " + category.getContainingImages().get(i).getPath());
            }
        }


        //개별 이미지1 클릭 리스너
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategoryFrag, ImageAdapter, getView, onClick() selected position : " + position);
//                ArrayList<Integer> dIDsInsideCategoryFragInAlbum = DatabaseCRUD.dIDsInsideCategoryFragInAlbumUsingCId(category.getcID()); //전체중에 카테고리 아이디 같은 did (서브쿼리 결과)
//                ArrayList<Integer> dIDsInSameCategoryAndSameDir = FileUtil.getDidsCategoryInCategoryFragInAlbum(context, category.getcID(), dIDsInsideCategoryFragInAlbum, album); //그 중에서 같은 앨범에 있는 did(조건 추가)
//                ArrayList<ImageFile> imageFiles = FileUtil.getImagesUsingDids(context, dIDsInSameCategoryAndSameDir, album);//해당 did로 이미지 뽑아냄
//                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(imageFiles, 0);
                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(category.getContainingImages(), 0);

            }
        });

        //개별 이미지2 클릭 리스너
        holder.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategoryFrag, ImageAdapter, getView, onClick() selected position : " + position);
//                ArrayList<Integer> dIDsInsideCategoryFragInAlbum = DatabaseCRUD.dIDsInsideCategoryFragInAlbumUsingCId(category.getcID()); //전체중에 카테고리 아이디 같은 did (서브쿼리 결과)
//                ArrayList<Integer> dIDsInSameCategoryAndSameDir = FileUtil.getDidsCategoryInCategoryFragInAlbum(context, category.getcID(), dIDsInsideCategoryFragInAlbum, album); //그 중에서 같은 앨범에 있는 did(조건 추가)
//                ArrayList<ImageFile> imageFiles = FileUtil.getImagesUsingDids(context, dIDsInSameCategoryAndSameDir, album);//해당 did로 이미지 뽑아냄
//                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(imageFiles, 1);
                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(category.getContainingImages(), 1);
            }
        });

        //개별 이미지3 클릭 리스너
        holder.image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtil.showDebug("CategoryFrag, ImageAdapter, getView, onClick() selected position : " + position);
//                ArrayList<Integer> dIDsInsideCategoryFragInAlbum = DatabaseCRUD.dIDsInsideCategoryFragInAlbumUsingCId(category.getcID()); //전체중에 카테고리 아이디 같은 did (서브쿼리 결과)
//                ArrayList<Integer> dIDsInSameCategoryAndSameDir = FileUtil.getDidsCategoryInCategoryFragInAlbum(context, category.getcID(), dIDsInsideCategoryFragInAlbum, album); //그 중에서 같은 앨범에 있는 did(조건 추가)
//                ArrayList<ImageFile> imageFiles = FileUtil.getImagesUsingDids(context, dIDsInSameCategoryAndSameDir, album);//해당 did로 이미지 뽑아냄
//                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(imageFiles, 2);
                CategoryFragInAlbum.folderCategoryAct.goToGalleryAct(category.getContainingImages(), 2);
            }
        });


        return convertView;
    }

    class ViewHolder {
        public TextView category_name;
        public TextView category_count;
        public TextView view_more;
        public LinearLayout image_containing_linear_layout;
        public ImageView image;
        public ImageView image2;
        public ImageView image3;
    }
}

