# 单词搜索功能设计文档YSJ

## 1. 系统架构设计

### 1.1 整体架构
```
┌─────────────────────────────────────────┐
│         WordSearchActivityYSJ           │
│  ┌───────────────────────────────────┐  │
│  │      UI Layer (View)              │  │
│  │  - SearchView                     │  │
│  │  - TabLayout                      │  │
│  │  - RecyclerView (例句列表)        │  │
│  │  - TextView (释义)                │  │
│  └───────────────────────────────────┘  │
│              ↕                          │
│  ┌───────────────────────────────────┐  │
│  │   ViewModel Layer                 │  │
│  │   WordSearchViewModelYSJ          │  │
│  │  - LiveData<DictionaryWordEntity> │  │
│  │  - LiveData<List<ExampleSentence>>│  │
│  └───────────────────────────────────┘  │
│              ↕                          │
│  ┌───────────────────────────────────┐  │
│  │   Repository Layer                │  │
│  │   DictionaryWordRepository        │  │
│  │   ExampleSentenceRepository       │  │
│  └───────────────────────────────────┘  │
│              ↕                          │
│  ┌───────────────────────────────────┐  │
│  │   Database Layer (Room)           │  │
│  │  - DictionaryWordDao              │  │
│  │  - ExampleSentenceDao             │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 1.2 MVVM模式应用
- **Model**: `DictionaryWordEntity`, `ExampleSentenceEntity`
- **View**: `WordSearchActivityYSJ`, XML布局文件
- **ViewModel**: `WordSearchViewModelYSJ`

## 2. 数据库设计

### 2.1 例句表设计
```sql
CREATE TABLE example_sentences (
    id TEXT PRIMARY KEY NOT NULL,
    wordId TEXT NOT NULL,
    englishSentence TEXT NOT NULL,
    chineseSentence TEXT NOT NULL,
    source TEXT,
    difficulty INTEGER DEFAULT 5,
    category TEXT,
    FOREIGN KEY(wordId) REFERENCES dictionary_words(id)
);

CREATE INDEX idx_example_wordId ON example_sentences(wordId);
CREATE INDEX idx_example_difficulty ON example_sentences(difficulty);
```

### 2.2 生词本表设计
```sql
CREATE TABLE user_word_collection (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    wordId TEXT NOT NULL,
    userId TEXT DEFAULT 'default',
    collectedAt INTEGER NOT NULL,
    note TEXT,
    FOREIGN KEY(wordId) REFERENCES dictionary_words(id),
    UNIQUE(wordId, userId)
);

CREATE INDEX idx_collection_userId ON user_word_collection(userId);
CREATE INDEX idx_collection_time ON user_word_collection(collectedAt);
```

### 2.3 搜索历史表设计
```sql
CREATE TABLE search_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    keyword TEXT NOT NULL,
    searchTime INTEGER NOT NULL,
    userId TEXT DEFAULT 'default'
);

CREATE INDEX idx_search_time ON search_history(searchTime DESC);
CREATE INDEX idx_search_user ON search_history(userId);
```

## 3. 类设计

### 3.1 Activity类
```java
public class WordSearchActivityYSJ extends AppCompatActivity {
    // UI组件
    private SearchView searchView;
    private TabLayout tabLayout;
    private TextView tvWord;
    private TextView tvPhonetic;
    private TextView tvTranslation;
    private RecyclerView rvExamples;
    private Button btnAddToCollection;
    
    // ViewModel
    private WordSearchViewModelYSJ viewModel;
    
    // Adapter
    private ExampleSentenceAdapter exampleAdapter;
    
    // 方法
    @Override
    protected void onCreate(Bundle savedInstanceState);
    private void initViews();
    private void initViewModel();
    private void setupSearchView();
    private void setupTabLayout();
    private void observeData();
    private void searchWord(String keyword);
    private void displayWordDetail(DictionaryWordEntity word);
    private void addToCollection();
    private void playPronunciation(String word, boolean isUK);
}
```

### 3.2 ViewModel类
```java
public class WordSearchViewModelYSJ extends ViewModel {
    // Repository
    private DictionaryWordRepository wordRepository;
    private ExampleSentenceRepository sentenceRepository;
    private UserWordCollectionRepository collectionRepository;
    
    // LiveData
    private MutableLiveData<DictionaryWordEntity> currentWord;
    private MutableLiveData<List<ExampleSentenceEntity>> examples;
    private MutableLiveData<List<String>> searchSuggestions;
    private MutableLiveData<Boolean> isInCollection;
    
    // 方法
    public void searchWord(String keyword);
    public void loadExamples(String wordId);
    public void addToCollection(String wordId);
    public void removeFromCollection(String wordId);
    public void checkCollectionStatus(String wordId);
    public LiveData<DictionaryWordEntity> getCurrentWord();
    public LiveData<List<ExampleSentenceEntity>> getExamples();
}
```

### 3.3 Entity类

#### ExampleSentenceEntity
```java
@Entity(tableName = "example_sentences")
public class ExampleSentenceEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String wordId;
    private String englishSentence;
    private String chineseSentence;
    private String source;
    private int difficulty;
    private String category;
    
    // Getters and Setters
}
```

#### UserWordCollectionEntity
```java
@Entity(tableName = "user_word_collection")
public class UserWordCollectionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String wordId;
    private String userId;
    private long collectedAt;
    private String note;
    
    // Getters and Setters
}
```

### 3.4 Adapter类
```java
public class ExampleSentenceAdapter extends RecyclerView.Adapter<ExampleSentenceAdapter.ViewHolder> {
    private List<ExampleSentenceEntity> sentences;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onCollectClick(ExampleSentenceEntity sentence);
        void onCopyClick(ExampleSentenceEntity sentence);
        void onPlayClick(ExampleSentenceEntity sentence);
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish;
        TextView tvChinese;
        TextView tvSource;
        ImageButton btnCollect;
        ImageButton btnCopy;
        ImageButton btnPlay;
    }
}
```

## 4. 界面布局设计

### 4.1 主布局文件 (activity_word_search_ysj.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    
    <!-- 顶部工具栏 -->
    <com.google.android.material.appbar.AppBarLayout>
        <androidx.appcompat.widget.Toolbar>
            <!-- 返回按钮 -->
            <ImageView android:id="@+id/btn_back"/>
            
            <!-- 搜索框 -->
            <androidx.appcompat.widget.SearchView 
                android:id="@+id/search_view"/>
            
            <!-- 更多按钮 -->
            <ImageView android:id="@+id/btn_more"/>
        </androidx.appcompat.widget.Toolbar>
        
        <!-- 标签页 -->
        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tab_layout"/>
    </com.google.android.material.appbar.AppBarLayout>
    
    <!-- 主内容区域 -->
    <androidx.core.widget.NestedScrollView>
        <LinearLayout android:orientation="vertical">
            
            <!-- 单词显示区域 -->
            <androidx.cardview.widget.CardView>
                <LinearLayout>
                    <TextView 
                        android:id="@+id/tv_word"
                        android:textSize="32sp"
                        android:fontFamily="华文中宋"/>
                    
                    <TextView 
                        android:id="@+id/tv_phonetic"
                        android:textSize="16sp"/>
                    
                    <Button 
                        android:id="@+id/btn_add_collection"
                        android:text="+ 生词本"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            
            <!-- 释义/例句切换 -->
            <com.google.android.material.tabs.TabLayout
                android:id="@+id/tab_content"/>
            
            <!-- ViewPager2 内容区域 -->
            <androidx.viewpager2.widget.ViewPager2
                android:id="@+id/view_pager"/>
            
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 4.2 例句列表项布局 (item_example_sentence.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView>
    <LinearLayout android:orientation="vertical">
        
        <!-- 序号 -->
        <TextView 
            android:id="@+id/tv_number"
            android:text="1."/>
        
        <!-- 英文例句 -->
        <TextView 
            android:id="@+id/tv_english"
            android:textSize="16sp"/>
        
        <!-- 中文翻译 -->
        <TextView 
            android:id="@+id/tv_chinese"
            android:textSize="14sp"
            android:fontFamily="华文中宋"/>
        
        <!-- 来源 -->
        <TextView 
            android:id="@+id/tv_source"
            android:textSize="12sp"/>
        
        <!-- 操作按钮 -->
        <LinearLayout android:orientation="horizontal">
            <ImageButton 
                android:id="@+id/btn_collect"
                android:src="@drawable/ic_star"/>
            
            <TextView android:text="收藏"/>
            
            <ImageButton 
                android:id="@+id/btn_copy"
                android:src="@drawable/ic_copy"/>
            
            <TextView android:text="复制"/>
            
            <ImageButton 
                android:id="@+id/btn_play"
                android:src="@drawable/ic_volume"/>
            
            <TextView android:text="朗读"/>
        </LinearLayout>
        
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

## 5. 数据流程设计

### 5.1 搜索流程
```
用户输入关键词
    ↓
SearchView.onQueryTextChange
    ↓
防抖处理 (300ms)
    ↓
ViewModel.searchWord(keyword)
    ↓
Repository.searchWordsSync(keyword)
    ↓
Dao.searchWords(keyword)
    ↓
返回搜索结果 List<DictionaryWordEntity>
    ↓
LiveData更新
    ↓
UI显示搜索建议
    ↓
用户点击选择
    ↓
显示单词详情
```

### 5.2 单词详情加载流程
```
用户选择单词
    ↓
ViewModel.loadWordDetail(wordId)
    ↓
并行执行:
  ├─ Repository.getWordById(wordId)
  ├─ Repository.getExamples(wordId)
  └─ Repository.checkCollection(wordId)
    ↓
LiveData更新
    ↓
UI显示:
  ├─ 单词基本信息
  ├─ 释义
  ├─ 例句列表
  └─ 生词本状态
```

### 5.3 生词本添加流程
```
用户点击"生词本"按钮
    ↓
检查是否已收藏
    ↓
如果未收藏:
  ViewModel.addToCollection(wordId)
    ↓
  Repository.addToCollection(wordId)
    ↓
  Dao.insert(UserWordCollectionEntity)
    ↓
  更新UI状态
    ↓
  显示Toast提示
如果已收藏:
  提示"已在生词本中"
```

## 6. 工具类设计

### 6.1 TextToSpeechHelper
```java
public class TextToSpeechHelper {
    private TextToSpeech tts;
    private Context context;
    
    public void init(Context context);
    public void speak(String text, Locale locale);
    public void speakWord(String word, boolean isUK);
    public void speakSentence(String sentence);
    public void stop();
    public void release();
}
```

### 6.2 SearchDebouncer
```java
public class SearchDebouncer {
    private Handler handler;
    private Runnable runnable;
    private long delayMillis;
    
    public SearchDebouncer(long delayMillis);
    public void debounce(Runnable action);
    public void cancel();
}
```

### 6.3 ClipboardHelper
```java
public class ClipboardHelper {
    public static void copyText(Context context, String text);
    public static void copyText(Context context, String label, String text);
    public static void showCopyToast(Context context);
}
```

## 7. 样式设计

### 7.1 颜色资源 (colors.xml)
```xml
<color name="word_search_primary">#2196F3</color>
<color name="word_search_accent">#FF9800</color>
<color name="word_search_background">#F5F5F5</color>
<color name="word_search_card">#FFFFFF</color>
<color name="word_search_text_primary">#333333</color>
<color name="word_search_text_secondary">#666666</color>
<color name="word_search_divider">#E0E0E0</color>
```

### 7.2 尺寸资源 (dimens.xml)
```xml
<dimen name="word_text_size">32sp</dimen>
<dimen name="phonetic_text_size">16sp</dimen>
<dimen name="translation_text_size">18sp</dimen>
<dimen name="example_english_size">16sp</dimen>
<dimen name="example_chinese_size">14sp</dimen>
<dimen name="card_margin">16dp</dimen>
<dimen name="card_padding">16dp</dimen>
```

### 7.3 字体样式 (styles.xml)
```xml
<style name="WordTextStyle">
    <item name="android:textSize">32sp</item>
    <item name="android:textColor">@color/word_search_text_primary</item>
    <item name="android:fontFamily">华文中宋</item>
    <item name="android:shadowColor">#40000000</item>
    <item name="android:shadowDx">2</item>
    <item name="android:shadowDy">2</item>
    <item name="android:shadowRadius">4</item>
</style>

<style name="TranslationTextStyle">
    <item name="android:textSize">18sp</item>
    <item name="android:textColor">@color/word_search_text_primary</item>
    <item name="android:fontFamily">华文中宋</item>
    <item name="android:shadowColor">#40000000</item>
    <item name="android:shadowDx">1</item>
    <item name="android:shadowDy">1</item>
    <item name="android:shadowRadius">2</item>
</style>
```

## 8. 性能优化方案

### 8.1 搜索优化
- 使用防抖机制，减少数据库查询
- 限制搜索结果数量（最多50条）
- 使用索引优化查询速度

### 8.2 列表优化
- RecyclerView使用ViewHolder模式
- 使用DiffUtil优化列表更新
- 分页加载例句（每页10条）

### 8.3 内存优化
- 使用弱引用缓存最近查看的单词
- 及时释放TTS资源
- 图片使用合适的尺寸

### 8.4 数据库优化
- 使用事务批量操作
- 合理使用索引
- 异步执行数据库操作

## 9. 异常处理

### 9.1 搜索异常
- 无搜索结果：显示"未找到相关单词"
- 数据库错误：显示"搜索失败，请重试"

### 9.2 加载异常
- 网络错误：显示"网络连接失败"
- 数据加载失败：显示重试按钮

### 9.3 TTS异常
- TTS初始化失败：禁用朗读按钮
- 朗读失败：显示Toast提示

## 10. 测试计划

### 10.1 单元测试
- ViewModel逻辑测试
- Repository数据操作测试
- 工具类功能测试

### 10.2 UI测试
- 搜索功能测试
- 详情显示测试
- 按钮交互测试

### 10.3 集成测试
- 完整流程测试
- 数据一致性测试
- 性能测试

## 11. 开发时间估算

| 任务 | 预计时间 |
|------|---------|
| 数据库表设计与创建 | 2小时 |
| Entity和Dao开发 | 2小时 |
| Repository开发 | 2小时 |
| ViewModel开发 | 3小时 |
| Activity开发 | 4小时 |
| 布局文件开发 | 3小时 |
| Adapter开发 | 2小时 |
| 工具类开发 | 2小时 |
| 功能测试与调试 | 4小时 |
| UI优化与美化 | 2小时 |
| **总计** | **26小时** |

## 12. 风险评估

### 12.1 技术风险
- **例句数据缺失**：当前数据库可能没有例句数据
  - 解决方案：先使用模拟数据，后期集成API

- **TTS兼容性**：不同设备TTS效果不同
  - 解决方案：提供在线TTS备选方案

### 12.2 性能风险
- **搜索性能**：7万单词搜索可能较慢
  - 解决方案：优化索引，使用全文搜索

- **内存占用**：大量例句加载可能占用内存
  - 解决方案：分页加载，及时释放

## 13. 后续迭代计划

### 版本1.0（当前版本）
- 基础搜索功能
- 单词详情展示
- 简单例句显示

### 版本1.1
- AI释义集成
- 更丰富的例句来源
- 笔记功能

### 版本1.2
- 单词卡片学习
- 语音搜索
- 离线例句包

### 版本2.0
- AI解词分析
- 智能推荐
- 社区分享
