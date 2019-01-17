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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.cricin.colorpicker.AlphaPicker;
import cn.cricin.colorpicker.CircleColorPicker;
import cn.cricin.colorpicker.ColorPicker;
import cn.cricin.colorpicker.GrayPicker;
import cn.cricin.colorpicker.OnValueChangeListener;

public class MainActivity extends AppCompatActivity implements OnValueChangeListener {

  ViewGroup mMainContent;
  View mPreview;
  TextView mColorValue;
  TextView mAlphaValue;
  AlphaPicker mAlphaPicker;

  @IntRange(from = 0, to = 255)
  int mCurAlpha = 0xFF;
  @ColorInt
  int mCurColor = Color.RED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMainContent = findViewById(R.id.content_main);
    mPreview = findViewById(R.id.preview);
    mColorValue = findViewById(R.id.color_value);
    mAlphaValue = findViewById(R.id.alpha_value);

    CircleColorPicker circleColorPicker = findViewById(R.id.color_picker_circle);
    circleColorPicker.setOnValueChangeListener(this);

    initBarPickers();
    refreshPreviews(255, Color.RED);
  }

  void initBarPickers() {
    ColorPicker colorPicker = findViewById(R.id.color_picker);
    colorPicker.setColor(mCurColor);
    colorPicker.setOnValueChangeListener(this);

    mAlphaPicker = findViewById(R.id.alpha_picker);
    mAlphaPicker.setAlphaInt(mCurAlpha);
    mAlphaPicker.setOnValueChangeListener(this);

    GrayPicker grayPicker = findViewById(R.id.gray_picker);
    grayPicker.setOnValueChangeListener(this);
  }

  void refreshPreviews(int alpha, int color) {
    color = color & 0x00FFFFFF;
    if (color != mCurColor) {
      this.mCurColor = color;
      mAlphaPicker.setPreviewColor(color);
    }
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(R.string.switch_orientation).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    View view = findViewById(R.id.horizontal_picker_container);
    mMainContent.removeViewAt(mMainContent.getChildCount() - 1);
    if (view == null) {
      getLayoutInflater().inflate(R.layout.bar_pickers_horizontal, mMainContent);
    } else {
      getLayoutInflater().inflate(R.layout.bar_pickers_vertical, mMainContent);
    }
    initBarPickers();
    refreshPreviews(mCurAlpha, mCurColor);
    return true;
  }
}
