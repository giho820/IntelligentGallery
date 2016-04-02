package kr.ac.korea.intelligentgallery.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 * Created by kiho on 2015. 9. 7..
 */

public class TextUtil {

    public static Typeface NotoSansCJKkrRegular;
    public static Typeface NotoSansCJKkrMedium;
    public static Typeface NotoSansCJKkrBold;
    public static Typeface NotoSansCJKkrDemiLight;
    public static Typeface RobotoBold;

    public static Typeface NanumGothic;
    public static Typeface NanumGothicBold;
    public static Typeface NanumGothicExtraBold;


    /**
     * if source is null or empty
     * return true
     * else
     * return false
     *
     * @param source
     * @return
     */
    public static boolean isNull(String source) {
        boolean isNull = false;
        if (source == null || TextUtils.isEmpty(source))
            isNull = true;
        return isNull;
    }

    /**
     * 문자열인지 Y인지N인지?
     *
     * @param source
     * @return
     */
    public static boolean isYN(String source) {
        boolean isY = false;
        if (!isNull(source) && source.equals("Y")) {
            isY = true;
        }
        return isY;
    }

    /**
     * InputStream-> 문자열로 변환
     *
     * @param is
     * @return
     */
    public static String inputStreamToString(InputStream is, String charset) {
        BufferedReader br = null;
        StringBuffer sb = null;
        String data = null;
        String line = "";
        try {
            br = new BufferedReader(new InputStreamReader(is, charset));
            sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (Exception err) {

        } finally {
            line = null;
            sb = null;
            br = null;
        }

        return data;

    }

    /*
     * 최소문자열 길이 체크
     */
    public static boolean isLenghCheck(String str, int minCheck, int maxCheck) {
        boolean isCheck = false;

        DebugUtil.showDebug("str length::" + str.length());
        if (str.length() >= minCheck && str.length() <= maxCheck) {
            isCheck = true;
        }

        return isCheck;
    }

    /**
     * 한글 UTF-8 인코딩
     *
     * @param source
     * @return
     */
    public static String textEncoding(String source) {
        String encodingStr = null;
        try {
            encodingStr = URLEncoder.encode(source, "utf-8");
        } catch (Exception err) {
            encodingStr = "";
        }
        return encodingStr;
    }

    /**
     * 이메일 형식 체크(해당 정규화에 정확히 맞는지)
     *
     * @return
     */
    public static boolean isEmailCheck(String email) {
        boolean isConfirm = false;
        isConfirm = Pattern.matches("^[0-9a-zA-Z-]+(.[0-9a-zA-Z-]+)*@(?:\\w+\\.)+\\w+$", email);
        return isConfirm;
    }

    public static boolean isStringInteger(String s){
        boolean isConfirm = false;
        try{
            Integer.parseInt(s);
            isConfirm = true;
        } catch (NumberFormatException e) {
            isConfirm = false;
        }
        return isConfirm;
    }

    public static boolean isURlCheck(String url) {
        // URL
        // ^(https?):\/\/([^:\/\s]+)(:([^\/]*))?((\/[^\s/\/]+)*)?\/?([^#\s\?]*)(\?([^#\s]*))?(#(\w*))?$
        // (@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?
        // (@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?
        boolean isConfirm = false;
        isConfirm = Pattern.matches(
                "(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?(https://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?", url);
        return isConfirm;
    }

    public static String makeStringWithComma(String string, boolean ignoreZero) {
        if (string.length() == 0) {
            return "";
        }
        try {
            if (string.indexOf(".") >= 0) {
                double value = Double.parseDouble(string);
                if (ignoreZero && value == 0) {
                    return "";
                }
                DecimalFormat format = new DecimalFormat("###,##0.00");
                return format.format(value);
            } else {
                long value = Long.parseLong(string);
                if (ignoreZero && value == 0) {
                    return "";
                }
                DecimalFormat format = new DecimalFormat("###,###");
                return format.format(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static SpannableStringBuilder settingLink(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        ssb.setSpan(new UnderlineSpan(), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public static void setTextViewColorPartial(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static boolean checkLanguage(String s) {

        if (s == null)
            return false;

        boolean result = true;

        char[] temp = s.toCharArray();
        char frist = temp[0];
        if ((0xAC00 <= frist && frist <= 0xD7A3) || (0x3131 <= frist && frist <= 0x318E)) {
            // 한글
            result = true;
        } else if ((0x61 <= frist && frist <= 0x7A) || (0x41 <= frist && frist <= 0x5A)) {
            // 영어
            result = true;
        } else if (0x30 <= frist && frist <= 0x39) {
            // 숫자
            result = false;
        } else {
            // 그 외
            result = false;
        }

        return result;
    }

    /**
     * find initial sound
     */
    private static final int CHOSEONG_COUNT = 19;
    private static final int JUNGSEONG_COUNT = 21;
    private static final int JONGSEONG_COUNT = 28;
    private static final int HANGUL_SYLLABLE_COUNT = CHOSEONG_COUNT * JUNGSEONG_COUNT * JONGSEONG_COUNT;
    private static final int HANGUL_SYLLABLES_BASE = 0xAC00;
    private static final int HANGUL_SYLLABLES_END = HANGUL_SYLLABLES_BASE + HANGUL_SYLLABLE_COUNT;

    private static final int SSANGIEOK = 4353; // ㄲ
    private static final int SSANGDIGEUT = 4356; // ㄸ
    private static final int SSANGBIEUP = 4360; // ㅃ
    private static final int SSANGSIOT = 4362; // ㅆ
    private static final int SSANGJIAEUT = 4365; // ㅉ

    private static final int[] COMPAT_CHOSEONG_MAP = new int[]{0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
            0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E};

    public static boolean isChoseong(char c) {
        return 0x1100 <= c && c <= 0x1112;
    }

    public static boolean isCompatChoseong(char c) {
        return 0x3131 <= c && c <= 0x314E;
    }

    public static boolean isSyllable(char c) {
        return HANGUL_SYLLABLES_BASE <= c && c < HANGUL_SYLLABLES_END;
    }

    public static char getChoseong(char value) {
        if (!isSyllable(value))
            return '\0';

        final int choseongIndex = getChoseongIndex(value);
        return (char) (0x1100 + choseongIndex);
    }

    public static char getCompatChoseong(char value) {
        if (!isSyllable(value))
            return '\0';

        final int choseongIndex = getChoseongIndex(value);
        return (char) COMPAT_CHOSEONG_MAP[choseongIndex];
    }

    private static int getChoseongIndex(char syllable) {
        final int syllableIndex = syllable - HANGUL_SYLLABLES_BASE;
        final int choseongIndex = syllableIndex / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
        return choseongIndex;
    }

    public static char getSectionChar(char section) {

        char sectionChar;

        if (!isSyllable(section)) {
            // english
            char tempEnglish = section;
            if (tempEnglish < 97) {
                // upper
                sectionChar = tempEnglish;
            } else {
                // lower
                sectionChar = (char) (tempEnglish - 32);
            }
        } else {
            // korea
            char tempKor = getChoseong(section);
            switch (tempKor) {
                case SSANGIEOK: // ㄲ
                case SSANGDIGEUT: // ㄸ
                case SSANGBIEUP: // ㅃ
                case SSANGSIOT: // ㅆ
                case SSANGJIAEUT: // ㅉ
                    sectionChar = (char) (tempKor - 1);
                    break;
                default:
                    sectionChar = tempKor;
                    break;
            }
        }

        return sectionChar;
    }

    /**
     * 한글 초성 검색
     */
    private static final char HANGUL_BEGIN_UNICODE = 44032; // 가
    private static final char HANGUL_LAST_UNICODE = 55203; // 힣
    private static final char HANGUL_BASE_UNIT = 588;// 각자음 마다 가지는 글자수
    // 자음
    private static final char[] INITIAL_SOUND = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    /**
     * 해당 문자가 INITIAL_SOUND인지 검사.
     *
     * @param searchar
     * @return
     */
    private static boolean isInitialSound(char searchar) {
        for (char c : INITIAL_SOUND) {
            if (c == searchar) {
                return true;
            }
        }
        return false;
    }

    /**
     * 해당 문자의 자음을 얻는다.
     *
     * @param c 검사할 문자
     * @return
     */
    private static char getInitialSound(char c) {
        int hanBegin = (c - HANGUL_BEGIN_UNICODE);
        int index = hanBegin / HANGUL_BASE_UNIT;
        return INITIAL_SOUND[index];
    }

    /**
     * 해당 문자가 한글인지 검사
     *
     * @param c 문자 하나
     * @return
     */
    private static boolean isHangul(char c) {
        return HANGUL_BEGIN_UNICODE <= c && c <= HANGUL_LAST_UNICODE;
    }

    public static boolean matchString(String value, String search) {
        int t = 0;
        int seof = value.length() - search.length();
        int slen = search.length();
        if (seof < 0)
            return false; // 검색어가 더 길면 false를 리턴한다.
        for (int i = 0; i <= seof; i++) {
            t = 0;
            while (t < slen) {
                if (isInitialSound(search.charAt(t)) == true && isHangul(value.charAt(i + t))) {
                    // 만약 현재 char이 초성이고 value가 한글이면
                    if (getInitialSound(value.charAt(i + t)) == search.charAt(t))
                        // 각각의 초성끼리 같은지 비교한다
                        t++;
                    else
                        break;
                } else {
                    // char이 초성이 아니라면
                    if (value.charAt(i + t) == search.charAt(t))
                        // 그냥 같은지 비교한다.
                        t++;
                    else
                        break;
                }
            }
            if (t == slen)
                return true; // 모두 일치한 결과를 찾으면 true를 리턴한다.
        }
        return false; // 일치하는 것을 찾지 못했으면 false를 리턴한다.
    }



}
