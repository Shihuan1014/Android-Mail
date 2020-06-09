package edu.hnu.mail.ui.mails;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import edu.hnu.mail.util.DensityUtil;

public class BackGroundSpan extends ReplacementSpan {
    private int bgColor;
    private int textColor;
    private Context context;

    public BackGroundSpan(Context context,int bgColor, int textColor) {
        super();
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.context = context;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        //设置宽度为文字宽度加16dp
        return ((int)paint.measureText(text, start, end)+ DensityUtil.pxToDp(context,16));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int originalColor = paint.getColor();
        paint.setColor(this.bgColor);
        //画圆角矩形背景
        canvas.drawRoundRect(new RectF(x,
                        top+ DensityUtil.pxToDp(context,3),
                        x + ((int) paint.measureText(text, start, end)+ DensityUtil.pxToDp(context,16)),
                        bottom-DensityUtil.pxToDp(context,1)),

                DensityUtil.pxToDp(context,4),
                DensityUtil.pxToDp(context,4),
                paint);
        paint.setColor(this.textColor);
        //画文字,两边各增加8dp
        canvas.drawText(text, start, end, x+DensityUtil.pxToDp(context,8), y, paint);
        //将paint复原
        paint.setColor(originalColor);
    }
}