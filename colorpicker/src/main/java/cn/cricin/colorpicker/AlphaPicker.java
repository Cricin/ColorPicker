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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * A horizontal Picker For alpha value[0, 255].
 */
public class AlphaPicker extends BarPicker {

  @ColorInt
  private int mPreviewColor = Color.RED;
  private Paint mBgPaint;

  public AlphaPicker(Context context) {
    super(context);
  }

  public AlphaPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    super.mValue = 0xFF;
    initBgPaint();
  }

  void initBgPaint() {
    Bitmap temp = Bitmap.createBitmap(12, 12, Bitmap.Config.RGB_565);
    Canvas canvas = new Canvas(temp);
    Paint paint = new Paint();
    paint.setColor(0xFF888888);
    canvas.drawRect(0, 0, 6, 6, paint);
    canvas.drawRect(6, 6, 12, 12, paint);

    paint.setColor(0xFFDDDDDD);
    canvas.drawRect(6, 0, 12, 6, paint);
    canvas.drawRect(0, 6, 6, 12, paint);
    Bitmap bg = Bitmap.createBitmap(temp);
    mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    BitmapShader bs = new BitmapShader(bg, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    mBgPaint.setShader(bs);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    final RectF barRectF = getBarRectF();
    final float radius = Math.min(barRectF.width(), barRectF.height()) / 2;
    canvas.drawRoundRect(barRectF, radius, radius, mBgPaint);
    super.onDraw(canvas);
  }

  @Override
  protected void onChanged(float percent) {
    int newAlpha = 255 - (int) (percent * 255 + 0.5);
    if (newAlpha != super.mValue) {
      super.mValue = newAlpha;
      if (super.mListener != null) super.mListener.onValueChanged(this, newAlpha);
    }
  }

  @Override
  protected int[] getGradientColors() {
    @ColorInt final int startColor = Util.setAlpha(mPreviewColor, 0xFF);
    @ColorInt final int endColor = Util.setAlpha(mPreviewColor, 0);
    return new int[]{startColor, endColor};
  }

  /**
   * Set a color to preview
   */
  public void setPreviewColor(@ColorInt int color) {
    this.mPreviewColor = color;
    super.invalidateGradient();
  }

  /**
   * Set current alpha value
   */
  public void setAlphaInt(@IntRange(from = 0, to = 255) int newAlpha) {
    super.mValue = newAlpha & 0xFF;
    super.moveThumb(1- super.mValue / (float) 255);
  }

  /**
   * Get alpha in this AlphaPicker
   */
  @IntRange(from = 0, to = 255)
  public int getAlphaInt() {
    return super.mValue;
  }

}
