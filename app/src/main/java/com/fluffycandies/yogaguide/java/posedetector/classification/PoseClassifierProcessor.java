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

package com.fluffycandies.yogaguide.java.posedetector.classification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.WorkerThread;
import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
    private static final String TAG = "PoseClassifierProcessor";
    private static final String POSE_SAMPLES_FILE = "pose/yoga_poses.csv";

    // Specify classes for which we want rep counting.
    // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
    // for your pose samples.
    private static final String[] POSE_CLASSES = {
            "Navasana",
            "Ardha Navasana",
            "Dhanurasana",
            "Setu Bandha Sarvangasana",
            "Baddha Konasana",
            "Ustrasana",
            "Marjaryasana",
            "Bitilasana",
            "Utkatasana",
            "Balasana",
            "Sivasana",
            "Alanasana",
            "Bakasana",
            "Ardha Pincha Mayurasana",
            "Adho Mukha Svanasana",
            "Garudasana",
            "Utthita Hasta Padangusthasana",
            "Utthita Parsvakonasana",
            "Pincha Mayurasana",
            "Uttanasana",
            "Ardha Chandrasana",
            "Adho Mukha Vrksasana",
            "Anjaneyasana",
            "Supta Kapotasana",
            "Eka Pada Rajakapotasana",
            "Phalakasana",
            "Halasana",
            "Parsvottanasana",
            "Parsva Virabhadrasana",
            "Paschimottanasana",
            "Padmasana",
            "Ardha Matsyendrasana",
            "Salamba Sarvangasana",
            "Vasisthasana",
            "Salamba Bhujangasana",
            "Hanumanasana",
            "Malasana",
            "Uttanasana",
            "Ashta Chandrasana",
            "Upavistha Konasana",
            "Vrksasana",
            "Trikonasana",
            "Urdhva Mukha Svsnssana",
            "Virabhadrasana One",
            "Virabhadrasana Two",
            "Virabhadrasana Three",
            "Urdhva Dhanurasana",
            "Camatkarasana"
    };
    public static int lastCount = 0;
    private final boolean isStreamMode;

    private EMASmoothing emaSmoothing;
    private List<CountdownTimer> countdownTimers;
    private PoseClassifier poseClassifier;
    private String lastCountResult;
    @WorkerThread
    public PoseClassifierProcessor(Context context, boolean isStreamMode) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        this.isStreamMode = isStreamMode;
        if (isStreamMode) {
            emaSmoothing = new EMASmoothing();
            countdownTimers = new ArrayList<>();
            lastCountResult = "";
        }
        loadPoseSamples(context);
    }

    private void loadPoseSamples(Context context) {
        List<PoseSample> poseSamples = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(POSE_SAMPLES_FILE)));
            String csvLine = reader.readLine();
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
                if (poseSample != null) {
                    poseSamples.add(poseSample);
                }
                csvLine = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when loading pose samples.\n" + e);
        }
        poseClassifier = new PoseClassifier(poseSamples);
        if (isStreamMode) {
            for (String className : POSE_CLASSES) {
                countdownTimers.add(new CountdownTimer(className));
            }
        }
    }

    /**
     * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
     * classification results.
     *
     * <p>Currently it returns up to 2 strings as following:
     * 0: PoseClass : X reps
     * 1: PoseClass : [0.0-1.0] confidence
     */
    @WorkerThread
    public List<String> getPoseResult(Pose pose, JSONObject selectedPose) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        List<String> result = new ArrayList<>();
        ClassificationResult classification = poseClassifier.classify(pose);

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.
        if (isStreamMode) {
            // Feed pose to smoothing even if no pose found.
            classification = emaSmoothing.getSmoothedResult(classification);

            if (pose.getAllPoseLandmarks().isEmpty()) {
                result.add(lastCountResult);
                return result;
            }

            for (CountdownTimer countdownTimer : countdownTimers) {
                String poseName = "";

                poseName = selectedPose.keys().next();

                if (!countdownTimer.getClassName().equals(poseName))
                    continue;

                int timeBefore = countdownTimer.getTimeCount();
                int timeAfter = countdownTimer.addClassificationResult(classification);
                if (timeAfter > timeBefore) {
                    // Play a fun beep when countdown updates.
                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    lastCount = timeAfter;
                    lastCountResult = String.format(
                            Locale.US, "%s : %d seconds", countdownTimer.getClassName(), timeAfter);

                    break;
                }
            }
            result.add(lastCountResult);
        }

        // Add maxConfidence class of current frame to result if pose is found.
        if (!pose.getAllPoseLandmarks().isEmpty()) {
            String maxConfidenceClass = classification.getMaxConfidenceClass();
            String maxConfidenceClassResult = String.format(
                    Locale.US,
                    "%s : %.2f confidence",
                    maxConfidenceClass,
                    classification.getClassConfidence(maxConfidenceClass)
                            / poseClassifier.confidenceRange());
            result.add(maxConfidenceClassResult);
        }

        return result;
    }

}
