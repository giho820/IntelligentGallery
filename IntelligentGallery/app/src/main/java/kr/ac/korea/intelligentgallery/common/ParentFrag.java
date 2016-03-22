package kr.ac.korea.intelligentgallery.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


/**
 * Created by preparkha on 2015. 11. 25..
 */
public abstract class ParentFrag extends Fragment  {

    /**
     * soft keyboard
     */
    private InputMethodManager inputManager;

    public void showKeyboard(final EditText editText) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInputFromInputMethod(editText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);

            }
        });
    }

    public void hiddenKeyboard(EditText view) {
        if (inputManager == null)
            inputManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        if (inputManager.isActive()) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * loading
     */
    public void showLoading() {
        if (getActivity() != null && getActivity() instanceof ParentAct)
            ((ParentAct) getActivity()).showLoading();
    }

    public void hideLoading() {
        if (getActivity() != null && getActivity() instanceof ParentAct)
            ((ParentAct) getActivity()).hideLoading();
    }

    public void setCutOffBackgroundTouch(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });
    }

    public void onUpdate() {};

    public void onUIRefresh() {};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
