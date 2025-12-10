package com.example.mybighomework;

import androidx.camera.core.ExperimentalGetImage;

import com.example.mybighomework.TranslationOverlayView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
// ML Kit imports已移除，仅使用有道API

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraTranslationActivity extends AppCompatActivity {

    private static final String TAG = "CameraTranslation";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    // 注意：REQUEST_IMAGE_PICK常量已被Activity Result API取代，不再需要

    // UI组件
    private PreviewView previewView;
    private ImageButton btnBack, btnFlash, btnCapture, btnSelectImage, btnCloseResult;
    private TextView tvSourceLanguage, tvProcessing;
    private ProgressBar progressBar;

    // 拍照结果相关组件
    private FrameLayout photoResultContainer;
    private TranslationOverlayView translationOverlayView;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabResetZoom, fabRetake, fabClosePhoto;

    // 旧的翻译结果组件（保留用于兼容）
    private androidx.cardview.widget.CardView cardTranslationResult;
    private TextView tvOriginalText, tvTranslatedText;
    private Button btnCopyResult, btnSaveResult;

    // CameraX 组件
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;

    // ML Kit 组件已移除，仅使用有道API


    // Activity Result API for selecting images from gallery
    private ActivityResultLauncher<Intent> selectImageLauncher;

    // 状态变量
    private boolean isFlashOn = false;
    private boolean isAlbumMode = false; // 是否为相册选择模式
    private String sourceLanguage = "zh-CHS";  // 有道语言代码
    private String targetLanguage = "en";      // 有道语言代码


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_translation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化相机执行器
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 初始化视图
        initViews();

        // 设置点击监听器
        setupClickListeners();

        // ML Kit已删除，仅使用有道API
        if (!YoudaoTranslateConfig.isConfigValid()) {
            Toast.makeText(this, "请配置有道API密钥", Toast.LENGTH_LONG).show();
        }

        // 初始化Activity Result API
        initializeActivityResultLauncher();

        // 检查相机权限并启动相机
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initializeActivityResultLauncher() {
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {
                                processSelectedImage(imageUri);
                            }
                        }
                    }
                });
    }

    // 这个方法已被删除，initMLKit应该只初始化ML Kit组件

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        btnBack = findViewById(R.id.btnBack);
        btnFlash = findViewById(R.id.btnFlash);
        btnCapture = findViewById(R.id.btnCapture);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCloseResult = findViewById(R.id.btnCloseResult);
        tvSourceLanguage = findViewById(R.id.tvSourceLanguage);
        tvProcessing = findViewById(R.id.tvProcessing);
        progressBar = findViewById(R.id.progressBar);

        // 拍照结果相关组件
        photoResultContainer = findViewById(R.id.photoResultContainer);
        translationOverlayView = findViewById(R.id.translationOverlayView);
        fabResetZoom = findViewById(R.id.fabResetZoom);
        fabRetake = findViewById(R.id.fabRetake);
        fabClosePhoto = findViewById(R.id.fabClosePhoto);

        // 旧的翻译结果组件（保留用于相册选择）
        cardTranslationResult = findViewById(R.id.cardTranslationResult);
        tvOriginalText = findViewById(R.id.tvOriginalText);
        tvTranslatedText = findViewById(R.id.tvTranslatedText);
        btnCopyResult = findViewById(R.id.btnCopyResult);
        btnSaveResult = findViewById(R.id.btnSaveResult);

        // 更新语言显示
        if (tvSourceLanguage != null) {
            updateLanguageDisplay();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFlash.setOnClickListener(v -> toggleFlash());

        btnCapture.setOnClickListener(v -> takePhoto());

        btnSelectImage.setOnClickListener(v -> selectImageFromGallery());

        // 语言指示器点击切换
        if (tvSourceLanguage != null) {
            tvSourceLanguage.setOnClickListener(v -> {
                Log.d(TAG, "语言指示器被点击，切换语言");
                switchLanguage();
            });
        }

        // 拍照结果相关按钮
        if (fabResetZoom != null) {
            fabResetZoom.setOnClickListener(v -> resetZoom());
        }
        if (fabRetake != null) {
            fabRetake.setOnClickListener(v -> retakePhoto());
        }
        if (fabClosePhoto != null) {
            fabClosePhoto.setOnClickListener(v -> closePhotoResult());
        }

        // 翻译结果相关按钮（用于相册选择）
        if (btnCopyResult != null) {
            btnCopyResult.setOnClickListener(v -> copyTranslationResult());
        }
        if (btnSaveResult != null) {
            btnSaveResult.setOnClickListener(v -> saveTranslationResult());
        }
        if (btnCloseResult != null) {
            btnCloseResult.setOnClickListener(v -> hideTranslationResult());
        }
    }

    // initMLKit方法已删除，仅使用有道API

    // downloadTranslationModel方法已删除，仅使用有道API

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "需要相机权限才能使用拍照翻译功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 解绑所有现有用例
                cameraProvider.unbindAll();

                // 预览用例 - 不指定分辨率，让CameraX自动选择最佳分辨率
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 图像捕获用例 - 同样不指定分辨率，使用和预览相同的自动选择策略
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                // 选择后置相机
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 绑定生命周期
                camera = cameraProvider.bindToLifecycle(this, cameraSelector,
                        preview, imageCapture);

                // 设置闪光灯初始状态
                updateFlashButton();

                Log.d(TAG, "相机启动成功，PreviewView尺寸: " + previewView.getWidth() + "x" + previewView.getHeight());

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "相机启动失败", e);
                Toast.makeText(this, "相机启动失败", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleFlash() {
        if (camera != null) {
            isFlashOn = !isFlashOn;
            imageCapture.setFlashMode(isFlashOn ?
                    ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
            updateFlashButton();
        }
    }

    private void updateFlashButton() {
        if (btnFlash == null) return;

        if (isFlashOn) {
            btnFlash.setImageResource(R.drawable.ic_flash_on);
        } else {
            btnFlash.setImageResource(R.drawable.ic_flash_off);
        }
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // 显示处理状态
        showProcessing(true);
        if (tvProcessing != null) {
            tvProcessing.setText("正在拍照...");
        }

        // 创建临时文件来保存图片
        try {
            java.io.File outputDir = getCacheDir();
            java.io.File outputFile = java.io.File.createTempFile("camera_capture_", ".jpg", outputDir);

            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(outputFile).build();

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            showProcessing(false);
                            showPhotoResult(outputFile);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "拍照保存失败", exception);
                            showProcessing(false);
                            Toast.makeText(CameraTranslationActivity.this,
                                    "拍照失败: " + exception.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "创建临时文件失败", e);
            showProcessing(false);
            Toast.makeText(this, "拍照失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void processCapturedImageFile(java.io.File imageFile) {
        try {
            Log.d(TAG, "处理拍照图片文件: " + imageFile.getAbsolutePath());

            // 从文件加载Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (bitmap == null) {
                Log.e(TAG, "Bitmap解码失败，文件可能损坏");
                showProcessing(false);
                Toast.makeText(this, "图片处理失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Bitmap解码成功，尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // 使用有道API翻译（一次完成OCR+翻译+渲染）
            if (!YoudaoTranslateConfig.isConfigValid()) {
                showProcessing(false);
                Toast.makeText(this, "有道API未配置，请在YoudaoTranslateConfig中配置appKey", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "使用有道API进行图片翻译");
            if (tvProcessing != null) {
                tvProcessing.setText("正在使用有道翻译...");
            }
            
            YoudaoApiTranslator.translateImage(bitmap, sourceLanguage, targetLanguage, 
                new YoudaoApiTranslator.TranslateCallback() {
                    @Override
                    public void onSuccess(String originalText, String translatedText, String renderedImage) {
                        Log.d(TAG, "有道翻译成功");
                        showProcessing(false);
                        
                        // 显示服务端渲染的图片
                        if (renderedImage != null && !renderedImage.isEmpty()) {
                            Log.d(TAG, "显示有道服务端渲染的图片");
                            Bitmap renderedBitmap = YoudaoApiTranslator.base64ToBitmap(renderedImage);
                            if (renderedBitmap != null) {
                                showRenderedImage(renderedBitmap, originalText, translatedText);
                            } else {
                                Log.e(TAG, "渲染图片解码失败");
                                Toast.makeText(CameraTranslationActivity.this, 
                                    "图片解码失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "未收到渲染图片");
                            Toast.makeText(CameraTranslationActivity.this, 
                                "服务器未返回渲染图片", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.e(TAG, "有道翻译失败(" + errorCode + "): " + errorMessage);
                        showProcessing(false);
                        Toast.makeText(CameraTranslationActivity.this, 
                            "翻译失败: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

            // 删除临时文件
            boolean deleted = imageFile.delete();
            Log.d(TAG, "临时文件删除结果: " + deleted);

        } catch (Exception e) {
            Log.e(TAG, "图像处理失败", e);
            showProcessing(false);
            Toast.makeText(this, "图像处理失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    

    @ExperimentalGetImage
    private void processCapturedImage(ImageProxy image) {
        try {
            // 将ImageProxy转换为Bitmap
            Bitmap bitmap = imageProxyToBitmap(image);

            if (bitmap == null) {
                showProcessing(false);
                Toast.makeText(this, "图片处理失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }

            // OCR功能已删除，仅使用有道API（在processCapturedImageFile中）
            showProcessing(false);
            Toast.makeText(this, "请使用拍照功能，该方法已废弃", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "图像处理失败", e);
            showProcessing(false);
            Toast.makeText(this, "图像处理失败，请重试", Toast.LENGTH_SHORT).show();
        } finally {
            image.close();
        }
    }

    @ExperimentalGetImage
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        try {
            Image img = image.getImage();
            if (img == null) {
                Log.e(TAG, "Image is null");
                return null;
            }

            // 获取图像平面
            Image.Plane[] planes = img.getPlanes();
            if (planes.length < 3) {
                Log.e(TAG, "Not enough planes in image");
                return null;
            }

            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            // 复制Y平面
            yBuffer.get(nv21, 0, ySize);

            // 复制VU平面 (NV21格式)
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            // 创建YuvImage
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                    img.getWidth(), img.getHeight(), null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rect rect = new Rect(0, 0, img.getWidth(), img.getHeight());

            // 压缩为JPEG
            boolean compressSuccess = yuvImage.compressToJpeg(rect, 85, out);
            if (!compressSuccess) {
                Log.e(TAG, "Failed to compress YUV image to JPEG");
                return null;
            }

            byte[] imageBytes = out.toByteArray();
            if (imageBytes.length == 0) {
                Log.e(TAG, "Compressed image bytes is empty");
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from bytes");
                return null;
            }

            // 根据图像旋转角度旋转bitmap
            int rotationDegrees = image.getImageInfo().getRotationDegrees();
            if (rotationDegrees != 0) {
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(rotationDegrees);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle(); // 回收原bitmap
                return rotatedBitmap;
            }

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Bitmap conversion failed", e);
            return null;
        }
    }




    /**
     * 显示拍照结果（图片+翻译）
     */
    private void showPhotoResult(java.io.File imageFile) {
        try {
            // 设置为拍照模式
            isAlbumMode = false;

            // 隐藏相机预览
            if (previewView != null) {
                previewView.setVisibility(View.GONE);
            }

            // 显示拍照结果容器
            if (photoResultContainer != null) {
                photoResultContainer.setVisibility(View.VISIBLE);
            }

            // 显示拍照的图片
            if (translationOverlayView != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                translationOverlayView.setImageBitmap(bitmap);

                // 注意：合成图片已经包含翻译文字，不需要缩放监听器
            }

            // 开始处理图片中的文字
            showProcessing(true);
            if (tvProcessing != null) {
                tvProcessing.setText("正在识别文字...");
            }

            // 异步处理图片（延迟一点，让用户看到完整的图片）
            cameraExecutor.execute(() -> {
                try {
                    Thread.sleep(300); // 给用户300ms的时间欣赏图片
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                runOnUiThread(() -> {
                    if (tvProcessing != null) {
                        tvProcessing.setText("正在识别文字...");
                    }
                    processCapturedImageFile(imageFile);
                });
            });

        } catch (Exception e) {
            Log.e(TAG, "显示拍照结果失败", e);
            showProcessing(false);
            Toast.makeText(this, "显示图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示相册选择结果（图片+翻译）
     */
    private void showAlbumResult(Bitmap bitmap) {
        Log.d(TAG, "showAlbumResult called, bitmap size: " +
              (bitmap != null ? bitmap.getWidth() + "x" + bitmap.getHeight() : "null"));
        try {
            // 设置为相册模式
            isAlbumMode = true;

            // 隐藏相机预览
            if (previewView != null) {
                previewView.setVisibility(View.GONE);
            }

            // 显示拍照结果容器（复用相同的容器）
            if (photoResultContainer != null) {
                photoResultContainer.setVisibility(View.VISIBLE);
            }

            // 显示选择的图片
            if (translationOverlayView != null) {
                translationOverlayView.setImageBitmap(bitmap);

                // 立即开始OCR识别（关键修复）
                Log.d(TAG, "开始相册图片OCR识别");
                showProcessing(true);
                if (tvProcessing != null) {
                    tvProcessing.setText("正在识别文字...");
                }

                // 相册图片使用有道API翻译
                YoudaoApiTranslator.translateImage(bitmap, sourceLanguage, targetLanguage,
                    new YoudaoApiTranslator.TranslateCallback() {
                        @Override
                        public void onSuccess(String originalText, String translatedText, String renderedImage) {
                            showProcessing(false);
                            if (renderedImage != null && !renderedImage.isEmpty()) {
                                Bitmap renderedBitmap = YoudaoApiTranslator.base64ToBitmap(renderedImage);
                                if (renderedBitmap != null) {
                                    showRenderedImage(renderedBitmap, originalText, translatedText);
                                }
                            }
                        }
                        
                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            showProcessing(false);
                            Toast.makeText(CameraTranslationActivity.this,
                                "翻译失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
            } else {
                Log.e(TAG, "translationOverlayView 为 null");
                showProcessing(false);
                Toast.makeText(this, "无法显示图片", Toast.LENGTH_SHORT).show();
            }

            // 更新重拍按钮为重新选择图片
            updateRetakeButtonForAlbumMode();

            Log.d(TAG, "showAlbumResult completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "显示相册结果失败", e);
            showProcessing(false);
            Toast.makeText(this, "显示图片失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 重置缩放
     */
    private void resetZoom() {
        if (translationOverlayView != null) {
            translationOverlayView.resetZoom();

            // 注意：合成图片已经包含翻译文字，不需要重新设置翻译结果

            Toast.makeText(this, "已重置缩放", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重拍照片或重新选择图片
     */
    private void retakePhoto() {
        if (isAlbumMode) {
            // 相册模式：重新选择图片
            selectImageFromGallery();
        } else {
            // 拍照模式：重拍照片
            closePhotoResult();
            // 相机预览应该还在运行，不需要重新启动
        }
    }

    /**
     * 关闭照片结果显示
     */
    private void closePhotoResult() {
        if (photoResultContainer != null) {
            photoResultContainer.setVisibility(View.GONE);
        }
        // 注意：clearTranslations方法已被移除，因为合成图片已经包含翻译文字


        // 重置相册模式状态
        isAlbumMode = false;

        // 恢复相机预览
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示有道服务端渲染的图片结果
     * @param renderedBitmap 有道服务端渲染好的图片（已包含翻译文字）
     * @param originalText 原文（用于复制）
     * @param translatedText 译文（用于复制）
     */
    private void showRenderedImage(Bitmap renderedBitmap, String originalText, String translatedText) {
        Log.d(TAG, "显示有道渲染的图片");
        showProcessing(false);
        
        // 显示图片容器
        if (photoResultContainer != null) {
            photoResultContainer.setVisibility(View.VISIBLE);
        }
        
        // 隐藏相机预览
        if (previewView != null) {
            previewView.setVisibility(View.GONE);
        }
        
        // 显示渲染好的图片（不需要叠加文字，有道已经渲染好了）
        if (translationOverlayView != null) {
            translationOverlayView.setImageBitmap(renderedBitmap);
        }
        
        Toast.makeText(this, "有道翻译完成，可缩放和滚动查看", Toast.LENGTH_SHORT).show();
    }

    // 旧的showTranslationResult方法已移除，只使用showRenderedImage

    /**
     * 格式化长文本，使其更易读
     */
    private String formatLongText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 如果文本太长，进行智能截断和格式化
        String trimmedText = text.trim();

        // 移除多余的空白字符和换行符
        trimmedText = trimmedText.replaceAll("\\s+", " ");
        trimmedText = trimmedText.replaceAll("\\n+", "\n");

        // 如果文本超过500个字符，进行截断
        final int MAX_LENGTH = 500;
        if (trimmedText.length() > MAX_LENGTH) {
            trimmedText = trimmedText.substring(0, MAX_LENGTH - 3) + "...";
            Log.w(TAG, "文本过长，已截断显示");

            // 在UI线程中显示提示
            runOnUiThread(() -> {
                Toast.makeText(CameraTranslationActivity.this,
                    "文本内容过长，已自动截断显示",
                    Toast.LENGTH_SHORT).show();
            });
        }

        // 在适当位置添加换行，使其更易读
        if (trimmedText.length() > 50) {
            // 在标点符号后添加换行
            trimmedText = trimmedText.replaceAll("([。！？.!?；;])", "$1\n");
            // 在逗号后添加换行，但避免过度换行
            if (trimmedText.split("\n").length < 10) {
                trimmedText = trimmedText.replaceAll("([，,])", "$1\n");
            }
        }

        return trimmedText;
    }

    /**
     * 更新重拍按钮为相册模式样式
     */
    private void updateRetakeButtonForAlbumMode() {
        if (fabRetake != null) {
            // 在相册模式下，更改图标为相册图标
            fabRetake.setImageResource(R.drawable.ic_gallery);
            // 可以考虑添加一个提示，表示这是重新选择图片
            fabRetake.setContentDescription("重新选择图片");
        }
    }

    private void hideTranslationResult() {
        if (cardTranslationResult != null) {
            cardTranslationResult.setVisibility(View.GONE);
        }
    }

    private void copyTranslationResult() {
        // ML Kit已删除，此功能暂不可用
        Toast.makeText(this, "复制功能暂不可用", Toast.LENGTH_SHORT).show();
    }


    private void saveTranslationResult() {
        // ML Kit已删除，此功能暂不可用
        Toast.makeText(this, "保存功能暂不可用", Toast.LENGTH_SHORT).show();
    }
    
    // getTranslationAndSave方法已删除

    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("翻译结果", text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * 保存翻译结果到本地数据库
     * 将翻译记录保存为一个简单的学习记录
     */
    private void saveToVocabulary(String original, String translation) {
        new Thread(() -> {
            try {
                // 获取数据库实例
                com.example.mybighomework.database.AppDatabase database = 
                    com.example.mybighomework.database.AppDatabase.getInstance(this);
                
                // 创建学习记录
                com.example.mybighomework.database.entity.StudyRecordEntity studyRecord = 
                    new com.example.mybighomework.database.entity.StudyRecordEntity();
                studyRecord.setStudyType("拍照翻译");
                studyRecord.setNotes(original + " -> " + translation); // 使用notes字段保存翻译对
                studyRecord.setCorrect(true); // 翻译记录默认为正确
                studyRecord.setStudyDate(new java.util.Date());
                
                // 保存到数据库
                database.studyRecordDao().insertStudyRecord(studyRecord);
                
                Log.d(TAG, "翻译记录已保存: " + original + " -> " + translation);
                
            } catch (Exception e) {
                Log.e(TAG, "保存翻译记录失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    // 注意：onActivityResult方法已被Activity Result API取代，不再需要

    private void processSelectedImage(Uri imageUri) {
        Log.d(TAG, "processSelectedImage called with URI: " + imageUri);
        // ML Kit翻译已删除，此功能不再可用
        Toast.makeText(this, "请使用拍照翻译功能", Toast.LENGTH_SHORT).show();
    }

    private void switchLanguage() {
        Log.d(TAG, "switchLanguage 方法被调用");
        Log.d(TAG, "切换前 - 源语言: " + sourceLanguage + ", 目标语言: " + targetLanguage);
        
        // 切换语言（使用有道API的语言代码）
        if (sourceLanguage.equals("zh-CHS")) {
            sourceLanguage = "en";
            targetLanguage = "zh-CHS";
        } else {
            sourceLanguage = "zh-CHS";
            targetLanguage = "en";
        }
        
        Log.d(TAG, "切换后 - 源语言: " + sourceLanguage + ", 目标语言: " + targetLanguage);

        updateLanguageDisplay();

        // 显示提示信息
        Toast.makeText(this, "已切换语言方向", Toast.LENGTH_SHORT).show();
    }

    private void updateLanguageDisplay() {
        if (tvSourceLanguage == null) return;

        String fromText = sourceLanguage.equals("zh-CHS") ? "中文" : "英文";
        String toText = targetLanguage.equals("zh-CHS") ? "中文" : "英文";
        String displayText = fromText + " → " + toText;
        tvSourceLanguage.setText(displayText);
    }



    private void showProcessing(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (tvProcessing != null) {
            tvProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }




    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        // ML Kit组件已移除


        // 清理图片资源
        if (translationOverlayView != null) {
            translationOverlayView.clearResources();
        }

    }


}
