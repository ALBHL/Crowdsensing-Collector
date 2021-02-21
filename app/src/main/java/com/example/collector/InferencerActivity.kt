package com.example.collector

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collector.Helper.GraphicOverlay
import com.example.collector.Helper.RectOverlay
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.demo.kotlin.facedetector.FaceGraphic
import com.google.mlkit.vision.demo.kotlin.labeldetector.LabelGraphic
import com.google.mlkit.vision.demo.kotlin.posedetector.PoseGraphic
import com.google.mlkit.vision.demo.kotlin.textdetector.TextGraphic
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_inferencer.*
import kotlin.math.max


class InferencerActivity: AppCompatActivity() {
    private var graphicOverlay: GraphicOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inferencer)
        var chooseMdl : Spinner = findViewById(R.id.spinner_choosemdl)
        val models = arrayOf("Object Detection", "Face Detection", "Pose Detection", "Image Labeling", "Text Recognition")
        chooseMdl.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, models)


        val context = this
        val db = DataBaseHandler(context)
        val bmp = db.readDataImg()

        graphicOverlay = findViewById(R.id.graphic_overlay_inf)
        // Clear the overlay first
        graphicOverlay!!.clear()
        // Get the dimensions of the image view
        val targetedSize = Pair(768, 1024)
        // Determine how much to scale down the image
        if (bmp != null) {
            val scaleFactor = max(
                bmp.width.toFloat() / targetedSize.first.toFloat(),
                bmp.height.toFloat() / targetedSize.second.toFloat()
            )
            val resizedBitmap = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width / scaleFactor).toInt(),
                (bmp.height / scaleFactor).toInt(),
                true
            )
            imageViewshow!!.setImageBitmap(resizedBitmap)
            var image = InputImage.fromBitmap(resizedBitmap, 0)

            // invoke the model accroding to user's choice
            chooseMdl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //default to object detection
                    detectObjectAndTrack(image)
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    graphicOverlay!!.clear()
                    when (position) {
                        0 -> detectObjectAndTrack(image)
                        1 -> detectFaces(image)
                        2 -> detectFPose(image)
                        3 -> labelImage(image)
                        4 -> recognizeText(image)
                    }
                }
            }
        }
    }

    private fun detectObjectAndTrack(image : InputImage) {
//        // Live detection and tracking
//        val options = ObjectDetectorOptions.Builder()
//            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
//            .enableClassification()  // Optional
//            .build()

        // Multiple object detection in static images
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        val objectDetector = ObjectDetection.getClient(options)
        var count = 0
        var ids = 0
        var lab = ""

        button_inf.setOnClickListener {
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    for (detectedObject in detectedObjects) {
                        graphicOverlay?.add(ObjectGraphic(graphicOverlay!!, detectedObject))
                        count += 1
                        for (label in detectedObject.labels) {
                            count += 100
                            val text = label.text
                            lab += text
                        }
                    }
                    textView_count.text = "Count: " + count.toString() + lab
                }
//                .addOnSuccessListener(object : OnSuccessListener<List<DetectedObject>> {
//                    override fun onSuccess(results: List<DetectedObject>) {
//                        for (result in results) {
//                            graphicOverlay?.add(ObjectGraphic(graphicOverlay!!, result))
//                            count += 1
//                            ids += if (result.trackingId != null) {
//                                result.trackingId
//                            } else {
//                                100
//                            }
//                        }
//                        textView_count.text = "Count: " + ids.toString() + count.toString()
//                    }
//                })
                .addOnFailureListener { e ->
                    Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun detectFaces(image : InputImage) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient();

        button_inf.setOnClickListener {
            val result = detector.process(image)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    for (face in faces) {
                        val bounds = face.boundingBox
                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
//                        graphicOverlay?.add(RectOverlay(graphicOverlay, face.boundingBox))
                        graphicOverlay?.add(FaceGraphic(graphicOverlay!!, face))

//                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
//                        // nose available):
//                        val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
//                        leftEar?.let {
//                            val leftEarPos = leftEar.position
//                        }
//
//                        // If classification was enabled:
//                        if (face.smilingProbability != null) {
//                            val smileProb = face.smilingProbability
//                        }
//                        if (face.rightEyeOpenProbability != null) {
//                            val rightEyeOpenProb = face.rightEyeOpenProbability
//                        }
//
//                        // If face tracking was enabled:
//                        if (face.trackingId != null) {
//                            val id = face.trackingId
//                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun detectFPose(image : InputImage) {
        // Base pose detector with streaming frames, when depending on the pose-detection sdk
//        val options = PoseDetectorOptions.Builder()
//            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
//            .build()

        // Accurate pose detector on static images, when depending on the pose-detection-accurate sdk
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()

        val poseDetector = PoseDetection.getClient(options)

        button_inf.setOnClickListener {
            val result = poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    // Task completed successfully
                    graphicOverlay?.add(PoseGraphic(graphicOverlay!!, pose, true, true, true))
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun labelImage(image : InputImage) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        button_inf.setOnClickListener {
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    graphicOverlay?.add(LabelGraphic(graphicOverlay!!, labels))
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // text recognition is not working
    private fun recognizeText(image : InputImage) {
        val recognizer = TextRecognition.getClient()

        button_inf.setOnClickListener {
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    graphicOverlay?.add(TextGraphic(graphicOverlay, visionText))
//                    for (block in visionText.textBlocks) {
//                        val boundingBox = block.boundingBox
//                        val cornerPoints = block.cornerPoints
//                        val text = block.text
//                        Log.d("Inference", "textrec" + text)
//
//                        for (line in block.lines) {
//                            // ...
//                            for (element in line.elements) {
//                                // ...
//                            }
//                        }
//                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                }
        }
    }
}