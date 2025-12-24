package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 词书实体类
 * 存储从 DictionaryData 数据集导入的词书信息
 * 
 * 对应 book.csv 数据
 * 支持层级结构：level=1 为顶级分类，level=2 为具体词书
 */
@Entity(
    tableName = "books",
    indices = {
        @Index("parentId"),      // 父分类索引（用于层级查询）
        @Index("level"),         // 等级索引（用于筛选顶级/子级）
        @Index("bookOrder")      // 排序索引
    }
)
public class BookEntity {
    
    @PrimaryKey
    @NonNull
    private String id;              // 词书ID
    
    private String parentId;        // 父分类ID（"0"表示顶级）
    private int level;              // 等级: 1=顶级分类, 2=具体词书
    private float bookOrder;        // 排序
    private String name;            // 书名
    private int itemNum;            // 单词总数
    private int directItemNum;      // 直接单词数
    private String author;          // 作者
    private String fullName;        // 完整书名
    private String comment;         // 描述
    private String organization;    // 组织
    private String publisher;       // 出版社
    private String version;         // 版本
    private String flag;            // 标记
    
    // 默认构造函数（Room需要）
    public BookEntity() {
        this.id = "";
    }
    
    @Ignore
    public BookEntity(@NonNull String id, String parentId, int level, float bookOrder,
                      String name, int itemNum, int directItemNum, String author,
                      String fullName, String comment, String organization,
                      String publisher, String version, String flag) {
        this.id = id;
        this.parentId = parentId;
        this.level = level;
        this.bookOrder = bookOrder;
        this.name = name;
        this.itemNum = itemNum;
        this.directItemNum = directItemNum;
        this.author = author;
        this.fullName = fullName;
        this.comment = comment;
        this.organization = organization;
        this.publisher = publisher;
        this.version = version;
        this.flag = flag;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public float getBookOrder() { return bookOrder; }
    public void setBookOrder(float bookOrder) { this.bookOrder = bookOrder; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getItemNum() { return itemNum; }
    public void setItemNum(int itemNum) { this.itemNum = itemNum; }
    
    public int getDirectItemNum() { return directItemNum; }
    public void setDirectItemNum(int directItemNum) { this.directItemNum = directItemNum; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    
    /**
     * 是否为顶级分类
     */
    public boolean isTopLevel() {
        return level == 1 || "0".equals(parentId);
    }
    
    /**
     * 是否为具体词书（可以学习的）
     */
    public boolean isLeafBook() {
        return level == 2 || directItemNum > 0;
    }
    
    /**
     * 获取显示名称（优先完整名称）
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return name;
    }
    
    /**
     * 获取作者和出版社信息
     */
    public String getAuthorInfo() {
        StringBuilder sb = new StringBuilder();
        if (author != null && !author.isEmpty()) {
            sb.append(author);
        }
        if (publisher != null && !publisher.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(publisher);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "BookEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", itemNum=" + itemNum +
                '}';
    }
}
