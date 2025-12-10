plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mybighomework"
    compileSdk = 36

    ndkVersion = "25.2.9519653"  // 使用稳定版本而不是依赖自动检测


    defaultConfig {
        applicationId = "com.example.mybighomework"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    
    // 百度ASR原生库打包配置
    packagingOptions {
        jniLibs {
            // 不要压缩这些原生库，保持原样
            useLegacyPackaging = true
        }
        // 保留这些特殊的原生库不被strip
        doNotStrip("*/*/libvad.dnn.so")
        doNotStrip("*/*/libbd_easr_s1_merge_normal_20151216.dat.so")
        doNotStrip("*/*/libBaiduSpeechSDK.so")
        doNotStrip("*/*/libbdEASRAndroid.so")
        doNotStrip("*/*/libbdSpilWakeup.so")
    }
    
    // 启用 ViewBinding
    buildFeatures {
        viewBinding = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // CardView 依赖项
    implementation("androidx.cardview:cardview:1.0.0")
    
    // FlexboxLayout 依赖项
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // HTTP客户端和JSON处理
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Retrofit 网络请求框架
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Glide 图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CameraX 相机功能
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Google ML Kit (OCR识别)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:translate:17.0.2")

    // Room 数据库依赖
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Lifecycle 组件（支持 ViewModel 和 LiveData）
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    
    // SharedPreferences 增强
    implementation("androidx.preference:preference:1.2.1")
    
    // 图表库 - MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0") // LiveData测试支持
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}