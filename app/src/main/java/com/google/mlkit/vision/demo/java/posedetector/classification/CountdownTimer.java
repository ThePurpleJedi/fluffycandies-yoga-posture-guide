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

package com.google.mlkit.vision.demo.java.posedetector.classification;

import android.util.Log;

/**
 * Counts reps for the give class.
 */
public class CountdownTimer {
  // These thresholds can be tuned in conjunction with the Top K values in {@link PoseClassifier}.
  // The default Top K value is 10 so the range here is [0-10].
  private static final float DEFAULT_THRESHOLD = 6f; // EXIT THRESHOLD was 4f

  private final String className;
  private final float threshold;

  private int timeCount;

  public CountdownTimer(String className) {
    this(className, DEFAULT_THRESHOLD);
  }

  public CountdownTimer(String className, float enterThreshold) {
    this.className = className;
    this.threshold = enterThreshold;
    timeCount = 0;
  }

  /**
   * Adds a new Pose classification result and updates reps for given class.
   *
   * @param classificationResult {link ClassificationResult} of class to confidence values.
   * @return number of reps.
   */
  public int addClassificationResult(ClassificationResult classificationResult) {
    float poseConfidence = classificationResult.getClassConfidence(className);

    if (poseConfidence > threshold) {
      timeCount++;
    }

    Log.d("Countdown Timer", "countdown = " + timeCount);

    return timeCount;
  }

  public String getClassName() {
    return className;
  }

  public int getTimeCount() {
    return timeCount;
  }
}