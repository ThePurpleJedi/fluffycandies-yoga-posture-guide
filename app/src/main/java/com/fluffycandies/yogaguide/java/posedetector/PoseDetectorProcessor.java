/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fluffycandies.yogaguide.java.posedetector;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.common.InputImage;
import com.fluffycandies.yogaguide.GraphicOverlay;
import com.fluffycandies.yogaguide.java.VisionProcessorBase;
import com.fluffycandies.yogaguide.java.posedetector.classification.PoseClassifierProcessor;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** A processor to run pose detector. */
public class PoseDetectorProcessor
    extends VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification> {
  private static final String TAG = "PoseDetectorProcessor";

  private final PoseDetector detector;

  private final boolean visualizeZ;
  private final boolean rescaleZForVisualization;
  private final boolean runClassification;
  private final boolean isStreamMode;
  private final Context context;
  private final Executor classificationExecutor;
  private final JSONObject selectedPose;

  private PoseClassifierProcessor poseClassifierProcessor;
  /** Internal class to hold Pose and classification results. */
  protected static class PoseWithClassification {
    private final Pose pose;
    private final List<String> classificationResult;

    public PoseWithClassification(Pose pose, List<String> classificationResult) {
      this.pose = pose;
      this.classificationResult = classificationResult;
    }

    public Pose getPose() {
      return pose;
    }

    public List<String> getClassificationResult() {
      return classificationResult;
    }
  }

  public PoseDetectorProcessor(
      Context context,
      PoseDetectorOptionsBase options,
      boolean visualizeZ,
      boolean rescaleZForVisualization,
      boolean runClassification,
      boolean isStreamMode,
      JSONObject selectedPose
      ) {
    super(context);
    this.visualizeZ = visualizeZ;
    this.rescaleZForVisualization = rescaleZForVisualization;
    detector = PoseDetection.getClient(options);
    this.runClassification = runClassification;
    this.isStreamMode = isStreamMode;
    this.context = context;
    classificationExecutor = Executors.newSingleThreadExecutor();
    this.selectedPose = selectedPose;
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(InputImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<String> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose, selectedPose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(MlImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<String> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose, selectedPose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected void onSuccess(
      @NonNull PoseWithClassification poseWithClassification,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.add(
        new PoseGraphic(
            graphicOverlay,
            poseWithClassification.pose,
            visualizeZ,
            rescaleZForVisualization,
            poseWithClassification.classificationResult, selectedPose));
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Pose detection failed!", e);
  }

  @Override
  protected boolean isMlImageEnabled(Context context) {
    // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
    return true;
  }
}

/*
TODO: if inverted angles match better, then use those
 */