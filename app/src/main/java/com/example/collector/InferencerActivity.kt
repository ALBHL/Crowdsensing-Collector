package com.example.collector

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.collector.Graphics.ObjectGraphic
import com.example.collector.Helper.GraphicOverlay
import com.example.collector.TFLiteCustom.ImageSegmentationModelExecutor
import com.example.collector.TFLiteCustom.MLExecutionViewModel
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.demo.kotlin.facedetector.FaceGraphic
import com.google.mlkit.vision.demo.kotlin.labeldetector.LabelGraphic
import com.google.mlkit.vision.demo.kotlin.posedetector.PoseGraphic
import com.google.mlkit.vision.demo.kotlin.textdetector.TextGraphic
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_inferencer.*
import kotlinx.coroutines.asCoroutineDispatcher
import org.tensorflow.lite.Interpreter
import java.util.concurrent.Executors
import kotlin.math.max


class InferencerActivity: AppCompatActivity() {
    private var graphicOverlay: GraphicOverlay? = null
    // indicates whether the inference is made or not
    var doneInf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inferencer)
        var chooseMdl : Spinner = findViewById(R.id.spinner_choosemdl)
        val models = arrayOf("Object Detection", "Face Detection", "Pose Detection", "Image Labeling", "Text Recognition", "customSeg")
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
                    // reset view
                    graphicOverlay!!.clear()
                    // clear pop-up text
                    var logText: TextView = findViewById(R.id.log_view)  // textview in the pull-up list
                    logText.text = null
                    textView_count.text = "Count: 0"
                    // set doneInf to false
                    doneInf = false
                    imageViewshow!!.setImageBitmap(resizedBitmap)
                    when (position) {
                        0 -> detectObjectAndTrack(image)
                        1 -> detectFaces(image)
                        2 -> detectFPose(image)
                        3 -> labelImage(image)
                        4 -> recognizeText(image)
                        5 -> loadCustomModel(resizedBitmap)
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
        var logText: TextView = findViewById(R.id.log_view)  // textview in the pull-up list
        var count = 0
        var ids = 0
        var lab = ""

        button_inf.setOnClickListener {
            if (!doneInf) {
                doneInf = true
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        for (detectedObject in detectedObjects) {
                            count += 1
                            graphicOverlay?.add(
                                ObjectGraphic(
                                    graphicOverlay!!,
                                    detectedObject
                                )
                            )
                            logText.append("ObjectId: $count Labels:")
                            for (label in detectedObject.labels) {
                                val text = label.text
                                lab += text
                                logText.append("$text, ")
                            }
                            logText.append("\n \n")
                        }
                        textView_count.text = "Count: " + count.toString()
                        logText.append("Number of Object: " + count.toString())
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
        var count = 0
        var logText: TextView = findViewById(R.id.log_view)  // textview in the pull-up list

        button_inf.setOnClickListener {
            if(!doneInf) {
                doneInf = true
                val result = detector.process(image)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        for (face in faces) {
                            count += 1
                            val bounds = face.boundingBox
                            val trackId = face.trackingId
                            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
//                        graphicOverlay?.add(RectOverlay(graphicOverlay, face.boundingBox))
                            logText.append("FaceId: " + count.toString() + "\n"
                                    + " top: " + bounds.top.toString()
                                    + " bottom: " + bounds.bottom.toString()
                                    + " left: " +  bounds.left.toString()
                                    + " right: " +  bounds.right.toString() + "\n \n")
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
                        logText.append("Count: " + count.toString())
                        textView_count.text = "Count: " + count.toString()
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                    }
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
            if (!doneInf) {
                doneInf = true
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
    }

    private fun labelImage(image : InputImage) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        var logText: TextView = findViewById(R.id.log_view)  // textview in the pull-up list

        button_inf.setOnClickListener {
            if(!doneInf) {
                doneInf = true
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // Task completed successfully
                        for (label in labels) {
                            logText.append("Label: ${label.text}, confidence: ${label.confidence}, index: ${label.index}. \n")
                        }
                        graphicOverlay?.add(LabelGraphic(graphicOverlay!!, labels))
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Toast.makeText(this, "cannot inferrence", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // text recognition is not working
    private fun recognizeText(image : InputImage) {
        val recognizer = TextRecognition.getClient()

        button_inf.setOnClickListener {
            if(!doneInf) {
                doneInf = true
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


    private fun loadCustomModel(bmp : Bitmap) {
        lateinit var viewModel: MLExecutionViewModel
        var lastSavedFile = ""
        val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MLExecutionViewModel::class.java)
        viewModel.resultingBitmap.observe(
            this,
            Observer { resultImage ->
                if (resultImage != null) {
                    //update UI with the result
                    imageViewshow!!.setImageBitmap(resultImage.bitmapResult)
                    var logText: TextView = findViewById(R.id.log_view)  // textview in the pull-up list
                    logText.text = resultImage.executionLog
                }
            }
        )

        // create model class
        var imageSegmentationModel = ImageSegmentationModelExecutor(this, false)

        button_inf.setOnClickListener {
            if(!doneInf) {
                doneInf = true
                viewModel.onApplyModel(bmp, imageSegmentationModel, inferenceThread)
            }
        }
//        var interpreter : Interpreter
//        val imageSize = 257
//        val NUM_CLASSES = 21
//        val IMAGE_MEAN = 127.5f
//        val IMAGE_STD = 127.5f
//        val segmentColors = IntArray(NUM_CLASSES)
//        val labelsArrays = arrayOf(
//            "background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus",
//            "car", "cat", "chair", "cow", "dining table", "dog", "horse", "motorbike",
//            "person", "potted plant", "sheep", "sofa", "train", "tv"
//        )
//        val scaledBitmap =
//            ImageUtils.scaleBitmapAndKeepRatio(
//                bmp,
//                imageSize, imageSize
//            )
//        val contentArray =
//            ImageUtils.bitmapToByteBuffer(
//                scaledBitmap,
//                imageSize,
//                imageSize,
//                IMAGE_MEAN,
//                IMAGE_STD
//            )
//        val remoteModel = FirebaseCustomRemoteModel.Builder("Segmenter").build()
//        val conditions = FirebaseModelDownloadConditions.Builder()
//            .requireWifi()
//            .build()
//        FirebaseModelManager.getInstance().download(remoteModel, conditions)
//            .addOnCompleteListener {
//                // Download complete. Depending on your app, you could enable the ML
//                // feature, or switch from the local model to the remote model, etc.
//                Toast.makeText(this, "Successfully downloaded", Toast.LENGTH_SHORT).show()
//            }
//        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
//            .addOnCompleteListener { task ->
//                val modelFile = task.result
//                if (modelFile != null) {
//                    interpreter = Interpreter(modelFile)
//                }
//            }

    }
}