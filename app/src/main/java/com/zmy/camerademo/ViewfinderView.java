package com.zmy.camerademo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zmy.camerademo.util.DisplayUtil;

/**
 * @author Lize
 * Created  on 2019/3/11
 */
public class ViewfinderView extends View {

    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final String TIP = "请将二维码置入扫描区域";
    private static final int ANIMATION_DURATION = 6000;
    /**
     * 扫描框中的中间线的宽度
     */
    private static int MIDDLE_LINE_WIDTH;
    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static int MIDDLE_LINE_PADDING;

    private Context context;
    /**
     * 蒙版画笔
     */
    private Paint maskPaint;
    private int screenWidth;
    private int screenHeight;
    private int finderLineTop;
    private Rect rect;
    private int cornerPadding;
    private Paint linePaint;
    private ValueAnimator valueAnimator;
    /**
     * 画笔对象的引用
     */
    private Paint paint;
    private Paint textTipPaint;

    public ViewfinderView(Context context) {
        super(context);
    }

    public ViewfinderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAnimation();
    }

    public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        this.context = context;
        textTipPaint = new Paint();
        textTipPaint.setColor(Color.WHITE);
        textTipPaint.setTextSize(DisplayUtil.dip2px(context, 14));
        textTipPaint.setFakeBoldText(true);

        screenWidth = DisplayUtil.getScreenWidth(context);
        screenHeight = DisplayUtil.getScreenHeight(context);
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 开启反锯齿
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(context, R.color.white));
        cornerPadding = DisplayUtil.dip2px(context, 0.0F);
        maskPaint.setColor(ContextCompat.getColor(context, R.color.mask_bg_color));
        int leftAndRightMargin = DisplayUtil.dip2px(context, 15);
        //初始化扫描框 rect
        rect = new Rect();
        rect.left = leftAndRightMargin;
        rect.right = screenWidth - leftAndRightMargin;
        rect.top = (screenHeight - rect.width()) / 2;
        rect.bottom = rect.top + DisplayUtil.dip2px(getContext(),245);

        MIDDLE_LINE_PADDING = DisplayUtil.dip2px(context, 0);
        MIDDLE_LINE_WIDTH = DisplayUtil.dip2px(context, 3.0F);
        finderLineTop = rect.top;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(screenWidth, screenHeight);
    }

    public Rect getRect() {
        return rect;
    }

    private void initAnimation() {
        valueAnimator = ValueAnimator.ofInt(rect.top, rect.bottom);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                finderLineTop = Integer.parseInt(String.valueOf(animation.getAnimatedValue()));
                // 只刷新扫描框的内容，其他地方不刷新
                postInvalidateDelayed(ANIMATION_DELAY, rect.left, rect.top, rect.right, rect.bottom);
            }
        });
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setDuration(ANIMATION_DURATION);
    }

    public void startAnimation() {
        if (valueAnimator != null && !valueAnimator.isStarted()) {
            valueAnimator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMask(canvas);
        drawableLine(canvas);
        drawRectEdges(canvas);
        drawText(canvas);
        //drawScanningLine(canvas, rect);
    }

    /**
     * 绘制扫描线
     *
     * @param frame 扫描框
     */
    private void drawScanningLine(Canvas canvas, Rect frame) {
        Rect lineRect = new Rect();
        lineRect.left = frame.left + MIDDLE_LINE_PADDING;
        lineRect.right = frame.right - MIDDLE_LINE_PADDING;
        lineRect.top = finderLineTop;
        lineRect.bottom = (finderLineTop + MIDDLE_LINE_WIDTH);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.jdme_scan_laser), null, lineRect, paint);
    }

    /**
     * 画提示文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        float stringWidth = textTipPaint.measureText(TIP);
        float x = (getWidth() - stringWidth) / 2;
        canvas.drawText(TIP, x, rect.bottom + DisplayUtil.dip2px(context, 30), textTipPaint);
    }

    /**
     * 画边框的线
     *
     * @param canvas
     */
    private void drawableLine(Canvas canvas) {
        linePaint.setColor(ContextCompat.getColor(getContext(),R.color.theme_color));
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, linePaint);
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, linePaint);
        canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, linePaint);
        canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, linePaint);
    }

    /**
     * 画四个角
     *
     * @param canvas
     */
    private void drawRectEdges(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setAlpha(OPAQUE);

        Resources resources = getResources();
        // 这些资源可以用缓存进行管理，不需要每次刷新都新建
        Bitmap bitmapCornerTopLeft = BitmapFactory.decodeResource(resources, R.mipmap.jdme_scan_corner_top_left);
        Bitmap bitmapCornerTopRight = BitmapFactory.decodeResource(resources, R.mipmap.jdme_scan_corner_top_right);
        Bitmap bitmapCornerBottomLeft = BitmapFactory.decodeResource(resources, R.mipmap.jdme_scan_corner_bottom_left);
        Bitmap bitmapCornerBottomRight = BitmapFactory.decodeResource(resources, R.mipmap.jdme_scan_corner_bottom_right);
        canvas.drawBitmap(bitmapCornerTopLeft, rect.left - cornerPadding, rect.top - cornerPadding, paint);
        canvas.drawBitmap(bitmapCornerTopRight, rect.right + cornerPadding - bitmapCornerTopRight.getWidth(), rect.top - cornerPadding, paint);
        canvas.drawBitmap(bitmapCornerBottomLeft, rect.left - cornerPadding, 2 + (rect.bottom + cornerPadding - bitmapCornerBottomLeft.getHeight()), paint);
        canvas.drawBitmap(bitmapCornerBottomRight, rect.right + cornerPadding - bitmapCornerBottomRight.getWidth(),
                2 + (rect.bottom + cornerPadding - bitmapCornerBottomRight.getHeight()), paint);
        bitmapCornerTopLeft.recycle();
        bitmapCornerTopRight.recycle();
        bitmapCornerBottomLeft.recycle();
        bitmapCornerBottomRight.recycle();
    }

    /**
     * 画背景
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0, 0, width, rect.top, maskPaint);
        canvas.drawRect(0, rect.top, rect.left, rect.bottom, maskPaint);
        canvas.drawRect(rect.right, rect.top, width, rect.bottom, maskPaint);
        canvas.drawRect(0, rect.bottom, width, height, maskPaint);
    }
}
