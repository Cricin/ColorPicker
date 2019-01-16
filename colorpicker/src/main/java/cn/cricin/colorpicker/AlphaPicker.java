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
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class AlphaPicker extends HorizontalPicker {

  @ColorInt
  private int mPreviewColor = 0xFFFF0000;
  private Paint mBgPaint;

  public AlphaPicker(Context context) {
    super(context);
  }

  public AlphaPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
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
  protected LinearGradient createGradient(int width, int height) {
    @ColorInt final int endColor = 0XFF000000 | mPreviewColor;
    @ColorInt final int startColor = 0x00FFFFFF & mPreviewColor;
    return new LinearGradient(0, height / 2, width, height / 2,
      startColor, endColor, Shader.TileMode.CLAMP);
  }

  @Override
  protected void onChanged(float percent) {
    int newAlpha = (int) (percent * 255 + 0.5);
    if (newAlpha != mValue) {
      this.mValue = newAlpha;
      if (mListener != null) mListener.onValueChanged(this, newAlpha);
    }
  }

  public void setPreviewColor(@ColorInt int color) {
    this.mPreviewColor = color;
    invalidateGradient();
  }

  public void setAlpha(@IntRange(from = 0, to = 255) int newAlpha) {
    this.mValue = newAlpha;
    moveThumb(newAlpha / (float) 255);
  }

}
