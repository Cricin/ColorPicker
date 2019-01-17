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
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.SweepGradient;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CircleColorPicker extends View {
  private static final int[] COLORS = {
    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
    Color.BLUE, Color.MAGENTA, Color.RED
  };

  private static final int DEFAULT_CIRCLE_RADIUS = 160;//dp

  private Paint mPaint;

  private ThumbDrawable mThumb;
  private int mThumbRadius;

  //first element is also the degree, X coordinate is left to right, Y coordinate is top to bottom
  private float[] mColorHSV = {0.0F, 1.0F, 1.0F};
  private OnValueChangeListener mListener;

  private int mRadius;//radius of circle
  private int mDistance;

  public CircleColorPicker(Context context) {
    this(context, null);
  }

  public CircleColorPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    final int desiredRadius = (int) (context.getResources().getDisplayMetrics().density * DEFAULT_CIRCLE_RADIUS + 0.5);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleColorPicker);
    mRadius = (int) a.getDimension(R.styleable.CircleColorPicker_radius, desiredRadius);
    a.recycle();
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mThumb = new ThumbDrawable(context);
    mThumbRadius = mThumb.getIntrinsicWidth() / 2;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int mySize = mThumbRadius * 2 + mRadius;
    setMeasuredDimension(resolveSize(mySize, widthMeasureSpec),
      resolveSize(mySize, heightMeasureSpec));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    //draw gradient circle
    final int viewCenterX = getWidth() / 2;
    final int viewCenterY = getHeight() / 2;
    final int radius = Math.min(viewCenterX, viewCenterY) - mThumbRadius;
    canvas.drawCircle(viewCenterX, viewCenterY, radius, mPaint);

    //draw thumb
    canvas.save();
    canvas.translate(viewCenterX, viewCenterY);
    final int len = mDistance;
    canvas.rotate(mColorHSV[0]);
    mThumb.setBounds(len - mThumbRadius, mThumbRadius, len + mThumbRadius, -mThumbRadius);
    mThumb.draw(canvas);
    canvas.restore();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    mRadius = Math.min(getWidth(), getHeight()) / 2 - mThumbRadius;
    SweepGradient sg = new SweepGradient(w / 2, h / 2, COLORS, null);
    mPaint.setShader(sg);
    mDistance = mRadius;
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
        mDistance = Math.min(mRadius, distance(x, y, getWidth() / 2, getHeight() / 2));
        mColorHSV[0] = calculateDegree(getWidth(), getHeight(), x, y);
        invalidate();
        if (mListener != null) mListener.onValueChanged(this, Color.HSVToColor(mColorHSV));
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

  /**
   * Get color in this ColorPicker, note this color is opaque(alpha channel is 0xFF).
   */
  @ColorInt
  public int getColor() {
    return Color.HSVToColor(mColorHSV);
  }

  /**
   * Set a new color to this ColorPicker, note this color will be convert to
   * opaque if the color's alpha channel is not 0xFF
   */
  public void setColor(@ColorInt int color) {
    Color.colorToHSV(color, mColorHSV);
    mColorHSV[1] = 1F;
    mColorHSV[2] = 1F;
    invalidate();
  }

  private static int distance(int x0, int y0, int x1, int y1) {
    final int xLen = x0 - x1;
    final int yLen = y0 - y1;
    return (int) Math.sqrt(xLen * xLen + yLen * yLen);
  }

  private static float calculateDegree(int w, int h, int x, int y) {
    final int centerX = w / 2;
    final int centerY = h / 2;
    double theta = Math.atan2(x - centerX, y - centerY);
    float degree = (float) (Math.toDegrees(theta) - 90);
    if (degree > 0) degree = 360 - degree;
    return Math.abs(degree);
  }
}
