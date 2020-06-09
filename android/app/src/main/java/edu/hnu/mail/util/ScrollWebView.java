package edu.hnu.mail.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.core.view.MotionEventCompat;

public class ScrollWebView extends WebView {
    private float startx;
    private float starty;
    private float offsetx;
    private float offsety;

    public ScrollWebView(Context context) {
        super(context);
    }

    public ScrollWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(MotionEventCompat.findPointerIndex(event,0) == -1) {
            return super.onTouchEvent(event);
        }
        if(event.getPointerCount() >=2) {
            requestDisallowInterceptTouchEvent(true);//多点触摸，大于两个手指的滑动我自己处理了
        }else{
            requestDisallowInterceptTouchEvent(false);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                requestDisallowInterceptTouchEvent(true);
                startx = event.getX();
                starty = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetx = Math.abs(event.getX() - startx);
                offsety = Math.abs(event.getY() - starty);
                if (offsetx > offsety) {
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

//    @Override
//    protected void onOverScrolled(int scrollX,int scrollY,boolean clampedX,boolean clampedY) {
//        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
//        if(clampedY){//当滑动到下边界时，开始
//
//        }
//        requestDisallowInterceptTouchEvent(true);
//    }
}
