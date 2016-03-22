package kr.ac.korea.intelligentgallery.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.listener.MakingContentDBDialogListener;
import kr.ac.korea.intelligentgallery.util.TextUtil;

public class MakingContentDBDialog extends DialogFragment implements View.OnClickListener {

    public static final int POSITIVE = -1;
    public static final int NEGATIVE = -2;

    public int dlgId;
    public MakingContentDBDialogListener makingContentDBDialogListener;
    public boolean isExistBothButton;
    public String titleMsgRes;
    public String contentMsgRes;
    public String txtPositiveBtn;
    public String txtNegativeBtn;
    private View view;

    private TextView txtTitle, txtMessage, txtNegative, txtPositive;

    public void setDialogSettings(MakingContentDBDialogListener alertDialogListener, int dlgId,
                                  boolean isExistBothButton,
                                  String titleMsgResStr, String contentMsgRes, String txtNegativeBtn, String txtPositiveBtn) {
        this.dlgId = dlgId;
        this.makingContentDBDialogListener = alertDialogListener;
        this.isExistBothButton = isExistBothButton;
        this.titleMsgRes = titleMsgResStr;
        this.contentMsgRes = contentMsgRes;
        this.txtPositiveBtn = txtPositiveBtn;
        this.txtNegativeBtn = txtNegativeBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dial_intro_select_making_contents_db, null);

        txtTitle = (TextView) view.findViewById(R.id.txt_title);
        txtMessage = (TextView) view.findViewById(R.id.txt_message);
        txtNegative = (TextView) view.findViewById(R.id.txt_negative);
        txtPositive = (TextView) view.findViewById(R.id.txt_positive);

        txtNegative.setOnClickListener(this);
        txtPositive.setOnClickListener(this);

        initializeUI();

        return view;

    }

    @Override
    public void onClick(View v) {

        dismiss();

        switch (v.getId()) {
            case R.id.txt_negative:
                if (makingContentDBDialogListener != null)
                    makingContentDBDialogListener.onClickCommonDialog(getDialog(), NEGATIVE);
                break;
            case R.id.txt_positive:

                if (makingContentDBDialogListener != null)
                    makingContentDBDialogListener.onClickCommonDialog(getDialog(), POSITIVE);
                break;
        }

    }

    private void initializeUI() {

        txtTitle.setVisibility(View.GONE);
        txtMessage.setVisibility(View.GONE);
        txtNegative.setVisibility(View.GONE);
        txtPositive.setVisibility(View.GONE);

        if (!TextUtil.isNull(titleMsgRes)) {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(titleMsgRes);
        }

        if (!TextUtil.isNull(contentMsgRes)) {
            txtMessage.setVisibility(View.VISIBLE);
            txtMessage.setText(contentMsgRes);
        }


        if (isExistBothButton) {

            txtNegative.setVisibility(View.VISIBLE);
            txtPositive.setVisibility(View.VISIBLE);

            if (TextUtil.isNull(txtPositiveBtn)) {
                txtPositiveBtn = "확인";
            }
            if (TextUtil.isNull(txtNegativeBtn)) {
                txtNegativeBtn = "취소";
            }

            txtNegative.setText(txtNegativeBtn);
            txtPositive.setText(txtPositiveBtn);

        } else {
            if (!TextUtil.isNull(txtPositiveBtn)) {

                txtPositive.setVisibility(View.VISIBLE);
                txtPositive.setText(txtPositiveBtn);

            } else if (!TextUtil.isNull(txtNegativeBtn)) {

                txtNegative.setVisibility(View.VISIBLE);
                txtNegative.setText(txtNegativeBtn);

            }
        }


    }

}
