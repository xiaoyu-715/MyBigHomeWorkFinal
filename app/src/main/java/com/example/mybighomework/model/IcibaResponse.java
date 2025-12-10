package com.example.mybighomework.model;

import com.google.gson.annotations.SerializedName;

/**
 * 金山词霸每日一句API响应模型
 * API地址: http://open.iciba.com/dsapi/
 */
public class IcibaResponse {
    
    @SerializedName("sid")
    private String sid;                    // 句子ID
    
    @SerializedName("tts")
    private String tts;                    // 音频URL
    
    @SerializedName("content")
    private String content;                // 英文句子
    
    @SerializedName("note")
    private String note;                   // 中文翻译
    
    @SerializedName("love")
    private String love;                   // 点赞数
    
    @SerializedName("translation")
    private String translation;            // 出处/作者
    
    @SerializedName("picture")
    private String picture;                // 图片URL
    
    @SerializedName("picture2")
    private String picture2;               // 背景图片URL
    
    @SerializedName("caption")
    private String caption;                // 标题
    
    @SerializedName("dateline")
    private String dateline;               // 日期 (yyyy-MM-dd格式)
    
    @SerializedName("s_pv")
    private String sPv;                    // 分享PV
    
    @SerializedName("sp_pv")
    private String spPv;                   // 特殊PV
    
    @SerializedName("fenxiang_img")
    private String fenxiangImg;            // 分享图片URL
    
    // 构造函数
    public IcibaResponse() {
    }
    
    // Getters and Setters
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid;
    }
    
    public String getTts() {
        return tts;
    }
    
    public void setTts(String tts) {
        this.tts = tts;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getLove() {
        return love;
    }
    
    public void setLove(String love) {
        this.love = love;
    }
    
    public String getTranslation() {
        return translation;
    }
    
    public void setTranslation(String translation) {
        this.translation = translation;
    }
    
    public String getPicture() {
        return picture;
    }
    
    public void setPicture(String picture) {
        this.picture = picture;
    }
    
    public String getPicture2() {
        return picture2;
    }
    
    public void setPicture2(String picture2) {
        this.picture2 = picture2;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getDateline() {
        return dateline;
    }
    
    public void setDateline(String dateline) {
        this.dateline = dateline;
    }
    
    public String getsPv() {
        return sPv;
    }
    
    public void setsPv(String sPv) {
        this.sPv = sPv;
    }
    
    public String getSpPv() {
        return spPv;
    }
    
    public void setSpPv(String spPv) {
        this.spPv = spPv;
    }
    
    public String getFenxiangImg() {
        return fenxiangImg;
    }
    
    public void setFenxiangImg(String fenxiangImg) {
        this.fenxiangImg = fenxiangImg;
    }
    
    @Override
    public String toString() {
        return "IcibaResponse{" +
                "sid='" + sid + '\'' +
                ", content='" + content + '\'' +
                ", note='" + note + '\'' +
                ", translation='" + translation + '\'' +
                ", dateline='" + dateline + '\'' +
                '}';
    }
}

