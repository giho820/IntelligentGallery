package kr.ac.korea.intelligentgallery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.listener.AdapterItemClickListener;


/**
 * Created by preparkha on 2015. 11. 12..
 */
public class MatchingImageRecyclerAdapter extends RecyclerView.Adapter {

    private ImageFile item;
    private ArrayList<ImageFile> matchingResultImages;
    private static Context context;
    private AdapterItemClickListener adapterItemClickListener;

    public static class MatchImgViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView matchingImg;

        private AdapterItemClickListener itemClickListener;

        public MatchImgViewHolder(View v) {
            super(v);

            matchingImg = (ImageView) v.findViewById(R.id.item_iv_matching);
            matchingImg.setOnClickListener(this);
        }

        public void setClickListener(AdapterItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onAdapterItemClick(v, getPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MatchingImageRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void setAdapterItemClickListener(AdapterItemClickListener adapterItemClickListener) {
        this.adapterItemClickListener = adapterItemClickListener;
    }

    public void setItem(ImageFile item) {
        this.item = item;
    }

    public void setAdapterArrayList(ArrayList<ImageFile> adapterArrayList) {
        this.matchingResultImages = adapterArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matching_image, parent, false);
        RecyclerView.ViewHolder vh = new MatchImgViewHolder(v);
        ((MatchImgViewHolder) vh).setClickListener(adapterItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        item = matchingResultImages.get(position);

        if (holder instanceof MatchImgViewHolder) {
            /** message's header **/

//            ((MessageViewHolder) holder).messageLinearLayout;
            ((MatchImgViewHolder) holder).matchingImg.setImageBitmap(null);

            if (item != null) {
                if (!ImageLoader.getInstance().isInited()) {
                    ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
                }
                ImageLoader.getInstance().displayImage(item.getPath(), ((MatchImgViewHolder) holder).matchingImg);
            }

        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (matchingResultImages != null)
            itemCount = matchingResultImages.size();
        return itemCount;
    }

}
