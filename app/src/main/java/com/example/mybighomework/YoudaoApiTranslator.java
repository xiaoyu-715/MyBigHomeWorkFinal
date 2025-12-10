package com.example.mybighomework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 有道图片翻译API调用类
 * 使用HTTP API方式调用有道图片翻译服务
 */
public class YoudaoApiTranslator {
    
    private static final String TAG = "YoudaoApiTranslator";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    /**
     * OCR识别区域
     */
    public static class OcrRegion {
        public String context;          // 原文
        public String tranContent;      // 译文
        public int left, top, width, height;  // 位置信息
        
        public OcrRegion(String context, String tranContent, int left, int top, int width, int height) {
            this.context = context;
            this.tranContent = tranContent;
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }
    }
    
    /**
     * 翻译回调接口
     */
    public interface TranslateCallback {
        /**
         * 翻译成功
         * @param originalText OCR识别的原文
         * @param translatedText 翻译后的文本
         * @param renderedImage 服务端渲染的图片（Base64），如果render=1则有值
         */
        void onSuccess(String originalText, String translatedText, String renderedImage);
        
        /**
         * 翻译失败
         * @param errorCode 错误代码
         * @param errorMessage 错误信息
         */
        void onError(String errorCode, String errorMessage);
    }
    
    /**
     * 图片翻译
     * @param bitmap 要翻译的图片
     * @param from 源语言（en, zh-CHS, auto等）
     * @param to 目标语言
     * @param callback 回调接口
     */
    public static void translateImage(Bitmap bitmap, String from, String to, TranslateCallback callback) {
        if (!YoudaoTranslateConfig.isConfigValid()) {
            callback.onError("-1", "有道API配置无效");
            return;
        }
        
        executor.execute(() -> {
            try {
                // 1. 转换图片为Base64
                String base64Image = bitmapToBase64(bitmap);
                if (base64Image == null) {
                    postError(callback, "-2", "图片转换失败");
                    return;
                }
                
                // 2. 生成签名参数
                String salt = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                String curtime = String.valueOf(System.currentTimeMillis() / 1000);
                
                // 3. 计算input用于签名
                String input = calculateInput(base64Image);
                
                // 4. 生成签名
                String sign = generateSign(input, salt, curtime);
                
                // 5. 构建请求参数
                String params = buildParams(base64Image, from, to, salt, curtime, sign);
                
                // 6. 发送HTTP请求
                String response = sendHttpRequest(params);
                
                // 7. 解析响应
                parseResponse(response, callback);
                
            } catch (Exception e) {
                Log.e(TAG, "翻译失败", e);
                postError(callback, "-3", "翻译请求失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 将Bitmap转换为Base64
     */
    private static String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 压缩图片（有道API建议图片不超过4MB）
            int quality = 85;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            
            // 如果图片过大，继续压缩
            while (baos.toByteArray().length > 4 * 1024 * 1024 && quality > 50) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap转Base64失败", e);
            return null;
        }
    }
    
    /**
     * 计算input用于签名
     * 规则：
     * - 当q长度≤20时，input = q
     * - 当q长度>20时，input = q前10个字符 + q长度 + q后10个字符
     */
    private static String calculateInput(String q) {
        if (q.length() <= 20) {
            return q;
        } else {
            String start = q.substring(0, 10);
            String end = q.substring(q.length() - 10);
            return start + q.length() + end;
        }
    }
    
    /**
     * 生成签名
     * sign = sha256(appKey + input + salt + curtime + appSecret)
     */
    private static String generateSign(String input, String salt, String curtime) {
        try {
            String signStr = YoudaoTranslateConfig.APP_KEY + 
                           input + 
                           salt + 
                           curtime + 
                           YoudaoTranslateConfig.APP_SECRET;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signStr.getBytes(StandardCharsets.UTF_8));
            
            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "生成签名失败", e);
            return "";
        }
    }
    
    /**
     * 构建请求参数
     */
    private static String buildParams(String q, String from, String to, 
                                     String salt, String curtime, String sign) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("type=").append(URLEncoder.encode(YoudaoTranslateConfig.IMG_TYPE, "UTF-8"));
            params.append("&q=").append(URLEncoder.encode(q, "UTF-8"));
            params.append("&from=").append(URLEncoder.encode(from, "UTF-8"));
            params.append("&to=").append(URLEncoder.encode(to, "UTF-8"));
            params.append("&appKey=").append(URLEncoder.encode(YoudaoTranslateConfig.APP_KEY, "UTF-8"));
            params.append("&salt=").append(URLEncoder.encode(salt, "UTF-8"));
            params.append("&sign=").append(URLEncoder.encode(sign, "UTF-8"));
            params.append("&signType=").append(URLEncoder.encode(YoudaoTranslateConfig.SIGN_TYPE, "UTF-8"));
            params.append("&curtime=").append(URLEncoder.encode(curtime, "UTF-8"));
            params.append("&docType=").append(URLEncoder.encode(YoudaoTranslateConfig.DOC_TYPE, "UTF-8"));
            params.append("&render=").append(URLEncoder.encode(YoudaoTranslateConfig.SERVER_RENDER, "UTF-8"));
            params.append("&translateOptions=").append(URLEncoder.encode(YoudaoTranslateConfig.TRANSLATE_MODE, "UTF-8"));
            
            return params.toString();
        } catch (Exception e) {
            Log.e(TAG, "构建参数失败", e);
            return "";
        }
    }
    
    /**
     * 发送HTTP POST请求
     */
    private static String sendHttpRequest(String params) throws Exception {
        URL url = new URL(YoudaoTranslateConfig.API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setConnectTimeout(YoudaoTranslateConfig.DEFAULT_TIMEOUT);
        connection.setReadTimeout(YoudaoTranslateConfig.DEFAULT_TIMEOUT);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Response Code: " + responseCode);
        
        BufferedReader reader;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        
        return response.toString();
    }
    
    /**
     * 解析响应结果
     */
    private static void parseResponse(String response, TranslateCallback callback) {
        try {
            Log.d(TAG, "Response length: " + response.length());
            // 打印响应的前1000个字符以便调试
            if (response.length() > 1000) {
                Log.d(TAG, "Response preview: " + response.substring(0, 1000));
            } else {
                Log.d(TAG, "Response full: " + response);
            }
            
            JSONObject json = new JSONObject(response);
            String errorCode = json.optString("errorCode", "-1");
            
            // 打印所有的JSON键
            Log.d(TAG, "JSON keys: " + json.keys().toString());
            
            if ("0".equals(errorCode)) {
                // 成功
                StringBuilder originalText = new StringBuilder();
                StringBuilder translatedText = new StringBuilder();
                String renderedImage = null;
                
                // 检查是否有服务端渲染的图片（注意：有道API使用下划线命名）
                if (json.has("render_image")) {
                    renderedImage = json.optString("render_image", "");
                    Log.d(TAG, "找到render_image字段，大小: " + renderedImage.length());
                } else if (json.has("renderImage")) {
                    renderedImage = json.optString("renderImage", "");
                    Log.d(TAG, "找到renderImage字段，大小: " + renderedImage.length());
                } else if (json.has("Result")) {
                    // 有些API可能用Result字段
                    JSONObject result = json.optJSONObject("Result");
                    if (result != null && result.has("img")) {
                        renderedImage = result.optString("img", "");
                        Log.d(TAG, "找到Result.img字段，大小: " + renderedImage.length());
                    }
                } else {
                    Log.w(TAG, "未找到渲染图片字段！可用字段: " + json.keys().toString());
                }
                
                JSONArray resRegions = json.optJSONArray("resRegions");
                if (resRegions != null) {
                    for (int i = 0; i < resRegions.length(); i++) {
                        JSONObject region = resRegions.getJSONObject(i);
                        String context = region.optString("context", "");
                        String tranContent = region.optString("tranContent", "");
                        
                        if (!context.isEmpty()) {
                            originalText.append(context).append("\n");
                        }
                        if (!tranContent.isEmpty()) {
                            translatedText.append(tranContent).append("\n");
                        }
                    }
                }
                
                String original = originalText.toString().trim();
                String translated = translatedText.toString().trim();
                
                // 应用文本预处理优化
                original = TranslationTextProcessor.preprocessText(original);
                translated = TranslationTextProcessor.formatTranslationResult(translated);
                
                postSuccess(callback, original, translated, renderedImage);
            } else {
                // 失败
                String errorMsg = getErrorMessage(errorCode);
                postError(callback, errorCode, errorMsg);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析响应失败", e);
            postError(callback, "-4", "解析结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取错误信息
     */
    private static String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case "101": return "缺少必填的参数";
            case "102": return "不支持的语言类型";
            case "103": return "翻译文本过长";
            case "104": return "不支持的API类型";
            case "105": return "不支持的签名类型";
            case "106": return "不支持的响应类型";
            case "107": return "不支持的传输加密类型";
            case "108": return "appKey无效";
            case "109": return "batchLog格式不正确";
            case "110": return "无相关服务的有效实例";
            case "111": return "开发者账号无效";
            case "113": return "q不能为空";
            case "201": return "解密失败";
            case "202": return "签名检验失败";
            case "203": return "访问IP地址不在可访问IP列表";
            case "301": return "辞典查询失败";
            case "302": return "翻译查询失败";
            case "303": return "服务端的其它异常";
            case "401": return "账户已经欠费";
            case "411": return "访问频率受限";
            default: return "未知错误: " + errorCode;
        }
    }
    
    /**
     * 在主线程发送成功回调
     */
    private static void postSuccess(TranslateCallback callback, String original, String translated, String renderedImage) {
        mainHandler.post(() -> callback.onSuccess(original, translated, renderedImage));
    }
    
    /**
     * 将Base64字符串转换为Bitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Base64转Bitmap失败", e);
            return null;
        }
    }
    
    /**
     * 在主线程发送错误回调
     */
    private static void postError(TranslateCallback callback, String errorCode, String errorMessage) {
        mainHandler.post(() -> callback.onError(errorCode, errorMessage));
    }
}

