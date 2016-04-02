package kr.ac.korea.intelligentgallery.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.listener.CommonDialogListener;
import kr.ac.korea.intelligentgallery.util.TextUtil;

public class CommonDialog extends DialogFragment implements View.OnClickListener {

    public static final int POSITIVE = -1;
    public static final int NEGATIVE = -2;

    public int dlgId;
    public CommonDialogListener commonDialogListener;
    public boolean isExistBothButton;
    public String titleMsgRes;
    public String contentMsgRes;
    public String newFolderName;
    public EditText edtTextNewFolderName;
    public String txtPositiveBtn;
    public String txtNegativeBtn;
    private View view;

    private TextView txtTitle, txtNegative, txtPositive;
    private EditText txtMessage;

    public void setDialogSettings(CommonDialogListener alertDialogListener, int dlgId,
                                  boolean isExistBothButton, String titleMsgResStr, String contentMsgRes,  String txtNegativeBtn, String txtPositiveBtn) {
        this.dlgId = dlgId;
        this.commonDialogListener = alertDialogListener;
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

        view = inflater.inflate(R.layout.dial_renaming, null);

        txtTitle = (TextView) view.findViewById(R.id.txt_title);
//        txtMessage = (EditText) view.findViewById(R.id.edittxt_renaming);
        edtTextNewFolderName = (EditText) view.findViewById(R.id.edittxt_renaming);
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
                if (commonDialogListener != null)
                    commonDialogListener.onClickCommonDialog(getDialog(), NEGATIVE, "");
                break;
            case R.id.txt_positive:
                if(!TextUtil.isNull(edtTextNewFolderName.getText().toString())) {
                    newFolderName = edtTextNewFolderName.getText().toString();
                }

                if (commonDialogListener != null)
                    commonDialogListener.onClickCommonDialog(getDialog(), POSITIVE, newFolderName);
                break;
        }

    }

    private void initializeUI() {

        txtTitle.setVisibility(View.GONE);
        edtTextNewFolderName.setVisibility(View.GONE);
        txtNegative.setVisibility(View.GONE);
        txtPositive.setVisibility(View.GONE);

        if (!TextUtil.isNull(titleMsgRes)) {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(titleMsgRes);
        }

        edtTextNewFolderName.setVisibility(View.VISIBLE);
        edtTextNewFolderName.setText(contentMsgRes);
//        if (!TextUtil.isNull(contentMsgRes)) {
//            edtTextNewFolderName.setVisibility(View.VISIBLE);
//            edtTextNewFolderName.setText(contentMsgRes);
//        }

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
