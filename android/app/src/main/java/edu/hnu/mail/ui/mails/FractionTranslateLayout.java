package edu.hnu.mail.ui.mails;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by solo on 15/2/9.
 */
public class FractionTranslateLayout extends RelativeLayout {
    private int screenWidth;
    private float fractionX;
    private OnLayoutTranslateListener onLayoutTranslateListener;

    public FractionTranslateLayout(Context context) {
        super(context);
    }

    public FractionTranslateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FractionTranslateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onSizeChanged(int w, int h, int oldW, int oldH){

        // Assign the actual screen width to our class variable.
        screenWidth = w;

        super.onSizeChanged(w, h, oldW, oldH);
    }

    public float getFractionX(){
        return fractionX;
    }

    public void setFractionX(float xFraction){
        this.fractionX = xFraction;

        setX((screenWidth > 0) ? (xFraction * screenWidth) : 0);

        if (xFraction == 1 || xFraction == -1) {
            setAlpha(0);
        } else if (xFraction < 1 /* enter */|| xFraction > -1 /* exit */) {
            if (getAlpha() != 1) {
                setAlpha(1);
            }
        }

        if (onLayoutTranslateListener != null) {
            onLayoutTranslateListener.onLayoutTranslate(this, xFraction);
        }
    }

    public void setOnLayoutTranslateListener(OnLayoutTranslateListener onLayoutTranslateListener) {
        this.onLayoutTranslateListener = onLayoutTranslateListener;
    }

    public static interface OnLayoutTranslateListener {
        void onLayoutTranslate(FractionTranslateLayout view, float xFraction);
    }
}