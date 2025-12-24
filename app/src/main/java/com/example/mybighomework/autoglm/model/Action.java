package com.example.mybighomework.autoglm.model;

/**
 * 动作基类 - 定义所有支持的操作类型
 * 需求: 2.1-2.9
 * 
 * 所有AI返回的操作指令都会被解析为Action的子类实例
 */
public abstract class Action {
    
    /**
     * 获取动作类型名称
     */
    public abstract String getActionType();
    
    /**
     * 获取动作描述（用于UI显示）
     */
    public abstract String getDescription();
    
    // ==================== 点击类动作 ====================
    
    /**
     * 点击动作
     * 需求: 2.1
     */
    public static class Tap extends Action {
        public final int x;
        public final int y;
        
        public Tap(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String getActionType() {
            return "Tap";
        }
        
        @Override
        public String getDescription() {
            return "点击位置 (" + x + ", " + y + ")";
        }
    }
    
    /**
     * 双击动作
     * 需求: 2.1 (扩展)
     */
    public static class DoubleTap extends Action {
        public final int x;
        public final int y;
        
        public DoubleTap(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String getActionType() {
            return "DoubleTap";
        }
        
        @Override
        public String getDescription() {
            return "双击位置 (" + x + ", " + y + ")";
        }
    }
    
    /**
     * 长按动作
     * 需求: 2.7
     */
    public static class LongPress extends Action {
        public final int x;
        public final int y;
        public final long durationMs;
        
        public LongPress(int x, int y) {
            this(x, y, 1000); // 默认长按1秒
        }
        
        public LongPress(int x, int y, long durationMs) {
            this.x = x;
            this.y = y;
            this.durationMs = durationMs;
        }
        
        @Override
        public String getActionType() {
            return "LongPress";
        }
        
        @Override
        public String getDescription() {
            return "长按位置 (" + x + ", " + y + ")";
        }
    }

    
    // ==================== 滑动动作 ====================
    
    /**
     * 滑动动作
     * 需求: 2.2
     */
    public static class Swipe extends Action {
        public final int startX;
        public final int startY;
        public final int endX;
        public final int endY;
        public final long durationMs;
        
        public Swipe(int startX, int startY, int endX, int endY) {
            this(startX, startY, endX, endY, 500); // 默认滑动500ms
        }
        
        public Swipe(int startX, int startY, int endX, int endY, long durationMs) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.durationMs = durationMs;
        }
        
        @Override
        public String getActionType() {
            return "Swipe";
        }
        
        @Override
        public String getDescription() {
            return "滑动 (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + ")";
        }
    }
    
    // ==================== 输入类动作 ====================
    
    /**
     * 文本输入动作
     * 需求: 2.3
     */
    public static class Type extends Action {
        public final String text;
        
        public Type(String text) {
            this.text = text;
        }
        
        @Override
        public String getActionType() {
            return "Type";
        }
        
        @Override
        public String getDescription() {
            String displayText = text.length() > 20 ? text.substring(0, 20) + "..." : text;
            return "输入文本: " + displayText;
        }
    }
    
    // ==================== 应用控制动作 ====================
    
    /**
     * 启动应用动作
     * 需求: 2.4
     */
    public static class Launch extends Action {
        public final String appName;
        
        public Launch(String appName) {
            this.appName = appName;
        }
        
        @Override
        public String getActionType() {
            return "Launch";
        }
        
        @Override
        public String getDescription() {
            return "启动应用: " + appName;
        }
    }
    
    /**
     * 返回动作
     * 需求: 2.5
     */
    public static class Back extends Action {
        @Override
        public String getActionType() {
            return "Back";
        }
        
        @Override
        public String getDescription() {
            return "返回上一页";
        }
    }
    
    /**
     * 回到桌面动作
     * 需求: 2.6
     */
    public static class Home extends Action {
        @Override
        public String getActionType() {
            return "Home";
        }
        
        @Override
        public String getDescription() {
            return "回到桌面";
        }
    }
    
    // ==================== 控制类动作 ====================
    
    /**
     * 等待动作
     * 需求: 2.8
     */
    public static class Wait extends Action {
        public final long durationMs;
        
        public Wait(long durationMs) {
            this.durationMs = durationMs;
        }
        
        @Override
        public String getActionType() {
            return "Wait";
        }
        
        @Override
        public String getDescription() {
            return "等待 " + (durationMs / 1000.0) + " 秒";
        }
    }
    
    /**
     * 任务完成动作
     * 需求: 2.9
     */
    public static class Finish extends Action {
        public final String message;
        
        public Finish(String message) {
            this.message = message;
        }
        
        @Override
        public String getActionType() {
            return "Finish";
        }
        
        @Override
        public String getDescription() {
            return "任务完成: " + message;
        }
    }
    
    /**
     * 错误动作 - 表示解析或执行过程中出现错误
     */
    public static class Error extends Action {
        public final String reason;
        
        public Error(String reason) {
            this.reason = reason;
        }
        
        @Override
        public String getActionType() {
            return "Error";
        }
        
        @Override
        public String getDescription() {
            return "错误: " + reason;
        }
    }
}
