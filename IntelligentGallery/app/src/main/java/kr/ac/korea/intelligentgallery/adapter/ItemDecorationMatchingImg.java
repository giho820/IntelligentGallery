package kr.ac.korea.intelligentgallery.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wonderland on 16. 3. 11..
 * ItemDecorationMatchingImg
 */
public class ItemDecorationMatchingImg extends RecyclerView.ItemDecoration {

    private int mSizeHorizontalSpacingPx;
    private int mHorizontalSize;

    private boolean mNeedLeftSpacing = false;

    public ItemDecorationMatchingImg(int horizontalSpacingPx, int mHorizontalSize) {
        this.mSizeHorizontalSpacingPx = horizontalSpacingPx;
        this.mHorizontalSize = mHorizontalSize;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewPosition();

        int frameWidth = (int) ((parent.getWidth() - (float) mSizeHorizontalSpacingPx * (childCount - 1)) / childCount);
        int padding = parent.getWidth() / childCount - frameWidth;
//        if (itemPosition < mHorizontalSize) {
//            outRect.top = 0;
//        } else {
//            outRect.top = mSizeHorizontalSpacingPx;
//        }
        if (itemPosition % childCount == 0) {
//            outRect.left = 0;
            outRect.left = padding;
            outRect.right = padding;
            mNeedLeftSpacing = true;
        } else if ((itemPosition + 1) % childCount == 0) {
            mNeedLeftSpacing = false;
//            outRect.right = 0;
            outRect.right = padding;
            outRect.left = padding;
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeHorizontalSpacingPx - padding;
            if ((itemPosition + 2) % childCount == 0) {
                outRect.right = mSizeHorizontalSpacingPx - padding;
            } else {
                outRect.right = mSizeHorizontalSpacingPx / 2;
            }
        } else if ((itemPosition + 2) % childCount == 0) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeHorizontalSpacingPx / 2;
            outRect.right = mSizeHorizontalSpacingPx - padding;
        } else {
            mNeedLeftSpacing = false;
            outRect.left = mSizeHorizontalSpacingPx / 2;
            outRect.right = mSizeHorizontalSpacingPx / 2;
        }
        outRect.bottom = 0;
    }
}
