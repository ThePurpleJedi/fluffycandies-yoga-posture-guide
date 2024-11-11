package com.google.mlkit.vision.demo.java;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/** Demo app chooser which allows you pick from all available testing Activities. */
public final class ChooserActivity extends AppCompatActivity
    implements AdapterView.OnItemClickListener {
  private static final String TAG = "ChooserActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    startActivity(new Intent(this, CameraXLivePreviewActivity.class));
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    startActivity(new Intent(this, CameraXLivePreviewActivity.class));
  }
}
