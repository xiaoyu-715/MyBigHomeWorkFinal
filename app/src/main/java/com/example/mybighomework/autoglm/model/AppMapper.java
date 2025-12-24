package com.example.mybighomework.autoglm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用映射器 - 将应用名称映射到包名
 * 需求: 5.1-5.5
 * 
 * 支持:
 * - 中文应用名称（如"微信"、"抖音"）
 * - 英文应用名称（如"WeChat"、"YouTube"）
 * - 大小写不敏感匹配
 */
public class AppMapper {
    
    // 应用名称到包名的映射表
    private static final Map<String, String> APP_MAP = new HashMap<>();
    
    static {
        // ==================== 系统应用 ====================
        APP_MAP.put("设置", "com.android.settings");
        APP_MAP.put("Settings", "com.android.settings");
        APP_MAP.put("AndroidSystemSettings", "com.android.settings");
        APP_MAP.put("Android System Settings", "com.android.settings");
        
        APP_MAP.put("相机", "com.android.camera2");
        APP_MAP.put("Camera", "com.android.camera2");
        
        APP_MAP.put("电话", "com.google.android.dialer");
        APP_MAP.put("Phone", "com.google.android.dialer");
        
        APP_MAP.put("短信", "com.google.android.apps.messaging");
        APP_MAP.put("Messages", "com.google.android.apps.messaging");
        
        APP_MAP.put("相册", "com.google.android.apps.photos");
        APP_MAP.put("Photos", "com.google.android.apps.photos");
        
        APP_MAP.put("联系人", "com.google.android.contacts");
        APP_MAP.put("Contacts", "com.android.contacts");
        APP_MAP.put("GoogleContacts", "com.google.android.contacts");
        APP_MAP.put("Google Contacts", "com.google.android.contacts");
        
        APP_MAP.put("日历", "com.google.android.calendar");
        APP_MAP.put("Calendar", "com.google.android.calendar");
        APP_MAP.put("GoogleCalendar", "com.google.android.calendar");
        APP_MAP.put("Google Calendar", "com.google.android.calendar");
        
        APP_MAP.put("时钟", "com.google.android.deskclock");
        APP_MAP.put("Clock", "com.android.deskclock");
        APP_MAP.put("GoogleClock", "com.google.android.deskclock");
        APP_MAP.put("Google Clock", "com.google.android.deskclock");
        
        APP_MAP.put("计算器", "com.google.android.calculator");
        APP_MAP.put("Calculator", "com.google.android.calculator");
        
        APP_MAP.put("文件", "com.google.android.documentsui");
        APP_MAP.put("Files", "com.android.fileexplorer");
        APP_MAP.put("File Manager", "com.android.fileexplorer");
        APP_MAP.put("GoogleFiles", "com.google.android.apps.nbu.files");
        APP_MAP.put("Files by Google", "com.google.android.apps.nbu.files");
        
        APP_MAP.put("浏览器", "com.android.chrome");
        APP_MAP.put("Chrome", "com.android.chrome");
        APP_MAP.put("Google Chrome", "com.android.chrome");
        
        APP_MAP.put("应用商店", "com.android.vending");
        APP_MAP.put("Play Store", "com.android.vending");
        APP_MAP.put("GooglePlayStore", "com.android.vending");
        APP_MAP.put("Google Play Store", "com.android.vending");

        
        // ==================== 社交通讯（中国） ====================
        APP_MAP.put("微信", "com.tencent.mm");
        APP_MAP.put("WeChat", "com.tencent.mm");
        
        APP_MAP.put("QQ", "com.tencent.mobileqq");
        APP_MAP.put("QQ邮箱", "com.tencent.androidqqmail");
        
        APP_MAP.put("微博", "com.sina.weibo");
        APP_MAP.put("Weibo", "com.sina.weibo");
        
        APP_MAP.put("小红书", "com.xingin.xhs");
        APP_MAP.put("Xiaohongshu", "com.xingin.xhs");
        APP_MAP.put("RED", "com.xingin.xhs");
        
        APP_MAP.put("知乎", "com.zhihu.android");
        APP_MAP.put("Zhihu", "com.zhihu.android");
        
        APP_MAP.put("豆瓣", "com.douban.frodo");
        
        // ==================== 购物生活（中国） ====================
        APP_MAP.put("支付宝", "com.eg.android.AlipayGphone");
        APP_MAP.put("Alipay", "com.eg.android.AlipayGphone");
        
        APP_MAP.put("淘宝", "com.taobao.taobao");
        APP_MAP.put("Taobao", "com.taobao.taobao");
        
        APP_MAP.put("京东", "com.jingdong.app.mall");
        APP_MAP.put("JD", "com.jingdong.app.mall");
        
        APP_MAP.put("拼多多", "com.xunmeng.pinduoduo");
        APP_MAP.put("Pinduoduo", "com.xunmeng.pinduoduo");
        
        APP_MAP.put("美团", "com.sankuai.meituan");
        APP_MAP.put("Meituan", "com.sankuai.meituan");
        
        APP_MAP.put("饿了么", "me.ele");
        APP_MAP.put("Eleme", "me.ele");
        
        APP_MAP.put("大众点评", "com.dianping.v1");
        APP_MAP.put("Dianping", "com.dianping.v1");
        
        APP_MAP.put("闲鱼", "com.taobao.idlefish");
        APP_MAP.put("Xianyu", "com.taobao.idlefish");
        
        // ==================== 出行（中国） ====================
        APP_MAP.put("携程", "ctrip.android.view");
        APP_MAP.put("铁路12306", "com.MobileTicket");
        APP_MAP.put("12306", "com.MobileTicket");
        APP_MAP.put("去哪儿", "com.Qunar");
        APP_MAP.put("去哪儿旅行", "com.Qunar");
        APP_MAP.put("滴滴出行", "com.sdu.did.psnger");
        APP_MAP.put("高德地图", "com.autonavi.minimap");
        APP_MAP.put("Amap", "com.autonavi.minimap");
        APP_MAP.put("百度地图", "com.baidu.BaiduMap");
        APP_MAP.put("Baidu Map", "com.baidu.BaiduMap");
        
        // ==================== 娱乐（中国） ====================
        APP_MAP.put("抖音", "com.ss.android.ugc.aweme");
        APP_MAP.put("Douyin", "com.ss.android.ugc.aweme");
        
        APP_MAP.put("快手", "com.smile.gifmaker");
        APP_MAP.put("Kuaishou", "com.smile.gifmaker");
        
        APP_MAP.put("哔哩哔哩", "tv.danmaku.bili");
        APP_MAP.put("Bilibili", "tv.danmaku.bili");
        APP_MAP.put("B站", "tv.danmaku.bili");
        
        APP_MAP.put("网易云音乐", "com.netease.cloudmusic");
        APP_MAP.put("NetEase Music", "com.netease.cloudmusic");
        
        APP_MAP.put("QQ音乐", "com.tencent.qqmusic");
        APP_MAP.put("QQ Music", "com.tencent.qqmusic");
        
        APP_MAP.put("喜马拉雅", "com.ximalaya.ting.android");
        
        APP_MAP.put("爱奇艺", "com.qiyi.video");
        APP_MAP.put("iQIYI", "com.qiyi.video");
        
        APP_MAP.put("腾讯视频", "com.tencent.qqlive");
        APP_MAP.put("Tencent Video", "com.tencent.qqlive");
        
        APP_MAP.put("优酷", "com.youku.phone");
        APP_MAP.put("Youku", "com.youku.phone");
        APP_MAP.put("优酷视频", "com.youku.phone");
        
        APP_MAP.put("芒果TV", "com.hunantv.imgo.activity");

        
        // ==================== 阅读（中国） ====================
        APP_MAP.put("番茄小说", "com.dragon.read");
        APP_MAP.put("番茄免费小说", "com.dragon.read");
        APP_MAP.put("七猫免费小说", "com.kmxs.reader");
        APP_MAP.put("腾讯新闻", "com.tencent.news");
        APP_MAP.put("今日头条", "com.ss.android.article.news");
        
        // ==================== 办公工具（中国） ====================
        APP_MAP.put("钉钉", "com.alibaba.android.rimet");
        APP_MAP.put("DingTalk", "com.alibaba.android.rimet");
        APP_MAP.put("飞书", "com.ss.android.lark");
        APP_MAP.put("Feishu", "com.ss.android.lark");
        APP_MAP.put("Lark", "com.ss.android.lark");
        APP_MAP.put("豆包", "com.larus.nova");
        
        // ==================== 国际应用 ====================
        APP_MAP.put("YouTube", "com.google.android.youtube");
        
        APP_MAP.put("Gmail", "com.google.android.gm");
        APP_MAP.put("GoogleMail", "com.google.android.gm");
        APP_MAP.put("Google Mail", "com.google.android.gm");
        
        APP_MAP.put("Google Maps", "com.google.android.apps.maps");
        APP_MAP.put("Maps", "com.google.android.apps.maps");
        APP_MAP.put("地图", "com.google.android.apps.maps");
        APP_MAP.put("GoogleMaps", "com.google.android.apps.maps");
        
        APP_MAP.put("GoogleChat", "com.google.android.apps.dynamite");
        APP_MAP.put("Google Chat", "com.google.android.apps.dynamite");
        
        APP_MAP.put("GoogleDocs", "com.google.android.apps.docs.editors.docs");
        APP_MAP.put("Google Docs", "com.google.android.apps.docs.editors.docs");
        
        APP_MAP.put("Google Drive", "com.google.android.apps.docs");
        APP_MAP.put("GoogleDrive", "com.google.android.apps.docs");
        
        APP_MAP.put("GoogleFit", "com.google.android.apps.fitness");
        APP_MAP.put("GoogleKeep", "com.google.android.keep");
        
        APP_MAP.put("Twitter", "com.twitter.android");
        APP_MAP.put("X", "com.twitter.android");
        
        APP_MAP.put("Facebook", "com.facebook.katana");
        APP_MAP.put("Instagram", "com.instagram.android");
        APP_MAP.put("WhatsApp", "com.whatsapp");
        APP_MAP.put("Telegram", "org.telegram.messenger");
        APP_MAP.put("Spotify", "com.spotify.music");
        APP_MAP.put("Netflix", "com.netflix.mediaclient");
        APP_MAP.put("Amazon", "com.amazon.mShop.android.shopping");
        
        APP_MAP.put("TikTok", "com.zhiliaoapp.musically");
        APP_MAP.put("Duolingo", "com.duolingo");
        APP_MAP.put("Reddit", "com.reddit.frontpage");
        APP_MAP.put("Quora", "com.quora.android");
        APP_MAP.put("VLC", "org.videolan.vlc");
        APP_MAP.put("Booking", "com.booking");
        APP_MAP.put("Booking.com", "com.booking");
        APP_MAP.put("Expedia", "com.expedia.bookings");
        APP_MAP.put("Temu", "com.einnovation.temu");
        APP_MAP.put("Keep", "com.gotokeep.keep");
        
        // ==================== 本应用 ====================
        APP_MAP.put("考研英语备考", "com.example.mybighomework");
        APP_MAP.put("考研英语", "com.example.mybighomework");
        APP_MAP.put("英语学习", "com.example.mybighomework");
        APP_MAP.put("英语备考", "com.example.mybighomework");
        APP_MAP.put("MyBigHomeWork", "com.example.mybighomework");
    }
    
    /**
     * 获取应用包名
     * 需求: 5.1-5.5
     * 
     * @param appName 应用名称（支持中英文，大小写不敏感）
     * @return 包名，如果未找到返回null
     */
    public static String getPackageName(String appName) {
        if (appName == null || appName.trim().isEmpty()) {
            return null;
        }
        
        String trimmedName = appName.trim();
        
        // 1. 精确匹配
        String exactMatch = APP_MAP.get(trimmedName);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 2. 大小写不敏感匹配
        // 需求: 5.4
        for (Map.Entry<String, String> entry : APP_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmedName)) {
                return entry.getValue();
            }
        }
        
        // 3. 未找到匹配
        // 需求: 5.5
        return null;
    }
    
    /**
     * 检查应用是否在映射表中
     * 
     * @param appName 应用名称
     * @return 是否存在映射
     */
    public static boolean hasMapping(String appName) {
        return getPackageName(appName) != null;
    }
    
    /**
     * 获取所有支持的应用名称
     * 
     * @return 应用名称数组
     */
    public static String[] getSupportedAppNames() {
        return APP_MAP.keySet().toArray(new String[0]);
    }
}
