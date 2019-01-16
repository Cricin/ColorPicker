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
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class GrayPicker extends HorizontalPicker {

  public GrayPicker(Context context) {
    super(context);
  }

  public GrayPicker(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected LinearGradient createGradient(int width, int height) {
    return new LinearGradient(0, height / 2, width, height / 2,
      0XFFFFFFFF, 0XFF000000, Shader.TileMode.CLAMP);
  }

  @Override
  protected void onChanged(float percent) {
    mValue = (int) (percent * 255 + 0.5f);
    if (mListener != null) {
      mListener.onValueChanged(this, Color.rgb(mValue, mValue, mValue));
    }
  }

  public void setGrayColor(@IntRange(from = 0, to = 255) int grayColor) {
    int safe = grayColor & 0x000000FF;
    this.mValue = safe;
    moveThumb(safe / (float) 255);
  }

  @ColorInt
  public int getColor() {
    return Color.rgb(mValue, mValue, mValue);
  }

}
