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
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class GrayPicker extends BarPicker {

  public GrayPicker(Context context) {
    super(context);
  }

  public GrayPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onChanged(float percent) {
    super.mValue = (int) (percent * 255 + 0.5f);
    if (super.mListener != null) {
      super.mListener.onValueChanged(this, Color.rgb(super.mValue, super.mValue, super.mValue));
    }
  }

  @Override
  protected int[] getGradientColors() {
    return new int[]{Color.BLACK, Color.WHITE};
  }

  /**
   * Set a new gray color, since gray color have three same value in r,g,b channel
   * so we just use the lower 8bit
   */
  public void setColor(@IntRange(from = 0, to = 255) int grayColor) {
    super.mValue = grayColor & 0xFF;
    moveThumb(super.mValue / (float) 255);
  }

  @ColorInt
  public int getColor() {
    return Color.rgb(super.mValue, super.mValue, super.mValue);
  }

}
