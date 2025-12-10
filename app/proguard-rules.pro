# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==================== 有道翻译SDK混淆规则 ====================
# 注意：集成有道SDK后取消以下注释
#-ignorewarnings
#-libraryjars libs/YoudaoBase_v20230803.jar
#-libraryjars libs/YoudaoTranslateOnline_v2.0.1.jar
#
#-keep class com.youdao.sdk.ydtranslate.** { *; }
#-keep class com.youdao.sdk.ydonlinetranslate.** { *; }
#-keep class com.youdao.sdk.app.** { *; }

# ==================== ML Kit混淆规则 ====================
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ==================== 讯飞SDK混淆规则 ====================
-keep class com.iflytek.** { *; }
-dontwarn com.iflytek.**