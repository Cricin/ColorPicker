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
import android.graphics.Shader;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BarPicker extends View {
  private static final int DEFAULT_BAR_HEIGHT = 6;//dp
  private static final int[] ANDROID_ORIENTATION_ATTR = {android.R.attr.orientation};
  private static final int DEFAULT_BAR_LENGTH = 500;//px

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;

  @IntDef({HORIZONTAL, VERTICAL})
  @Retention(RetentionPolicy.SOURCE)
  public @interface OrientationMode {}

  private Paint mPaint;
  private RectF mBarRectF;
  private ThumbDrawable mThumb;
  private int mBarHeight;//颜色条的高度
  private float mPercent = 0F;
  @OrientationMode
  private int mOrientation;

  protected int mValue;
  protected OnValueChangeListener mListener;

  public BarPicker(Context context) {
    this(context, null);
  }

  public BarPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.mThumb = new ThumbDrawable(context);
    this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mBarRectF = new RectF();

    int defBarHeight = (int) (context.getResources().getDisplayMetrics().density * DEFAULT_BAR_HEIGHT + 0.5);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BarPicker);
    this.mBarHeight = (int) a.getDimension(R.styleable.BarPicker_bar_height, defBarHeight);
    a.recycle();

    a = context.obtainStyledAttributes(attrs, ANDROID_ORIENTATION_ATTR);
    this.mOrientation = a.getInt(0, HORIZONTAL);
    a.recycle();
  }

  protected abstract void onChanged(@FloatRange(from = 0F, to = 1F) float percent);

  protected abstract int[] getGradientColors();

  protected RectF getBarRectF() {
    return mBarRectF;
  }

  protected void invalidateGradient() {
    final int w = getWidth();
    final int h = getHeight();
    final int colors[] = getGradientColors();
    Shader shader;
    if (mOrientation == HORIZONTAL) {
      shader = new LinearGradient(0, 0, w, 0,
        colors, null, Shader.TileMode.CLAMP);
    } else {
      shader = new LinearGradient(0, 0, 0, h,
        colors, null, Shader.TileMode.CLAMP);
    }
    mPaint.setShader(shader);
    invalidate();
  }

  protected void moveThumb(@FloatRange(from = 0F, to = 1F) float percent) {
    this.mPercent = percent;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int w, h;
    if (mOrientation == VERTICAL) {
      if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {//if we are scrolling view's child
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_BAR_LENGTH, MeasureSpec.EXACTLY);
      }
      w = resolveSize(Math.max(mThumb.getIntrinsicWidth(), mBarHeight), widthMeasureSpec);
      h = resolveSize(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec);
    } else {
      if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {//if we are scrolling view's child
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_BAR_LENGTH, MeasureSpec.EXACTLY);
      }
      w = resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec);
      h = resolveSize(Math.max(mThumb.getIntrinsicHeight(), mBarHeight), heightMeasureSpec);
    }
    setMeasuredDimension(w, h);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    //draw bar
    final float radius = Math.min(mBarRectF.width(), mBarRectF.height()) / 2;
    canvas.drawRoundRect(mBarRectF, radius, radius, mPaint);

    //draw thumb
    final int thumbWidth = mThumb.getIntrinsicWidth();
    final int thumbHeight = mThumb.getIntrinsicHeight();
    int thumbTop;
    int thumbLeft;
    if (mOrientation == LinearLayout.VERTICAL) {
      thumbTop = (int) (mPercent * mBarRectF.height());
      thumbLeft = (getWidth() - thumbWidth) / 2;
    } else {
      thumbTop = (getHeight() - thumbHeight) / 2;
      thumbLeft = (int) (mPercent * mBarRectF.width());
    }
    mThumb.setBounds(thumbLeft, thumbTop, thumbLeft + thumbWidth, thumbTop + thumbHeight);
    mThumb.draw(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    invalidateGradient();
    if (mOrientation == VERTICAL) {
      final int barLeft = (w - mBarHeight) / 2;
      final int thumbHalfH = mThumb.getIntrinsicWidth() / 2;
      mBarRectF.set(barLeft, thumbHalfH, barLeft + mBarHeight, h - thumbHalfH);
    } else {
      final int barTop = (h - mBarHeight) / 2;
      final int thumbHalfW = mThumb.getIntrinsicWidth() / 2;
      mBarRectF.set(thumbHalfW, barTop, w - thumbHalfW, barTop + mBarHeight);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    if (mThumb.isStateful()) {
      boolean changed = mThumb.setState(getDrawableState());
      if (changed) {
        invalidate();
      }
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
        setPressed(true);
        break;
      case MotionEvent.ACTION_MOVE:
        getParent().requestDisallowInterceptTouchEvent(true);
        int i;
        if (mOrientation == VERTICAL) {
          i = lerp((int) mBarRectF.height(), 0, y);
          mPercent = i / mBarRectF.height();
        } else {
          i = lerp((int) mBarRectF.width(), 0, x);
          mPercent = i / mBarRectF.width();
        }
        onChanged(mPercent);
        invalidate();
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        setPressed(false);
        invalidate();
    }
    return true;
  }

  public void setOnValueChangeListener(OnValueChangeListener listener) {
    this.mListener = listener;
  }

  private static int lerp(int max, int min, int value) {
    return Math.max(min, Math.min(max, value));
  }

}
