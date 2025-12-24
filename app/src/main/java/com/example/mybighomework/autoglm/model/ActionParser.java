package com.example.mybighomework.autoglm.model;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动作解析器 - 解析AI返回的文本，提取操作指令
 * 需求: 2.1-2.10
 * 
 * 支持的格式:
 * - do(action="Tap", element=[x,y])
 * - do(action="Swipe", start=[x1,y1], end=[x2,y2])
 * - do(action="Type", text="xxx")
 * - do(action="Launch", app="xxx")
 * - do(action="Back")
 * - do(action="Home")
 * - do(action="Long Press", element=[x,y])
 * - do(action="Wait", duration="x seconds")
 * - finish(message="xxx")
 */
public class ActionParser {
    
    private static final String TAG = "ActionParser";
    
    // 正则表达式模式
    private static final Pattern FINISH_PATTERN = Pattern.compile(
            "finish\\s*\\(\\s*message\\s*=\\s*[\"'](.*?)[\"']\\s*\\)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern DO_PATTERN = Pattern.compile(
            "do\\s*\\((.*)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern STRING_PARAM_PATTERN = Pattern.compile(
            "(\\w+)\\s*=\\s*[\"'](.*?)[\"']"
    );
    
    private static final Pattern LIST_PARAM_PATTERN = Pattern.compile(
            "(\\w+)\\s*=\\s*[\\[\\(]([\\d\\s,.]+)[\\]\\)]"
    );
    
    /**
     * 解析AI响应为Action对象
     * 需求: 2.1-2.10
     * 
     * @param response AI返回的响应文本
     * @param screenWidth 屏幕宽度（像素）
     * @param screenHeight 屏幕高度（像素）
     * @return 解析后的Action对象
     */
    public static Action parse(String response, int screenWidth, int screenHeight) {
        if (response == null || response.trim().isEmpty()) {
            return new Action.Error("空响应");
        }
        
        String cleanResponse = response.trim();
        Log.d(TAG, "解析响应: " + cleanResponse);
        
        // 1. 尝试匹配 finish(message="...")
        Matcher finishMatcher = FINISH_PATTERN.matcher(cleanResponse);
        if (finishMatcher.find()) {
            String message = finishMatcher.group(1);
            Log.d(TAG, "解析为Finish动作: " + message);
            return new Action.Finish(message);
        }
        
        // 2. 尝试匹配 do(action="...", ...)
        Matcher doMatcher = DO_PATTERN.matcher(cleanResponse);
        if (doMatcher.find()) {
            String args = doMatcher.group(1);
            Map<String, Object> params = parseParams(args);
            
            Object actionTypeObj = params.get("action");
            if (actionTypeObj == null) {
                return new Action.Error("缺少action类型");
            }
            
            String actionType = actionTypeObj.toString().toLowerCase();
            Log.d(TAG, "解析动作类型: " + actionType);
            
            return parseActionByType(actionType, params, screenWidth, screenHeight);
        }
        
        // 3. 回退：如果不是已知命令，作为对话完成处理
        Log.d(TAG, "无法解析为已知动作，作为Finish处理");
        return new Action.Finish(cleanResponse);
    }

    
    /**
     * 根据动作类型解析具体动作
     */
    private static Action parseActionByType(String actionType, Map<String, Object> params, 
                                            int screenWidth, int screenHeight) {
        switch (actionType) {
            case "tap":
                return parseTapAction(params, screenWidth, screenHeight);
                
            case "double tap":
                return parseDoubleTapAction(params, screenWidth, screenHeight);
                
            case "long press":
                return parseLongPressAction(params, screenWidth, screenHeight);
                
            case "swipe":
                return parseSwipeAction(params, screenWidth, screenHeight);
                
            case "type":
                return parseTypeAction(params);
                
            case "launch":
                return parseLaunchAction(params);
                
            case "back":
                return new Action.Back();
                
            case "home":
                return new Action.Home();
                
            case "wait":
                return parseWaitAction(params);
                
            default:
                return new Action.Error("未知动作类型: " + actionType);
        }
    }
    
    /**
     * 解析点击动作
     * 需求: 2.1, 2.10
     */
    @SuppressWarnings("unchecked")
    private static Action parseTapAction(Map<String, Object> params, int screenWidth, int screenHeight) {
        Object element = params.get("element");
        if (element instanceof List) {
            List<Float> coords = (List<Float>) element;
            int[] absCoords = convertCoordinates(coords, screenWidth, screenHeight);
            if (absCoords != null) {
                Log.d(TAG, "Tap动作 - 相对坐标: " + coords + ", 绝对坐标: (" + absCoords[0] + ", " + absCoords[1] + ")");
                return new Action.Tap(absCoords[0], absCoords[1]);
            }
        }
        return new Action.Error("Tap动作坐标无效");
    }
    
    /**
     * 解析双击动作
     */
    @SuppressWarnings("unchecked")
    private static Action parseDoubleTapAction(Map<String, Object> params, int screenWidth, int screenHeight) {
        Object element = params.get("element");
        if (element instanceof List) {
            List<Float> coords = (List<Float>) element;
            int[] absCoords = convertCoordinates(coords, screenWidth, screenHeight);
            if (absCoords != null) {
                return new Action.DoubleTap(absCoords[0], absCoords[1]);
            }
        }
        return new Action.Error("DoubleTap动作坐标无效");
    }
    
    /**
     * 解析长按动作
     * 需求: 2.7, 2.10
     */
    @SuppressWarnings("unchecked")
    private static Action parseLongPressAction(Map<String, Object> params, int screenWidth, int screenHeight) {
        Object element = params.get("element");
        if (element instanceof List) {
            List<Float> coords = (List<Float>) element;
            int[] absCoords = convertCoordinates(coords, screenWidth, screenHeight);
            if (absCoords != null) {
                return new Action.LongPress(absCoords[0], absCoords[1]);
            }
        }
        return new Action.Error("LongPress动作坐标无效");
    }
    
    /**
     * 解析滑动动作
     * 需求: 2.2, 2.10
     */
    @SuppressWarnings("unchecked")
    private static Action parseSwipeAction(Map<String, Object> params, int screenWidth, int screenHeight) {
        Object startObj = params.get("start");
        Object endObj = params.get("end");
        
        if (startObj instanceof List && endObj instanceof List) {
            List<Float> start = (List<Float>) startObj;
            List<Float> end = (List<Float>) endObj;
            
            if (start.size() >= 2 && end.size() >= 2) {
                int startX = convertCoordinate(start.get(0), screenWidth);
                int startY = convertCoordinate(start.get(1), screenHeight);
                int endX = convertCoordinate(end.get(0), screenWidth);
                int endY = convertCoordinate(end.get(1), screenHeight);
                
                Log.d(TAG, "Swipe动作 - 起点: (" + startX + ", " + startY + "), 终点: (" + endX + ", " + endY + ")");
                return new Action.Swipe(startX, startY, endX, endY);
            }
        }
        return new Action.Error("Swipe动作坐标无效");
    }
    
    /**
     * 解析输入动作
     * 需求: 2.3
     */
    private static Action parseTypeAction(Map<String, Object> params) {
        Object textObj = params.get("text");
        if (textObj != null) {
            String text = textObj.toString();
            Log.d(TAG, "Type动作 - 文本: " + text);
            return new Action.Type(text);
        }
        return new Action.Error("Type动作缺少文本");
    }
    
    /**
     * 解析启动应用动作
     * 需求: 2.4
     */
    private static Action parseLaunchAction(Map<String, Object> params) {
        Object appObj = params.get("app");
        if (appObj != null) {
            String appName = appObj.toString();
            Log.d(TAG, "Launch动作 - 应用: " + appName);
            return new Action.Launch(appName);
        }
        return new Action.Error("Launch动作缺少应用名称");
    }
    
    /**
     * 解析等待动作
     * 需求: 2.8
     */
    private static Action parseWaitAction(Map<String, Object> params) {
        Object durationObj = params.get("duration");
        long durationMs = 1000; // 默认1秒
        
        if (durationObj != null) {
            String durationStr = durationObj.toString()
                    .replace("seconds", "")
                    .replace("second", "")
                    .replace("秒", "")
                    .trim();
            try {
                double seconds = Double.parseDouble(durationStr);
                durationMs = (long) (seconds * 1000);
            } catch (NumberFormatException e) {
                Log.w(TAG, "无法解析等待时间: " + durationObj);
            }
        }
        
        Log.d(TAG, "Wait动作 - 时长: " + durationMs + "ms");
        return new Action.Wait(durationMs);
    }

    
    /**
     * 坐标转换：从相对值(0-999)转换为绝对像素值
     * 需求: 2.10
     * 
     * 公式: absX = relX / 1000 * screenWidth
     *       absY = relY / 1000 * screenHeight
     * 
     * @param coords 相对坐标列表 [x, y] 或 [y1, x1, y2, x2] (box格式)
     * @param screenWidth 屏幕宽度
     * @param screenHeight 屏幕高度
     * @return 绝对坐标数组 [x, y]，如果无效返回null
     */
    private static int[] convertCoordinates(List<Float> coords, int screenWidth, int screenHeight) {
        if (coords == null || coords.isEmpty()) {
            return null;
        }
        
        if (coords.size() >= 2 && coords.size() < 4) {
            // 标准格式 [x, y]
            float relX = coords.get(0);
            float relY = coords.get(1);
            int absX = convertCoordinate(relX, screenWidth);
            int absY = convertCoordinate(relY, screenHeight);
            return new int[]{absX, absY};
        } else if (coords.size() == 4) {
            // Box格式 [y1, x1, y2, x2] -> 点击中心
            float y1 = coords.get(0);
            float x1 = coords.get(1);
            float y2 = coords.get(2);
            float x2 = coords.get(3);
            
            float centerX = (x1 + x2) / 2;
            float centerY = (y1 + y2) / 2;
            
            int absX = convertCoordinate(centerX, screenWidth);
            int absY = convertCoordinate(centerY, screenHeight);
            return new int[]{absX, absY};
        }
        
        return null;
    }
    
    /**
     * 单个坐标转换
     * 需求: 2.10
     */
    private static int convertCoordinate(float relativeValue, int screenDimension) {
        return (int) (relativeValue / 1000f * screenDimension);
    }
    
    /**
     * 解析响应中的思考和动作部分
     * 用于历史记录和日志
     * 
     * @param content AI响应内容
     * @return Pair<思考部分, 动作部分>
     */
    public static Pair<String, String> parseResponseParts(String content) {
        if (content == null || content.isEmpty()) {
            return new Pair<>("", "");
        }
        
        // 规则1: 检查 finish(message=
        if (content.contains("finish(message=")) {
            String[] parts = content.split("finish\\(message=", 2);
            String thinking = parts[0].trim();
            String action = "finish(message=" + parts[1];
            return new Pair<>(thinking, action);
        }
        
        // 规则2: 检查 do(action=
        if (content.contains("do(action=")) {
            String[] parts = content.split("do\\(action=", 2);
            String thinking = parts[0].trim();
            String action = "do(action=" + parts[1];
            return new Pair<>(thinking, action);
        }
        
        // 规则3: 回退到旧版XML标签解析
        if (content.contains("<answer>")) {
            String[] parts = content.split("<answer>", 2);
            String thinking = parts[0]
                    .replace("<think>", "")
                    .replace("</think>", "")
                    .trim();
            String action = parts[1]
                    .replace("</answer>", "")
                    .trim();
            return new Pair<>(thinking, action);
        }
        
        // 规则4: 没有找到标记，返回内容作为动作
        return new Pair<>("", content);
    }
    
    /**
     * 解析参数字符串为Map
     * 
     * @param args 参数字符串，如 action="Tap", element=[100, 200]
     * @return 参数Map
     */
    private static Map<String, Object> parseParams(String args) {
        Map<String, Object> result = new HashMap<>();
        
        // 解析字符串参数: key="value" 或 key='value'
        Matcher stringMatcher = STRING_PARAM_PATTERN.matcher(args);
        while (stringMatcher.find()) {
            String key = stringMatcher.group(1);
            String value = stringMatcher.group(2);
            result.put(key, value);
        }
        
        // 解析列表参数: key=[123, 456] 或 key=(123, 456)
        Matcher listMatcher = LIST_PARAM_PATTERN.matcher(args);
        while (listMatcher.find()) {
            String key = listMatcher.group(1);
            String listStr = listMatcher.group(2);
            List<Float> list = parseNumberList(listStr);
            result.put(key, list);
        }
        
        return result;
    }
    
    /**
     * 解析数字列表字符串
     */
    private static List<Float> parseNumberList(String listStr) {
        List<Float> list = new ArrayList<>();
        String[] parts = listStr.split(",");
        for (String part : parts) {
            try {
                float value = Float.parseFloat(part.trim());
                list.add(value);
            } catch (NumberFormatException e) {
                // 忽略无效数字
            }
        }
        return list;
    }
}
