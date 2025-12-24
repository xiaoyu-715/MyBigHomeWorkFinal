# 词汇训练模块设计文档

## 1. 系统架构设计

### 1.1 整体架构
采用MVVM(Model-View-ViewModel)架构模式:
```
┌─────────────────────────────────────────────────────────┐
│                        View Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ BookList     │  │ BookDetail   │  │ Vocabulary   │  │
│  │ Activity     │  │ Activity     │  │ Activity     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────┐
│                     ViewModel Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ BookList     │  │ BookDetail   │  │ Vocabulary   │  │
│  │ ViewModel    │  │ ViewModel    │  │ ViewModel    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────┐
│                    Repository Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Book         │  │ Learning     │  │ Task         │  │
│  │ Repository   │  │ Repository   │  │ Repository   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Room         │  │ SharedPref   │  │ Network      │  │
│  │ Database     │  │              │  │ (TTS API)    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 1.2 模块划分

#### 1.2.1 UI模块
- **BookListActivity**: 词书列表界面
- **BookDetailActivity**: 词书详情界面(已存在,需改造)
- **VocabularyActivity**: 词汇训练界面(已存在,需扩展)
- **Adapters**: RecyclerView适配器

#### 1.2.2 ViewModel模块
- **BookListViewModel**: 词书列表数据管理
- **BookDetailViewModel**: 词书详情数据管理
- **VocabularyViewModel**: 词汇训练数据管理(已存在)

#### 1.2.3 Repository模块
- **BookRepository**: 词书数据仓库(已存在,需扩展)
- **LearningProgressRepository**: 学习进度数据仓库(已存在)
- **TaskProgressRepository**: 任务进度数据仓库

#### 1.2.4 Database模块
- **AppDatabase**: Room数据库(已存在)
- **DAOs**: 数据访问对象
- **Entities**: 数据实体

#### 1.2.5 Utils模块
- **TaskProgressTracker**: 任务进度追踪器(已存在)
- **AudioPlayer**: 音频播放器
- **QuestionGenerator**: 题目生成器

## 2. 数据库设计

### 2.1 表结构设计

#### 2.1.1 books表(已存在)
```sql
CREATE TABLE books (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT,
    level INTEGER DEFAULT 1,
    parent_id TEXT DEFAULT '0',
    word_count INTEGER DEFAULT 0,
    icon_url TEXT,
    created_at INTEGER,
    updated_at INTEGER
);
```

#### 2.1.2 dictionary_words表(已存在)
```sql
CREATE TABLE dictionary_words (
    id TEXT PRIMARY KEY NOT NULL,
    word TEXT NOT NULL,
    phonetic TEXT,
    meaning TEXT,
    word_class TEXT,
    example TEXT,
    translation TEXT,
    audio_url TEXT
);
```

#### 2.1.3 book_word_relations表(已存在)
```sql
CREATE TABLE book_word_relations (
    id TEXT PRIMARY KEY NOT NULL,
    book_id TEXT NOT NULL,
    word_id TEXT NOT NULL,
    word_order INTEGER DEFAULT 0,
    flag TEXT,
    tag TEXT,
    FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY(word_id) REFERENCES dictionary_words(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_book_word ON book_word_relations(book_id, word_id);
```

#### 2.1.4 word_learning_progress表(已存在)
```sql
CREATE TABLE word_learning_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT DEFAULT 'default_user',
    book_id TEXT NOT NULL,
    word_id TEXT NOT NULL,
    status INTEGER DEFAULT 0,  -- 0=未学习,1=学习中,2=已掌握
    proficiency INTEGER DEFAULT 0,  -- 熟练度0-100
    learn_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    wrong_count INTEGER DEFAULT 0,
    last_learn_time INTEGER,
    next_review_time INTEGER,
    created_at INTEGER,
    updated_at INTEGER,
    FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY(word_id) REFERENCES dictionary_words(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_user_book_word ON word_learning_progress(user_id, book_id, word_id);
```

#### 2.1.5 study_records表(已存在)
```sql
CREATE TABLE study_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT DEFAULT 'default_user',
    book_id TEXT,
    study_date TEXT NOT NULL,
    word_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    wrong_count INTEGER DEFAULT 0,
    accuracy REAL DEFAULT 0.0,
    study_duration INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    created_at INTEGER
);
```

#### 2.1.6 wrong_questions表(已存在)
```sql
CREATE TABLE wrong_questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT DEFAULT 'default_user',
    word_id TEXT NOT NULL,
    user_answer TEXT,
    correct_answer TEXT,
    wrong_time INTEGER,
    wrong_count INTEGER DEFAULT 1,
    FOREIGN KEY(word_id) REFERENCES dictionary_words(id) ON DELETE CASCADE
);
```

#### 2.1.7 daily_tasks表(已存在)
```sql
CREATE TABLE daily_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_id INTEGER DEFAULT 0,
    task_content TEXT NOT NULL,
    task_category TEXT,
    is_completed INTEGER DEFAULT 0,
    action_type TEXT,  -- vocabulary_training
    completion_type TEXT,  -- count/simple
    target_count INTEGER DEFAULT 0,
    current_progress INTEGER DEFAULT 0,
    task_date TEXT NOT NULL
);
```

### 2.2 DAO接口设计

#### 2.2.1 BookDao(已存在,需扩展)
```java
@Dao
public interface BookDao {
    // 已有方法
    @Query("SELECT * FROM books WHERE id = :bookId")
    BookEntity getBookByIdSync(String bookId);
    
    @Query("SELECT * FROM books WHERE level = 1 OR parent_id = '0'")
    LiveData<List<BookEntity>> getTopLevelBooks();
    
    // 新增方法
    @Query("SELECT * FROM books WHERE parent_id != '0' AND parent_id != '' ORDER BY name")
    LiveData<List<BookEntity>> getAllLearnableBooks();
    
    @Query("SELECT COUNT(*) FROM book_word_relations WHERE book_id = :bookId")
    int getWordCountByBookId(String bookId);
    
    @Query("SELECT b.*, " +
           "(SELECT COUNT(*) FROM word_learning_progress wlp " +
           "WHERE wlp.book_id = b.id AND wlp.status >= 1) as learned_count " +
           "FROM books b WHERE b.parent_id != '0' ORDER BY b.name")
    LiveData<List<BookWithProgress>> getBooksWithProgress();
}
```

#### 2.2.2 DictionaryWordDao(已存在)
```java
@Dao
public interface DictionaryWordDao {
    @Query("SELECT * FROM dictionary_words WHERE id = :wordId")
    DictionaryWordEntity getWordByIdSync(String wordId);
    
    @Query("SELECT dw.* FROM dictionary_words dw " +
           "INNER JOIN book_word_relations bwr ON dw.id = bwr.word_id " +
           "WHERE bwr.book_id = :bookId ORDER BY bwr.word_order")
    List<DictionaryWordEntity> getWordsByBookIdSync(String bookId);
}
```

#### 2.2.3 WordLearningProgressDao(已存在,需扩展)
```java
@Dao
public interface WordLearningProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WordLearningProgressEntity progress);
    
    @Update
    void update(WordLearningProgressEntity progress);
    
    // 获取词书的学习进度
    @Query("SELECT * FROM word_learning_progress " +
           "WHERE book_id = :bookId AND user_id = :userId")
    List<WordLearningProgressEntity> getProgressByBookId(String bookId, String userId);
    
    // 获取未学习的单词ID列表
    @Query("SELECT word_id FROM book_word_relations " +
           "WHERE book_id = :bookId AND word_id NOT IN " +
           "(SELECT word_id FROM word_learning_progress " +
           "WHERE book_id = :bookId AND user_id = :userId)")
    List<String> getUnlearnedWordIds(String bookId, String userId);
    
    // 获取需要复习的单词ID列表
    @Query("SELECT word_id FROM word_learning_progress " +
           "WHERE book_id = :bookId AND user_id = :userId " +
           "AND next_review_time <= :currentTime " +
           "ORDER BY next_review_time LIMIT :limit")
    List<String> getReviewWordIds(String bookId, String userId, long currentTime, int limit);
    
    // 获取学习统计
    @Query("SELECT COUNT(*) FROM word_learning_progress " +
           "WHERE book_id = :bookId AND user_id = :userId AND status = :status")
    int getCountByStatus(String bookId, String userId, int status);
}
```

## 3. 界面设计

### 3.1 词书列表界面(BookListActivity)

#### 3.1.1 布局结构
```xml
<LinearLayout orientation="vertical">
    <!-- 顶部标题栏 -->
    <Toolbar>
        <TextView text="选择词书" />
        <ImageView id="btn_search" />
    </Toolbar>
    
    <!-- 搜索和筛选 -->
    <LinearLayout>
        <EditText id="et_search" hint="搜索词书" />
        <Spinner id="spinner_category" />
    </LinearLayout>
    
    <!-- 词书列表 -->
    <RecyclerView id="rv_books" />
    
    <!-- 底部导航 -->
    <BottomNavigationView />
</LinearLayout>
```

#### 3.1.2 词书卡片布局
```xml
<CardView>
    <LinearLayout orientation="horizontal">
        <!-- 词书图标 -->
        <ImageView id="iv_book_icon" />
        
        <LinearLayout orientation="vertical">
            <!-- 词书名称 -->
            <TextView id="tv_book_name" />
            
            <!-- 词书信息 -->
            <TextView id="tv_book_info" />
            
            <!-- 进度条 -->
            <ProgressBar id="progress_bar" />
            
            <!-- 进度文字 -->
            <TextView id="tv_progress" />
        </LinearLayout>
    </LinearLayout>
</CardView>
```

### 3.2 词书详情界面(BookDetailActivity)

#### 3.2.1 布局改造
保持现有布局,添加以下功能:
- 显示学习模式选择(新词学习/复习/随机练习)
- 显示学习进度详细统计
- 优化操作按钮

### 3.3 词汇训练界面(VocabularyActivity)

#### 3.3.1 布局改造
保持现有布局,优化以下部分:
- 添加词书名称显示
- 优化进度显示
- 优化结果反馈

## 4. 核心功能设计

### 4.1 词书数据加载

#### 4.1.1 BookRepository扩展
```java
public class BookRepository {
    private BookDao bookDao;
    private BookWordRelationDao relationDao;
    private DictionaryWordDao wordDao;
    
    // 获取所有可学习的词书(带进度)
    public LiveData<List<BookWithProgress>> getBooksWithProgress() {
        return bookDao.getBooksWithProgress();
    }
    
    // 获取词书的单词列表
    public List<DictionaryWordEntity> getWordsForBookSync(String bookId) {
        return wordDao.getWordsByBookIdSync(bookId);
    }
    
    // 获取词书统计信息
    public BookStatistics getBookStatistics(String bookId, String userId) {
        int totalWords = relationDao.getWordCountByBookId(bookId);
        int learnedWords = progressDao.getCountByStatus(bookId, userId, 1) +
                          progressDao.getCountByStatus(bookId, userId, 2);
        int masteredWords = progressDao.getCountByStatus(bookId, userId, 2);
        
        return new BookStatistics(totalWords, learnedWords, masteredWords);
    }
}
```

### 4.2 单词学习流程

#### 4.2.1 学习模式选择
```java
public enum LearningMode {
    NEW_WORDS,      // 新词学习
    REVIEW,         // 复习模式
    RANDOM_PRACTICE // 随机练习
}
```

#### 4.2.2 单词筛选逻辑
```java
public class WordSelector {
    // 选择新词
    public List<String> selectNewWords(String bookId, String userId, int count) {
        List<String> unlearnedIds = progressDao.getUnlearnedWordIds(bookId, userId);
        Collections.shuffle(unlearnedIds);
        return unlearnedIds.subList(0, Math.min(count, unlearnedIds.size()));
    }
    
    // 选择复习词
    public List<String> selectReviewWords(String bookId, String userId, int count) {
        long currentTime = System.currentTimeMillis();
        return progressDao.getReviewWordIds(bookId, userId, currentTime, count);
    }
    
    // 随机选择
    public List<String> selectRandomWords(String bookId, int count) {
        List<String> allWordIds = relationDao.getWordIdsByBookId(bookId);
        Collections.shuffle(allWordIds);
        return allWordIds.subList(0, Math.min(count, allWordIds.size()));
    }
}
```

### 4.3 题目生成

#### 4.3.1 QuestionGenerator设计
```java
public class QuestionGenerator {
    // 生成选择题
    public VocabularyQuestion generateQuestion(
            DictionaryWordEntity targetWord,
            List<DictionaryWordEntity> allWords) {
        
        // 正确答案
        String correctAnswer = targetWord.getMeaning();
        
        // 生成3个干扰项
        List<String> distractors = generateDistractors(targetWord, allWords, 3);
        
        // 组合选项并打乱
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.addAll(distractors);
        Collections.shuffle(options);
        
        // 找到正确答案的索引
        int correctIndex = options.indexOf(correctAnswer);
        
        return new VocabularyQuestion(
            targetWord.getWord(),
            targetWord.getPhonetic(),
            targetWord.getMeaning(),
            options.toArray(new String[0]),
            correctIndex
        );
    }
    
    // 生成干扰项
    private List<String> generateDistractors(
            DictionaryWordEntity targetWord,
            List<DictionaryWordEntity> allWords,
            int count) {
        
        List<String> distractors = new ArrayList<>();
        List<DictionaryWordEntity> candidates = new ArrayList<>(allWords);
        candidates.remove(targetWord);
        Collections.shuffle(candidates);
        
        for (int i = 0; i < Math.min(count, candidates.size()); i++) {
            distractors.add(candidates.get(i).getMeaning());
        }
        
        return distractors;
    }
}
```

### 4.4 学习进度管理

#### 4.4.1 ProgressManager设计
```java
public class ProgressManager {
    // 更新学习进度
    public void updateProgress(String bookId, String wordId, boolean isCorrect) {
        WordLearningProgressEntity progress = 
            progressDao.getProgress(bookId, "default_user", wordId);
        
        if (progress == null) {
            // 创建新记录
            progress = new WordLearningProgressEntity();
            progress.setBookId(bookId);
            progress.setWordId(wordId);
            progress.setUserId("default_user");
        }
        
        // 更新统计
        progress.setLearnCount(progress.getLearnCount() + 1);
        if (isCorrect) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        } else {
            progress.setWrongCount(progress.getWrongCount() + 1);
        }
        
        // 更新熟练度
        updateProficiency(progress, isCorrect);
        
        // 更新状态
        updateStatus(progress);
        
        // 计算下次复习时间
        calculateNextReviewTime(progress);
        
        // 保存
        progress.setLastLearnTime(System.currentTimeMillis());
        progress.setUpdatedAt(System.currentTimeMillis());
        progressDao.insertOrUpdate(progress);
    }
    
    // 更新熟练度
    private void updateProficiency(WordLearningProgressEntity progress, boolean isCorrect) {
        int current = progress.getProficiency();
        if (isCorrect) {
            progress.setProficiency(Math.min(100, current + 10));
        } else {
            progress.setProficiency(Math.max(0, current - 5));
        }
    }
    
    // 更新状态
    private void updateStatus(WordLearningProgressEntity progress) {
        int proficiency = progress.getProficiency();
        if (proficiency >= 80) {
            progress.setStatus(2); // 已掌握
        } else if (proficiency > 0) {
            progress.setStatus(1); // 学习中
        } else {
            progress.setStatus(0); // 未学习
        }
    }
    
    // 计算下次复习时间(基于遗忘曲线)
    private void calculateNextReviewTime(WordLearningProgressEntity progress) {
        long now = System.currentTimeMillis();
        int learnCount = progress.getLearnCount();
        
        // 艾宾浩斯遗忘曲线: 5分钟、30分钟、12小时、1天、2天、4天、7天、15天
        long[] intervals = {
            5 * 60 * 1000L,      // 5分钟
            30 * 60 * 1000L,     // 30分钟
            12 * 60 * 60 * 1000L, // 12小时
            24 * 60 * 60 * 1000L, // 1天
            2 * 24 * 60 * 60 * 1000L,  // 2天
            4 * 24 * 60 * 60 * 1000L,  // 4天
            7 * 24 * 60 * 60 * 1000L,  // 7天
            15 * 24 * 60 * 60 * 1000L  // 15天
        };
        
        int index = Math.min(learnCount - 1, intervals.length - 1);
        long nextReviewTime = now + intervals[index];
        progress.setNextReviewTime(nextReviewTime);
    }
}
```

### 4.5 任务进度同步

#### 4.5.1 集成TaskProgressTracker
```java
public class VocabularyActivity extends AppCompatActivity {
    private void onAnswerCorrect() {
        // 更新得分和统计
        score += 10;
        correctAnswers++;
        
        // 更新学习进度
        progressManager.updateProgress(bookId, currentWord.getId(), true);
        
        // 同步任务进度
        TaskProgressTracker.getInstance(this)
            .recordProgress("vocabulary_training", 1);
        
        // 显示结果
        showResult(true);
    }
}
```

### 4.6 音频播放

#### 4.6.1 AudioPlayer设计
```java
public class AudioPlayer {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    
    public void playWordPronunciation(String word, AudioCallback callback) {
        if (isPlaying) {
            callback.onError("正在播放中");
            return;
        }
        
        try {
            releaseMediaPlayer();
            mediaPlayer = new MediaPlayer();
            
            // 有道词典API
            String url = "https://dict.youdao.com/dictvoice?audio=" + 
                        URLEncoder.encode(word, "UTF-8") + "&type=1";
            
            mediaPlayer.setDataSource(url);
            
            mediaPlayer.setOnPreparedListener(mp -> {
                isPlaying = true;
                callback.onStart();
                mp.start();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                callback.onComplete();
                releaseMediaPlayer();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPlaying = false;
                callback.onError("播放失败");
                releaseMediaPlayer();
                return true;
            });
            
            mediaPlayer.prepareAsync();
            
        } catch (Exception e) {
            callback.onError("播放失败: " + e.getMessage());
        }
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                // Ignore
            }
            mediaPlayer = null;
        }
    }
    
    public interface AudioCallback {
        void onStart();
        void onComplete();
        void onError(String message);
    }
}
```

## 5. 数据流设计

### 5.1 学习流程数据流
```
用户选择词书
    ↓
查询词书信息和单词列表
    ↓
根据学习模式筛选单词
    ↓
生成题目列表
    ↓
显示当前题目
    ↓
用户答题
    ↓
判断对错
    ↓
├─ 答对: 更新进度(+熟练度) → 同步任务进度(+1)
└─ 答错: 更新进度(-熟练度) → 记录错题
    ↓
显示下一题 / 显示完成界面
    ↓
保存学习记录
```

### 5.2 任务同步数据流
```
答对一题
    ↓
TaskProgressTracker.recordProgress("vocabulary_training", 1)
    ↓
查询今日任务(actionType=vocabulary_training, date=今天)
    ↓
更新任务进度(currentProgress++)
    ↓
检查是否达到目标(currentProgress >= targetCount)
    ↓
是 → 标记任务完成(isCompleted=true)
否 → 继续
```

## 6. 异常处理设计

### 6.1 数据加载异常
- 词书列表为空: 显示空状态提示
- 单词列表为空: 提示"该词书暂无单词"
- 数据库异常: 显示错误提示,记录日志

### 6.2 网络异常
- 发音加载失败: 提示"发音暂时不可用"
- 超时处理: 设置5秒超时

### 6.3 用户操作异常
- 重复点击: 防抖处理
- 快速切换: 取消未完成的操作

## 7. 性能优化设计

### 7.1 数据加载优化
- 词书列表分页加载
- 单词数据预加载
- 图片懒加载

### 7.2 数据库优化
- 添加必要的索引
- 使用事务批量操作
- 异步数据库操作

### 7.3 内存优化
- 及时释放MediaPlayer
- 使用ViewHolder模式
- 避免内存泄漏

## 8. 测试设计

### 8.1 单元测试
- Repository层测试
- ViewModel层测试
- 工具类测试

### 8.2 集成测试
- 数据库操作测试
- 任务同步测试
- 学习流程测试

### 8.3 UI测试
- 界面显示测试
- 交互流程测试
- 异常情况测试

## 9. 实施计划

### 9.1 阶段划分
- **阶段1**: 数据库和Repository层改造
- **阶段2**: VocabularyActivity扩展
- **阶段3**: 词书列表界面开发
- **阶段4**: 集成测试和优化
- **阶段5**: 上线和监控

### 9.2 风险控制
- 每个阶段完成后进行测试
- 保留数据备份
- 分支开发,主分支保持稳定
