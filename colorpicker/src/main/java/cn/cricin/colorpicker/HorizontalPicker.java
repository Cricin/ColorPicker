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
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public abstract class HorizontalPicker extends View {

  private static final int DEFAULT_BAR_HEIGHT = 6;//dp

  private Paint mPaint;
  private RectF mBarRectF;
  private ThumbDrawable mThumb;
  private int mTouchX;
  private int mBarHeight;//颜色条的高度
  private float mThumbPercent = 1F;

  protected int mValue;
  protected OnValueChangeListener mListener;

  public HorizontalPicker(Context context) {
    this(context, null);
  }

  public HorizontalPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.mThumb = new ThumbDrawable(context);
    this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mBarRectF = new RectF();

    int defBarHeight = (int) (context.getResources().getDisplayMetrics().density * DEFAULT_BAR_HEIGHT + 0.5);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalPicker);
    this.mBarHeight = (int) a.getDimension(R.styleable.HorizontalPicker_hp_bar_height, defBarHeight);
    a.recycle();
  }

  protected abstract LinearGradient createGradient(int width, int height);

  protected abstract void onChanged(@FloatRange(from = 0F, to = 1F) float percent);

  protected RectF getBarRectF() {
    return mBarRectF;
  }

  protected void invalidateGradient() {
    LinearGradient gradient = createGradient(getWidth(), getHeight());
    mPaint.setShader(gradient);
    invalidate();
  }

  protected void moveThumb(@FloatRange(from = 0F, to = 1F) float percent) {
    this.mThumbPercent = percent;
    this.mTouchX = (int) (percent * getWidth());
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //高度使用滑块和bar的最大值
    final int desiredHeight = Math.max(mThumb.getIntrinsicHeight(), mBarHeight);
    int height = resolveSize(desiredHeight, heightMeasureSpec);
    //宽度根据父布局定，如果是wrap_content，则宽度和父布局一样
    int width = resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    final float radius = Math.min(mBarRectF.width(), mBarRectF.height()) / 2;
    canvas.drawRoundRect(mBarRectF, radius, radius, mPaint);

    //绘制滑块
    final int thumbWidth = mThumb.getIntrinsicWidth();
    final int thumbHeight = mThumb.getIntrinsicHeight();
    final int mThumbTop = (getHeight() - thumbHeight) / 2;
    final int thumbLeft = Math.min(Math.max(0, mTouchX - thumbWidth / 2), getWidth() - thumbWidth);
    mThumb.setBounds(thumbLeft, mThumbTop, thumbLeft + thumbWidth, mThumbTop + thumbHeight);
    mThumb.draw(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    LinearGradient gradient = createGradient(w, h);
    mPaint.setShader(gradient);
    final int thumbWidth = mThumb.getIntrinsicWidth();
    final int barTop = (h - mBarHeight) / 2;
    mBarRectF = new RectF(thumbWidth / 2, barTop, w - thumbWidth / 2, barTop + mBarHeight);
    mTouchX = (int) (mBarRectF.right * mThumbPercent);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    final int x = (int) event.getX();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mThumb.setPressed(true);
        break;
      case MotionEvent.ACTION_MOVE:
        mTouchX = x;
        int i = Math.max(0, x - mThumb.getIntrinsicWidth());
        i = (int) Math.min(mBarRectF.width(), i);
        onChanged(i / mBarRectF.width());
        invalidate();
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

}
