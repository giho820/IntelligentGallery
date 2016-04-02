package kr.ac.korea.intelligentgallery.common.view.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import kr.ac.korea.intelligentgallery.util.TextUtil;

public class TextViewRobotoBold extends TextView {

    public TextViewRobotoBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (TextUtil.RobotoBold != null)
            setTypeface(TextUtil.RobotoBold);
    }

    public TextViewRobotoBold(Context context) {
        super(context);
        if (TextUtil.RobotoBold != null)
            setTypeface(TextUtil.RobotoBold);
    }

    public TextViewRobotoBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (TextUtil.RobotoBold != null)
            setTypeface(TextUtil.RobotoBold);
    }

}
