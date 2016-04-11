package kr.ac.korea.intelligentgallery.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.Serializable;
import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.act.CategoryAct;
import kr.ac.korea.intelligentgallery.act.FolderCategoryAct;
import kr.ac.korea.intelligentgallery.act.GalleryAct;
import kr.ac.korea.intelligentgallery.act.ViewMoreAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.ConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;

/**
 * Created by preparkha on 15. 6. 9..
 */
public abstract class ParentAct extends ActionBarActivity {

    public CommonLoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @param frag
     * @param dlgIdx
     */
    public void dlgShow(Fragment frag, String dlgIdx) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(dlgIdx);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ((DialogFragment) frag).show(ft, dlgIdx);
    }

    public void showLoading() {
        try {
            if (loading == null) {
                return;
            }
            DebugUtil.showDebug("showLoading()......");
            loading.show();
        } catch (Exception e) {
            DebugUtil.showDebug("showLoading() : " + e.getMessage());
        }
    }

    public void hideLoading() {
        try {
            if (loading == null) {
                return;
            }
            loading.dismiss();
        } catch (Exception e) {
            DebugUtil.showDebug("hideLoading() : " + e.getMessage());
        }
    }

    public void setLoading(Activity activity) {
        if (activity == null) {
            return;
        }
        loading = new CommonLoadingDialog(activity);
    }


    public void switchContent(Fragment fragment, int contentId, boolean isHistory, boolean isAni) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (isHistory)
            ft.addToBackStack(null);
        if (isAni) {
            ft.setCustomAnimations(R.anim.left, R.anim.left2, R.anim.right2, R.anim.right);
        }
        ft.replace(contentId, fragment).commit();
    }

    /* 로그인 이동 */
    public void goLogin() {
//        MoveActUtil.chageActivity(this, LoginAct.class, R.anim.left, R.anim.hold, false, true);
    }

    /*******************************
     * go to the other activity
     *******************************/

    public void goToFolderCategoryAct(String path) {
        Intent intent = new Intent(this, FolderCategoryAct.class);
        intent.putExtra("path", path);

        MoveActUtil.moveActivity(this, intent, -1, -1, false, false);
    }
    //폴더에서 클릭했을 시
    public void goToFolderCategoryAct(Album album) {
        Intent intent = new Intent(this, FolderCategoryAct.class);
        intent.putExtra("album", album);

        MoveActUtil.moveActivity(this, intent, -1, -1, false, false);
    }
    //카테고리에서 클릭했을 시
    public void goToCategoryAct(Integer clickedCid, String clickedCategroyName) {
        Intent intent = new Intent(this, CategoryAct.class);
        intent.putExtra("clickedCid", clickedCid);
        intent.putExtra("ClickedCname", clickedCategroyName);

        MoveActUtil.moveActivity(this, intent, -1, -1, false, false);
    }

    public void goToGalleryAct(ArrayList<ImageFile> items, Integer position) {
        Intent intent = new Intent(this, GalleryAct.class);
        intent.putExtra("imageItems", (Serializable) items);
        intent.putExtra("selectedPostion", position);
        startActivityForResult(intent, ConstantUtil.GALLERYACT_REQUESTCODE_FOR_IMAGES_REFRESH);
    }

    public void goToViewMoreAct(ArrayList<ImageFile> items, String categoryName) {
        Intent intent = new Intent(this, ViewMoreAct.class);
        intent.putExtra("categroyImages", (Serializable) items);
        intent.putExtra("categoryName" , categoryName);
        MoveActUtil.moveActivity(this, intent, -1, -1, false, false);
    }

    // about keyboard

    /**
     * hide keyboard
     *
     * @param et
     */
    public void hideKeyboard(EditText et) {
        if (this.getCurrentFocus() == null)
            return;
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}

