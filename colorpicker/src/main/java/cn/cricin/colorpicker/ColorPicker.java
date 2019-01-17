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
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class ColorPicker extends BarPicker {
  private static final int[] COLORS = {
    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
    Color.BLUE, Color.MAGENTA, Color.RED
  };

  private float[] mColorHSV = {0F, 1.0F, 1.0F};

  public ColorPicker(Context context) {
    this(context, null);
  }

  public ColorPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    super.mValue = Color.RED;
  }

  /**
   * Set a new color to this ColorPicker, note this color will be convert to
   * opaque if the color's alpha channel is not 0xFF
   */
  public void setColor(@ColorInt int color) {
    if (super.mValue != color) {
      super.mValue = Util.setAlpha(color, 0xFF);
      Color.colorToHSV(color, this.mColorHSV);
      this.mColorHSV[1] = 1F;
      this.mColorHSV[2] = 1F;
      super.moveThumb(mColorHSV[0] / 360);
    }
  }

  /**
   * Get color in this ColorPicker, note this color is opaque(alpha channel is 0xFF).
   */
  @ColorInt
  public int getColor() {
    return super.mValue;
  }

  @Override
  protected void onChanged(float percent) {
    this.mColorHSV[0] = (float) (percent * 360 + 0.5);
    final int color = Color.HSVToColor(this.mColorHSV);
    if (super.mValue != color) {
      super.mValue = color;
      if (super.mListener != null) super.mListener.onValueChanged(this, color);
    }
  }

  @Override
  protected int[] getGradientColors() {
    return COLORS;
  }
}
