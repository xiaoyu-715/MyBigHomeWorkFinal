# 清理ML Kit代码清单

## 需要删除的代码

### 1. 删除imports
```java
// 删除这些imports
import com.google.mlkit.nl.translate.*;
import com.google.mlkit.vision.text.*;
```

### 2. 删除成员变量
```java
// 删除
private TextRecognizer textRecognizer;
private Translator translator;
private com.google.mlkit.vision.text.Text lastOcrResult;
private boolean isModelDownloading = false;
```

### 3. 删除整个方法
- `downloadTranslationModel()`
- `recognizeText(Bitmap bitmap)`
- `translateText(String text)`
- `translateTextBlocks(String fullText)`  
- `displayBlockTranslations(String[] translations)`
- `showTranslationResult(String originalText, String translatedText)`
- `initTranslator()`

### 4. 修改onCreate()
删除：
```java
textRecognizer = TextRecognition.getClient(...);
downloadTranslationModel();
```

添加：
```java
// 检查有道配置
if (!YoudaoTranslateConfig.isConfigValid()) {
    Toast.makeText(this, "请配置有道API", Toast.LENGTH_LONG).show();
}
```

### 5. 修改语言变量
从：
```java
private String sourceLanguage = TranslateLanguage.CHINESE;
private String targetLanguage = TranslateLanguage.ENGLISH;
```

改为：
```java
private String sourceLanguage = "zh-CHS";
private String targetLanguage = "en";
```

### 6. 简化switchLanguage()
删除downloadTranslationModel()调用

### 7. 修改processCapturedImageFile()
只保留有道API调用，删除ML Kit降级逻辑

### 8. 修改onDestroy()
删除：
```java
if (textRecognizer != null) {
    textRecognizer.close();
}
if (translator != null) {
    translator.close();
}
```

##完整代码请参考上述修改



