package com.moutamid.tweetytheclone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

//import org.jetbrains.annotations.Nullable;
//
//import kotlin.jvm.internal.Intrinsics;

public class AppCompatLineEditText extends AppCompatEditText {

    private Rect mRect;
    private Paint mPaint;
    private boolean showLines = true;

    // we need this constructor for LayoutInflater
//    public LinedEditText(Context context, AttributeSet attrs) {
//        super(context, attrs);


//        showLines = Settings.getNoteLinesOption(context);
//    }

    public AppCompatLineEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.gray));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int curHeight = 0;
        int baseline = getLineBounds(0, mRect);
        if (showLines) {
            for (curHeight = baseline + 3; curHeight < height; curHeight += getLineHeight()) {
                canvas.drawLine(mRect.left, curHeight, mRect.right, curHeight, mPaint);
            }
        }
        super.onDraw(canvas);
    }

    //--------------------------------------
//    private Rect mRect = new Rect();
//    private Paint mPaint = new Paint();
//    private Float mWidth;
//    private Integer mColor;
//    private TypedArray array;
//
//
//    public AppCompatLineEditText(@NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs) {
//        super(context, attrs);
////        Intrinsics.checkNotNull(context);
////        TypedArray var10001 = context.obtainStyledAttributes(attrs);//, R.styleable.LineEditText
////        Intrinsics.checkNotNullExpressionValue(var10001, "context!!.obtainStyledAt…R.styleable.LineEditText)");
////        this.array = var10001;
//    }
//
//    @Override
//    protected void onDraw(@Nullable Canvas canvas) {
//        ViewParent viewParent = this.getParent();
//        if (viewParent == null) {
//            throw new NullPointerException("null cannot be cast to non-null type android.view.View");
//        } else {
//            int height = ((View) viewParent).getHeight();
//            int lineHeight = this.getLineHeight();
//            int numberOfLines = height / lineHeight;
//            Rect rect = this.mRect;
//            Paint paint = this.mPaint;
//            this.mPaint.setStyle(Paint.Style.FILL);
//            Paint.Style var10;
//            switch (this.getId()) {
//                case 0:
//                    var10 = Paint.Style.FILL;
//                    break;
//                case 1:
//                    var10 = Paint.Style.STROKE;
//                    break;
//                case 2:
//                    var10 = Paint.Style.FILL_AND_STROKE;
//            }
//
//            this.mPaint.setStrokeWidth(2.0F);//this.array.getFloat(styleable.LineEditText_strokeWidth,
//            this.mPaint.setColor(-16777216);//this.array.getInt(styleable.LineEditText_lineColor,
//            int baseLine = this.getLineBounds(0, rect);
//            int i = 0;
//            int var9 = numberOfLines;
//            if (i <= numberOfLines) {
//                while (true) {
////                    Intrinsics.checkNotNull(canvas);
//                    canvas.drawLine((float) rect.left, (float) (baseLine + 1), (float) rect.right, (float) (baseLine + 1), paint);
//                    baseLine += lineHeight;
//                    if (i == var9) {
//                        break;
//                    }
//
//                    ++i;
//                }
//            }
//
//            super.onDraw(canvas);
//        }
//    }
//
//    public final void setStrok(float width) {
//        this.mWidth = width;
//        this.requestLayout();
//    }
//
//    public final void setColor(int color) {
//        this.mColor = color;
//        this.requestLayout();
//    }
//------------------------------------------
}
