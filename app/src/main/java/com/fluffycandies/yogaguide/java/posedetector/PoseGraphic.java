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

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.mlkit.vision.common.PointF3D;
import com.fluffycandies.yogaguide.GraphicOverlay;
import com.fluffycandies.yogaguide.GraphicOverlay.Graphic;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/** Draw the detected pose in preview. */
public class PoseGraphic extends Graphic {

    private static final float DOT_RADIUS = 8.0f;
    private static final float STROKE_WIDTH = 10.0f;
    private static final float POSE_CLASSIFICATION_TEXT_SIZE = 60.0f;
    private static final float ANGLE_THRESHOLD = 25.0f;

    private final Pose pose;
    private final boolean visualizeZ;
    private final boolean rescaleZForVisualization;
    private float zMin = Float.MAX_VALUE;
    private float zMax = Float.MIN_VALUE;

    private final List<String> poseClassification;
    private final Paint classificationTextPaint;
    private final Paint leftPaint;
    private final Paint rightPaint;
    private final Paint whitePaint;
    private final Paint redPaint;
    private final Paint bluePaint;
    private String lastPoseTime;
    private JSONObject poseAnglesObject;
    PoseGraphic(
            GraphicOverlay overlay,
            Pose pose,
            boolean visualizeZ,
            boolean rescaleZForVisualization,
            List<String> poseClassification,
            JSONObject selectedPoseAngles) {
        super(overlay);
        this.pose = pose;
        this.visualizeZ = visualizeZ;
        this.rescaleZForVisualization = rescaleZForVisualization;
        this.poseAnglesObject = selectedPoseAngles;

        this.poseClassification = poseClassification;
        classificationTextPaint = new Paint();
        classificationTextPaint.setColor(Color.WHITE);
        classificationTextPaint.setTextSize(POSE_CLASSIFICATION_TEXT_SIZE);
        classificationTextPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(STROKE_WIDTH);
        whitePaint.setColor(Color.WHITE);
        leftPaint = new Paint();
        leftPaint.setStrokeWidth(STROKE_WIDTH);
        leftPaint.setColor(Color.GREEN);
        rightPaint = new Paint();
        rightPaint.setStrokeWidth(STROKE_WIDTH);
        rightPaint.setColor(Color.YELLOW);

        redPaint = new Paint();
        redPaint.setStrokeWidth(STROKE_WIDTH);
        redPaint.setColor(Color.RED);
        bluePaint = new Paint();
        bluePaint.setStrokeWidth(STROKE_WIDTH);
        bluePaint.setColor(Color.BLUE);
    }
    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> pose_landmarks = pose.getAllPoseLandmarks();
        if (pose_landmarks.isEmpty()) {
            return;
        }

        List<PoseLandmark> landmarks = List.copyOf(pose_landmarks.subList(PoseLandmark.RIGHT_MOUTH + 1, pose_landmarks.size()));

        float classificationX = POSE_CLASSIFICATION_TEXT_SIZE * 0.5f;
        for (int i = 0; i < poseClassification.size(); i++) {
            float classificationY =
                    (canvas.getHeight()
                            - POSE_CLASSIFICATION_TEXT_SIZE * 1.5f * (poseClassification.size() - i));
            canvas.drawText(
                    poseClassification.get(i), classificationX, classificationY, classificationTextPaint);
        }

        for (PoseLandmark landmark : landmarks) {
            int type = landmark.getLandmarkType();
            if ((type >= 17 && type <= 22) || (type >= 29 && type <= 32))
                continue;

            drawPoint(canvas, landmark, whitePaint);
            if (visualizeZ && rescaleZForVisualization) {
                zMin = min(zMin, landmark.getPosition3D().getZ());
                zMax = max(zMax, landmark.getPosition3D().getZ());
            }
        }

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        try {
            String name = poseAnglesObject.keys().next();
            JSONObject angleJSON = poseAnglesObject.getJSONObject(name);
            PoseAngles idealAngles = new PoseAngles(angleJSON);

            PoseAngles measuredAngles = new PoseAngles(
                    getJointAngle(leftElbow, leftShoulder, leftHip),
                    getJointAngle(rightElbow, rightShoulder, rightHip),
                    getJointAngle(leftShoulder , leftElbow, leftWrist),
                    getJointAngle(rightShoulder, rightElbow, rightWrist),
                    getJointAngle(leftShoulder, leftHip, leftKnee),
                    getJointAngle(rightShoulder, rightHip, rightKnee),
                    getJointAngle(leftHip, leftKnee, leftAnkle),
                    getJointAngle(rightHip, rightKnee, leftAnkle)
            );

            float[] comparison = idealAngles.compareTo(measuredAngles);
            PoseLandmark[] joints = {leftShoulder, rightShoulder, leftElbow, rightElbow, leftHip, rightHip, leftAnkle, rightAnkle};

            String lastPoseDetected = poseClassification.get(0).split(" : ")[0];
            String currentPose = poseClassification.get(1).split(" : ")[0];

            for (int i = 0; i < 8; i ++) {
                if (abs(comparison[i]) > ANGLE_THRESHOLD && lastPoseDetected.equals(currentPose)) {
                    Log.d("PoseGraphic", "Joint " + i + " is out of sync by " + abs(comparison[i]) + " degrees");
                    drawCircle(canvas, joints[i], redPaint);
                } else {
                    drawPoint(canvas, joints[i], bluePaint);
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }

        drawLine(canvas, leftShoulder, rightShoulder, whitePaint);
        drawLine(canvas, leftHip, rightHip, whitePaint);

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint);
        drawLine(canvas, leftElbow, leftWrist, leftPaint);
        drawLine(canvas, leftShoulder, leftHip, leftPaint);
        drawLine(canvas, leftHip, leftKnee, leftPaint);
        drawLine(canvas, leftKnee, leftAnkle, leftPaint);

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint);
        drawLine(canvas, rightElbow, rightWrist, rightPaint);
        drawLine(canvas, rightShoulder, rightHip, rightPaint);
        drawLine(canvas, rightHip, rightKnee, rightPaint);
        drawLine(canvas, rightKnee, rightAnkle, rightPaint);
    }

    private float getJointAngle(PoseLandmark l1, PoseLandmark l2, PoseLandmark l3) {
        Vector vec1 = new Vector(l1, l2);
        Vector vec2 = new Vector(l3, l2);
        return vec1.angle(vec2);
    }

    void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
        PointF3D point = landmark.getPosition3D();
        updatePaintColorByZValue(
                paint, canvas, visualizeZ, rescaleZForVisualization, point.getZ(), zMin, zMax);
        canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), DOT_RADIUS, paint);
    }

    void drawCircle(Canvas canvas, PoseLandmark landmark, Paint paint) {
        if (landmark != null) {
            PointF3D point = landmark.getPosition3D();
            float x = point.getX();
            float y = point.getY();

            float radius = 24.0f;

            canvas.drawCircle(translateX(x), translateY(y), radius, paint);
        }
    }


    void drawLine(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark, Paint paint) {
        PointF3D start = startLandmark.getPosition3D();
        PointF3D end = endLandmark.getPosition3D();

        // Gets average z for the current body line
        float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
        updatePaintColorByZValue(
                paint, canvas, visualizeZ, rescaleZForVisualization, avgZInImagePixel, zMin, zMax);

        canvas.drawLine(
                translateX(start.getX()),
                translateY(start.getY()),
                translateX(end.getX()),
                translateY(end.getY()),
                paint);
    }

    public class PoseAngles {
        public double left_shoulder;
        public double right_shoulder;
        public double left_elbow;
        public double right_elbow;
        public double left_hip;
        public double right_hip;
        public double left_knee;
        public double right_knee;

        public PoseAngles(double left_shoulder, double right_shoulder, double left_elbow, double right_elbow,
                          double left_hip, double right_hip, double left_knee, double right_knee) {
            this.left_shoulder = left_shoulder;
            this.right_shoulder = right_shoulder;
            this.left_elbow = left_elbow;
            this.right_elbow = right_elbow;
            this.left_hip = left_hip;
            this.right_hip = right_hip;
            this.left_knee = left_knee;
            this.right_knee = right_knee;
        }

        // Constructor for initializing the values
        public PoseAngles(JSONObject angles) {

            try {
                this.left_shoulder = Double.parseDouble(angles.getString("left_shoulder"));
                this.right_shoulder = Double.parseDouble(angles.getString("right_shoulder"));
                this.left_elbow = Double.parseDouble(angles.getString("left_elbow"));
                this.right_elbow = Double.parseDouble(angles.getString("right_elbow"));
                this.left_hip = Double.parseDouble(angles.getString("left_hip"));
                this.right_hip = Double.parseDouble(angles.getString("right_hip"));
                this.left_knee = Double.parseDouble(angles.getString("left_knee"));
                this.right_knee = Double.parseDouble(angles.getString("right_knee"));
            } catch (JSONException e) { e.printStackTrace(); }
        }

        float[] compareTo(PoseAngles measuredPose) {
            float[] comparison = new float[8];

            comparison[0] = (float) (this.left_shoulder - measuredPose.left_shoulder);
            comparison[1] = (float) (this.right_shoulder - measuredPose.right_shoulder);
            comparison[2] = (float) (this.left_elbow - measuredPose.left_elbow);
            comparison[3] = (float) (this.right_elbow - measuredPose.right_elbow);
            comparison[4] = (float) (this.left_hip - measuredPose.left_hip);
            comparison[5] = (float) (this.right_hip - measuredPose.right_hip);
            comparison[6] = (float) (this.left_knee - measuredPose.left_knee);
            comparison[7] = (float) (this.right_knee - measuredPose.right_knee);

            return comparison;
        }
    }

    private static class Vector{
        private final float x;
        private final float y;
        private final float z;
        public Vector(PoseLandmark Point , PoseLandmark Vertex){
            this.x = Point.getPosition3D().getX() - Vertex.getPosition3D().getX();
            this.y = Point.getPosition3D().getY() - Vertex.getPosition3D().getY();
            this.z = Point.getPosition3D().getZ() - Vertex.getPosition3D().getZ();
        }
        public float dotProduct(Vector v) {
            return this.x * v.x + this.y * v.y + this.z * v.z;
        }

        public float magnitude() {
            return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }

        public float angle(Vector v){
            float mag1 = this.magnitude();
            float mag2 = v.magnitude();
            float dot = this.dotProduct(v);
            if(mag1 == 0f || mag2 == 0f)
                return -1f;
            float rad = (float)Math.acos(dot/(mag1 * mag2));
            return (float)(Math.toDegrees(rad));
        }
    }
}
