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
import java.util.List;
import java.util.Locale;

/** Draw the detected pose in preview. */
public class PoseGraphic extends Graphic {

    private static final float DOT_RADIUS = 8.0f;
    private static final float IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f;
    private static final float STROKE_WIDTH = 10.0f;
    private static final float POSE_CLASSIFICATION_TEXT_SIZE = 60.0f;

    private final Pose pose;
    private final boolean showInFrameLikelihood;
    private final boolean visualizeZ;
    private final boolean rescaleZForVisualization;
    private float zMin = Float.MAX_VALUE;
    private float zMax = Float.MIN_VALUE;

    private final List<String> poseClassification;
    private final Paint classificationTextPaint;
    private final Paint leftPaint;
    private final Paint rightPaint;
    private final Paint whitePaint;
    private static float[][] angles = new float[4][2];
    private class Vector{
        private float x , y , z;
        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
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
    PoseGraphic(
            GraphicOverlay overlay,
            Pose pose,
            boolean showInFrameLikelihood,
            boolean visualizeZ,
            boolean rescaleZForVisualization,
            List<String> poseClassification) {
        super(overlay);
        this.pose = pose;
        this.showInFrameLikelihood = showInFrameLikelihood;
        this.visualizeZ = visualizeZ;
        this.rescaleZForVisualization = rescaleZForVisualization;

        this.poseClassification = poseClassification;
        classificationTextPaint = new Paint();
        classificationTextPaint.setColor(Color.WHITE);
        classificationTextPaint.setTextSize(POSE_CLASSIFICATION_TEXT_SIZE);
        classificationTextPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(STROKE_WIDTH);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(IN_FRAME_LIKELIHOOD_TEXT_SIZE);
        leftPaint = new Paint();
        leftPaint.setStrokeWidth(STROKE_WIDTH);
        leftPaint.setColor(Color.GREEN);
        rightPaint = new Paint();
        rightPaint.setStrokeWidth(STROKE_WIDTH);
        rightPaint.setColor(Color.YELLOW);
    }
    public void init(Vector V , PoseLandmark Point , PoseLandmark Vertex){
        V.x = Point.getPosition3D().getX() - Vertex.getPosition3D().getX();
        V.y = Point.getPosition3D().getY() - Vertex.getPosition3D().getY();
        V.z = Point.getPosition3D().getZ() - Vertex.getPosition3D().getZ();
        return ;
    }
    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> pose_landmarks = pose.getAllPoseLandmarks();
        if (pose_landmarks.isEmpty()) {
            return;
        }

        List<PoseLandmark> landmarks = List.copyOf(pose_landmarks.subList(PoseLandmark.RIGHT_MOUTH + 1, pose_landmarks.size()));

        // Draw pose classification text.
        float classificationX = POSE_CLASSIFICATION_TEXT_SIZE * 0.5f;
        for (int i = 0; i < poseClassification.size(); i++) {
            float classificationY =
                    (canvas.getHeight()
                            - POSE_CLASSIFICATION_TEXT_SIZE * 1.5f * (poseClassification.size() - i));
            canvas.drawText(
                    poseClassification.get(i), classificationX, classificationY, classificationTextPaint);
        }

        // Draw all the points
        for (PoseLandmark landmark : landmarks) {
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

//        Log.d("PoseGraphic", "LEFT_SHOULDER = " + leftShoulder.getPosition3D().getX());

        PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
        PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
        PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
        PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
        PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
        PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
        PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
        PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);
        // Calculate angles for 4 pairs of joints , in each half of the body (right and left) .
        Vector L_Eljoint1 = new Vector(0f , 0f , 0f);
        Vector L_Eljoint2 = new Vector(0f , 0f , 0f);
        Vector L_Sjoint1 =  new Vector(0f , 0f , 0f);
        Vector L_Sjoint2 =  new Vector(0f , 0f , 0f);
        Vector L_Hjoint1 =  new Vector(0f , 0f , 0f);
        Vector L_Hjoint2 =  new Vector(0f , 0f , 0f);
        Vector L_Kjoint1 =  new Vector(0f , 0f , 0f);
        Vector L_Kjoint2 =  new Vector(0f , 0f , 0f);

        Vector R_Eljoint1 = new Vector(0f , 0f , 0f);
        Vector R_Eljoint2 = new Vector(0f , 0f , 0f);
        Vector R_Sjoint1 =  new Vector(0f , 0f , 0f);
        Vector R_Sjoint2 =  new Vector(0f , 0f , 0f);
        Vector R_Hjoint1 =  new Vector(0f , 0f , 0f);
        Vector R_Hjoint2 =  new Vector(0f , 0f , 0f);
        Vector R_Kjoint1 =  new Vector(0f , 0f , 0f);
        Vector R_Kjoint2 =  new Vector(0f , 0f , 0f);

        init(L_Eljoint1 , leftShoulder , leftElbow);
        init(L_Eljoint2 , leftWrist , leftElbow);
        init(L_Sjoint1 , leftElbow , leftShoulder);
        init(L_Sjoint2 , leftHip , leftShoulder);
        init(L_Hjoint1 , leftShoulder , leftHip);
        init(L_Hjoint2 , leftKnee , leftHip);
        init(L_Kjoint1 , leftHip , leftKnee);
        init(L_Kjoint2 , leftHeel , leftKnee);

        init(R_Eljoint1 , leftShoulder , leftElbow);
        init(R_Eljoint2 , leftWrist , leftElbow);
        init(R_Sjoint1 , leftElbow , leftShoulder);
        init(R_Sjoint2 , leftHip , leftShoulder);
        init(R_Hjoint1 , leftShoulder , leftHip);
        init(R_Hjoint2 , leftKnee , leftHip);
        init(R_Kjoint1 , leftHip , leftKnee);
        init(R_Kjoint2 , leftHeel , leftKnee);

        angles[0][0] = L_Eljoint1.angle(L_Eljoint2);
        angles[0][1] = R_Eljoint1.angle(R_Eljoint2);

        angles[1][0] = L_Sjoint1.angle(L_Sjoint2);
        angles[1][1] = R_Sjoint1.angle(R_Sjoint2);

        angles[2][0] = L_Hjoint1.angle(L_Hjoint2);
        angles[2][1] = R_Hjoint1.angle(R_Hjoint2);

        angles[3][0] = L_Kjoint1.angle(L_Kjoint2);
        angles[3][1] = R_Kjoint1.angle(R_Kjoint2);
//        Log.d("PoseGraphic" , "Left_Elbow joint: " + angles[0][0]);
        drawLine(canvas, leftShoulder, rightShoulder, whitePaint);
        drawLine(canvas, leftHip, rightHip, whitePaint);

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint);
        drawLine(canvas, leftElbow, leftWrist, leftPaint);
        drawLine(canvas, leftShoulder, leftHip, leftPaint);
        drawLine(canvas, leftHip, leftKnee, leftPaint);
        drawLine(canvas, leftKnee, leftAnkle, leftPaint);
        drawLine(canvas, leftWrist, leftThumb, leftPaint);
        drawLine(canvas, leftWrist, leftPinky, leftPaint);
        drawLine(canvas, leftWrist, leftIndex, leftPaint);
        drawLine(canvas, leftIndex, leftPinky, leftPaint);
        drawLine(canvas, leftAnkle, leftHeel, leftPaint);
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint);

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint);
        drawLine(canvas, rightElbow, rightWrist, rightPaint);
        drawLine(canvas, rightShoulder, rightHip, rightPaint);
        drawLine(canvas, rightHip, rightKnee, rightPaint);
        drawLine(canvas, rightKnee, rightAnkle, rightPaint);
        drawLine(canvas, rightWrist, rightThumb, rightPaint);
        drawLine(canvas, rightWrist, rightPinky, rightPaint);
        drawLine(canvas, rightWrist, rightIndex, rightPaint);
        drawLine(canvas, rightIndex, rightPinky, rightPaint);
        drawLine(canvas, rightAnkle, rightHeel, rightPaint);
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint);

        // Draw inFrameLikelihood for all points
        if (showInFrameLikelihood) {
            for (PoseLandmark landmark : landmarks) {
                canvas.drawText(
                        String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
                        translateX(landmark.getPosition().x),
                        translateY(landmark.getPosition().y),
                        whitePaint);
            }
        }
    }

    void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
        PointF3D point = landmark.getPosition3D();
        updatePaintColorByZValue(
                paint, canvas, visualizeZ, rescaleZForVisualization, point.getZ(), zMin, zMax);
        canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), DOT_RADIUS, paint);
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
}
