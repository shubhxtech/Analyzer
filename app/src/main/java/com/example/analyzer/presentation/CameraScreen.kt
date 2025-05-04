package com.example.analyzer.presentation

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }

    // Request camera permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val encodedUri = Uri.encode(it.toString())
            navController.navigate("analysis/$encodedUri")
        }
    }

    // Request camera permission on entry
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Create ImageCapture instance
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Executor for photo capture
    val executor = remember { ContextCompat.getMainExecutor(context) }

    // Output directory for saving photos
    val outputDirectory = remember {
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (hasPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera preview
                CameraPreview(
                    imageCapture = imageCapture,
                    onImageCaptured = { uri ->
                        imageUri = uri
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate("analysis/$encodedUri")
                    },
                    flashEnabled = flashEnabled,
                    useFrontCamera = useFrontCamera
                )

                // Top app bar with back button
                TopAppBar(
                    title = { Text("Take a Photo", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.statusBarsPadding()
                )

                // Camera controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Guide text
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "Position your tongue in the center of the frame and keep your mouth open wide",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Control buttons row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Flash toggle button
                            IconButton(
                                onClick = { flashEnabled = !flashEnabled },
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(1.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Toggle Flash",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Capture button
                        IconButton(
                            onClick = {
                                takePhoto(
                                    imageCapture = imageCapture,
                                    outputDirectory = outputDirectory,
                                    executor = executor,
                                    onImageCaptured = { uri ->
                                        imageUri = uri
                                        val encodedUri = Uri.encode(uri.toString())
                                        navController.navigate("analysis/$encodedUri")
                                    },
                                    onError = { exception ->
                                        Log.e("CameraScreen", "Failed to capture photo", exception)
                                        Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take Photo",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Additional controls row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Switch camera button
                            IconButton(
                                onClick = { useFrontCamera = !useFrontCamera },
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(1.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Gallery picker button
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(1.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pick from Gallery",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Tongue guide overlay
                GuideOverlay()
            }
        } else {
            // Permission not granted screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "This app needs camera access to take photos of your tongue for analysis",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    flashEnabled: Boolean,
    useFrontCamera: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraSelector = remember(useFrontCamera) {
        if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA
    }

    LaunchedEffect(flashEnabled, useFrontCamera) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Set flash mode only if back camera is in use (most front cameras don't have flash)
        if (!useFrontCamera) {
            try {
                camera.cameraControl.enableTorch(flashEnabled)
                imageCapture.flashMode = if (flashEnabled) {
                    ImageCapture.FLASH_MODE_ON
                } else {
                    ImageCapture.FLASH_MODE_OFF
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Flash mode not supported", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                // context.getCameraProvider().unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to unbind camera", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}

private fun takePhoto(
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}