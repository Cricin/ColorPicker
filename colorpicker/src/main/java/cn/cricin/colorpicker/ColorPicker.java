/*
 * Copyright (C) 2019 Cricin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.cricin.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {
  private static final int[] COLORS = {
    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
    Color.BLUE, Color.MAGENTA, Color.RED
  };


  private static final int DEFAULT_BAR_HEIGHT = 5;//dp
  private static final int DEFAULT_CIRCLE_WIDTH = 320;//dp

  private RectF mBarRectF;//颜色条位置RectF
  private Paint mPaint;

  private ThumbDrawable mThumb;//滑块
  private int mThumbHalfWidth;//滑块宽度的一半
  private int mThumbHalfHeight;//滑块高度的一半

  private boolean mHorizontal = true;//是否是水平选择器
  private Point mTouchPoint;//当前点击的位置
  private float[] mColorHSV = {0.0F, 1.0F, 1.0F};
  private OnValueChangeListener mListener;

  //圆形选择器的属性
  private float mDegree;//和view中心点对角度
  private int mRadius;//圆形选择器的半径

  //横向选择器的属性
  private int mBarHeight;//颜色条的高度

  public ColorPicker(Context context) {
    this(context, null);
  }

  public ColorPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPicker);
    mHorizontal = a.getInt(R.styleable.ColorPicker_picker_style, 0) == 0;
    mBarHeight = (int) a.getDimension(R.styleable.ColorPicker_bar_height, dp2px(DEFAULT_BAR_HEIGHT));
    a.recycle();
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mThumb = new ThumbDrawable(context);
    mThumbHalfWidth = mThumb.getIntrinsicWidth() / 2;
    mThumbHalfHeight = mThumb.getIntrinsicHeight() / 2;
    mTouchPoint = new Point();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mHorizontal) {
      //高度使用滑块和bar的最大值
      final int desiredHeight = Math.max(mThumbHalfHeight * 2, mBarHeight);
      int height = resolveSize(desiredHeight, heightMeasureSpec);
      //宽度根据父布局定，如果是wrap_content，则宽度和父布局一样
      int width = resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec);
      setMeasuredDimension(width, height);
    } else {
      //如果是wrap_content并且父布局足够大，就使用desireRadius
      final int desiredRadius = dp2px(DEFAULT_CIRCLE_WIDTH);
      setMeasuredDimension(resolveSize(desiredRadius, widthMeasureSpec),
        resolveSize(desiredRadius, heightMeasureSpec));
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mHorizontal) {
      final int barRadius = mBarHeight / 2;
      canvas.drawRoundRect(mBarRectF, barRadius, barRadius, mPaint);
      int mThumbTop = getHeight() / 2 - mThumbHalfHeight;
      final int thumbLeft = Math.min(Math.max(0, mTouchPoint.x - mThumbHalfWidth), getWidth() - mThumbHalfWidth * 2);
      mThumb.setBounds(thumbLeft, mThumbTop, thumbLeft + mThumbHalfWidth * 2, mThumbTop + mThumbHalfHeight * 2);
      mThumb.draw(canvas);
    } else {
      //先画颜色选择圆盘
      final int viewCenterX = getWidth() / 2;
      final int viewCenterY = getHeight() / 2;
      final int radius = Math.min(viewCenterX, viewCenterY) - mThumbHalfWidth;
      canvas.drawCircle(viewCenterX, viewCenterY, radius, mPaint);

      canvas.save();
      canvas.translate(viewCenterX, viewCenterY);

      final int pointToCenterX = mTouchPoint.x - viewCenterX;
      final int pointToCenterY = mTouchPoint.y - viewCenterY;
      final int len = (int) Math.min(Math.sqrt(powerOfTwo(pointToCenterX) + powerOfTwo(pointToCenterY)), mRadius);
      canvas.rotate(mDegree);
      mThumb.setBounds(len - mThumbHalfWidth, mThumbHalfHeight, len + mThumbHalfWidth, -mThumbHalfHeight);
      mThumb.draw(canvas);
      canvas.restore();
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (mHorizontal) {
      int top = (h - mBarHeight) / 2;
      int bottom = h - (h - mBarHeight) / 2;
      mBarRectF = new RectF(mThumbHalfWidth, top, w - mThumbHalfWidth, bottom);
      LinearGradient lg = new LinearGradient(
        mBarRectF.left,
        mBarRectF.top,
        mBarRectF.right,
        mBarRectF.bottom,
        COLORS,
        null,
        Shader.TileMode.CLAMP);
      mPaint.setShader(lg);
      mTouchPoint.set((int) mBarRectF.left, h / 2);
    } else {
      mRadius = Math.min(getWidth(), getHeight()) / 2 - mThumbHalfWidth;
      SweepGradient sg = new SweepGradient(w / 2, h / 2, COLORS, null);
      mPaint.setShader(sg);
      mTouchPoint.set(w - mThumbHalfWidth, h / 2);
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    final int x = (int) event.getX();
    final int y = (int) event.getY();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mThumb.setPressed(true);
        break;
      case MotionEvent.ACTION_MOVE:
        mTouchPoint.set(x, y);
        if (mHorizontal) {
          mColorHSV[0] = (x - mThumbHalfWidth) / mBarRectF.width() * 360.0F;
        } else {
          final int centerX = getWidth() / 2;
          final int centerY = getHeight() / 2;
          double theta = Math.atan2(x - centerX, y - centerY);
          mDegree = (float) (Math.toDegrees(theta) - 90);
          if (mDegree > 0) mDegree = 360 - mDegree;
          mColorHSV[0] = mDegree = Math.abs(mDegree);
        }
        invalidate();
        if (mListener != null) mListener.onValueChanged(this, Color.HSVToColor(mColorHSV));
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        mThumb.setPressed(false);
        invalidate();
    }
    return true;
  }

  public void setOnValueChangeListener(OnValueChangeListener listener) {
    this.mListener = listener;
  }

  @ColorInt
  public int getColor() {
    return Color.HSVToColor(mColorHSV);
  }


  //utility methods
  private int dp2px(int dpValue) {
    return (int) (getContext().getResources().getDisplayMetrics().density * dpValue + 0.5);
  }

  private int powerOfTwo(int value) {
    return value * value;
  }
}
