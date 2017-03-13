package cn.fcbayern.android.common;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by chenzhan on 15/6/11.
 */
public class ContentTextView extends TextView {

    private String contentText;

    private final static String dots = "..";

    public ContentTextView(Context context) {
        super(context);
    }

    public ContentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContentTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setContentText(String str) {
        contentText = str;
        setText(str);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //super.onLayout(changed, left, top, right, bottom);
        if (getPaint() != null && getLineCount() >= 2) {
            Rect rect = new Rect();
            getLineBounds(0, rect);
            int line1W = rect.width();
            int line2W = rect.width() - (int) getPaint().measureText("  HH:MM");
            String result = getMaxTextLength(getPaint(), line1W, contentText, false);
            String result2 = getMaxTextLength(getPaint(), line2W, contentText.substring(result.length()), true);
            result = result + "\n" + result2;
            if (result.length() != getText().length()) {
                setText(result);
            }
        }
    }

    private static String getMaxTextLength(TextPaint paint, int dstWidth, String text, boolean needEnd) {

        TextPaint testPaint = new TextPaint();
        testPaint.set(paint);
        float allwidth = testPaint.measureText(text);
        if(allwidth <= dstWidth) {
            return text;
        }
        String end = needEnd ? dots : "";
        float endWidth = testPaint.measureText(end);
        int targetWidth = (int) (dstWidth - endWidth);
        if (allwidth <= targetWidth) {
            return text;
        }
        int hi_size;
        int lo_size;
        int subStringTempSize = (int) ((text.length() * targetWidth) / allwidth);
        float tempwidth = testPaint.measureText(text.substring(0, subStringTempSize));
        int resultsize = 0;
        if (tempwidth < targetWidth) {
            lo_size = subStringTempSize;
            hi_size = text.length();
            while ((hi_size - lo_size) > 1) {
                lo_size += 1;
                float tempsubstringwidth = testPaint.measureText(text.substring(0, lo_size));
                if (tempsubstringwidth > targetWidth) {
                    break;
                }
            }
            resultsize = lo_size - 1;
            if (resultsize <= 0) {
                resultsize = 1;
            }
        } else {
            lo_size = 0;
            hi_size = subStringTempSize;
            while ((hi_size - lo_size) > 1) {
                hi_size -= 1;
                float tempsubstringwidth = testPaint.measureText(text.substring(0, hi_size));
                if (tempsubstringwidth <= targetWidth) {
                    break;
                }
            }
            resultsize = hi_size;
            if (resultsize <= 0) {
                resultsize = 1;
            }
        }
        String res = text.substring(0, resultsize);
        res += end;
        return res;
    }
}
