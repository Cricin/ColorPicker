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

package cn.cricin.colorpicker.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import cn.cricin.colorpicker.AlphaPicker;
import cn.cricin.colorpicker.ColorPicker;
import cn.cricin.colorpicker.GrayPicker;
import cn.cricin.colorpicker.OnValueChangeListener;

public class MainActivity extends AppCompatActivity implements OnValueChangeListener {

  View mPreview;
  TextView mColorValue;
  TextView mAlphaValue;

  @IntRange(from = 0, to = 255)
  int mCurAlpha = 0xFF;
  @ColorInt
  int mCurColor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mPreview = findViewById(R.id.preview);
    mColorValue = findViewById(R.id.color_value);
    mAlphaValue = findViewById(R.id.alpha_value);

    refreshPreviews(255, Color.RED);

    ColorPicker colorPicker = findViewById(R.id.color_picker_circle);
    colorPicker.setOnValueChangeListener(this);

    colorPicker = findViewById(R.id.color_picker_rectangle);
    colorPicker.setOnValueChangeListener(this);

    AlphaPicker alphaPicker = findViewById(R.id.alpha_picker);
    alphaPicker.setOnValueChangeListener(this);

    GrayPicker picker = findViewById(R.id.gray_picker);
    picker.setOnValueChangeListener(this);
  }

  void refreshPreviews(int alpha, int color) {
    this.mCurColor = color & 0x00FFFFFF;
    this.mCurAlpha = alpha;
    mPreview.setBackgroundColor((alpha << 24) | color);
    String string = getString(R.string.color_value, mCurColor);
    mColorValue.setText(string);
    string = getString(R.string.alpha_value, alpha);
    mAlphaValue.setText(string);
  }

  @Override
  public void onValueChanged(View view, int newValue) {
    if (view instanceof AlphaPicker) {
      refreshPreviews(newValue, mCurColor);
    } else {
      refreshPreviews(mCurAlpha, newValue);
    }
  }
}
