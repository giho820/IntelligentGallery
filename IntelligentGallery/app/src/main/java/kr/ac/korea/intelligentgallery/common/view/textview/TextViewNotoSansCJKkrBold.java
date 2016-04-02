package kr.ac.korea.intelligentgallery.common.view.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import kr.ac.korea.intelligentgallery.util.TextUtil;

public class TextViewNotoSansCJKkrBold extends TextView {

    public TextViewNotoSansCJKkrBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (TextUtil.NotoSansCJKkrBold != null)
            setTypeface(TextUtil.NotoSansCJKkrBold);
    }

    public TextViewNotoSansCJKkrBold(Context context) {
        super(context);
        if (TextUtil.NotoSansCJKkrBold != null)
            setTypeface(TextUtil.NotoSansCJKkrBold);
    }

    public TextViewNotoSansCJKkrBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (TextUtil.NotoSansCJKkrBold != null)
            setTypeface(TextUtil.NotoSansCJKkrBold);
    }

}
