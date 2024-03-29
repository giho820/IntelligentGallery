package kr.ac.korea.intelligentgallery.act;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.service.MakingContentDBService;
import kr.ac.korea.intelligentgallery.util.MoveActUtil;

public class IntroAct extends ParentAct {
    DatabaseHelper databaseHelper;
    ParentAct parentAct;

    public IntroAct() {
    }

    public IntroAct(ParentAct parentAct) {
        this.parentAct = parentAct;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        parentAct = (ParentAct) getParent();
        databaseHelper = DatabaseHelper.getInstacnce(this);

        setLoading(this);

        //서비스 실행
        Intent intent = new Intent(this, MakingContentDBService.class);
        startService(intent);


        //MainAct 이동
        showLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    hideLoading();
                MoveActUtil.chageActivity(IntroAct.this, MainAct.class, -1, -1, true, false);
            }
        }, 0);

    }
}



