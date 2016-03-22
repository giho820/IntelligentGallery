package kr.ac.korea.intelligentgallery.common;

/**
 * Custom your own ViewPager to extends support ViewPager. java source:  Created by azi on 2013-6-21.
 */
/** Created by azi on 2013-6-21.  */


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import kr.ac.korea.intelligentgallery.listener.OnPhotoViewClickedListener;

public class ViewPagerFixed extends android.support.v4.view.ViewPager {

    private boolean isGalleryToolbarReveal;
    private OnPhotoViewClickedListener mOnPhotoViewClickedListener;


    public ViewPagerFixed(Context context) {
        super(context);
        isGalleryToolbarReveal = false;
    }

    public ViewPagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
        isGalleryToolbarReveal = false;
        mOnPhotoViewClickedListener = null;
    }

    public void setOnPhotoViewClickedListener(OnPhotoViewClickedListener onPhotoViewClickedListener) {
        this.mOnPhotoViewClickedListener = onPhotoViewClickedListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {

            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
//            isGalleryToolbarReveal = !isGalleryToolbarReveal;
//            DebugUtil.showDebug("ViewpagerFixed, onInterceptTouchEvent, isGalleryToolbarReveal : " + isGalleryToolbarReveal);
//            if(isGalleryToolbarReveal) {
//                mOnPhotoViewClickedListener.onPhotoViewClicked(true);
//            } else {
//                mOnPhotoViewClickedListener.onPhotoViewClicked(false);
//            }
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }


}