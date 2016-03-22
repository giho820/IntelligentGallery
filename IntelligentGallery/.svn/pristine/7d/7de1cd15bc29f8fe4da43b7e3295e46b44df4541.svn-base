package kr.ac.korea.intelligentgallery.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import kr.ac.korea.intelligentgallery.R;

/**
 * Created by kiho on 2015. 9. 8..
 * @프로그램 설명 : 앱에서 공통으로 사용하는 로딩화면을 정의해놓은 클래스
 */
public class CommonLoadingDialog extends Dialog {

    public CommonLoadingDialog(Context context) {
        super(context, android.R.style.Theme_Holo_Light_NoActionBar);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dialog 배경을 투명 처리 해준다.
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dlg_progress);
    }
}
