# 需求文档：词汇数据源集成与单词书功能

## 简介

本功能旨在将 DictionaryData 高质量英语词典数据集集成到现有的词汇训练功能中，并开发完整的单词书管理功能。该数据集包含从小学到出国考试的完整词汇体系，涵盖约10万+单词和100+词书分类。

## 术语表

- **Dictionary_Data**: DictionaryData 文件夹中的高质量英语词典数据集
- **Word_Entity**: 单词实体，包含单词、音标、词频、难度等信息
- **Translation_Entity**: 单词翻译实体，包含单词和中文释义
- **Book_Entity**: 词书实体，包含词书名称、分类、单词数量等信息
- **Book_Word_Relation**: 词书与单词的关联关系
- **Vocabulary_Training_System**: 现有的词汇训练系统
- **Data_Importer**: 数据导入器，负责将CSV数据导入到Room数据库
- **Book_Browser**: 单词书浏览器，用于浏览和选择词书
- **Learning_Progress_Tracker**: 学习进度追踪器

## 需求

### 需求 1：数据源导入

**用户故事：** 作为开发者，我希望将 DictionaryData 数据集导入到应用数据库中，以便为词汇训练提供丰富的数据源。

#### 验收标准

1. WHEN 应用首次启动 THEN Data_Importer SHALL 检测数据库是否已初始化
2. WHEN 数据库未初始化 THEN Data_Importer SHALL 从 assets 目录读取 CSV 文件并解析
3. WHEN 解析 word.csv 文件 THEN Data_Importer SHALL 正确提取单词ID、单词、英式音标、美式音标、词频、难度、认识率字段
4. WHEN 解析 word_translation.csv 文件 THEN Data_Importer SHALL 正确提取单词和中文翻译字段
5. WHEN 解析 book.csv 文件 THEN Data_Importer SHALL 正确提取词书ID、父分类ID、等级、书名、单词数量、作者、出版社等字段
6. WHEN 解析 relation_book_word.csv 文件 THEN Data_Importer SHALL 正确提取词书-单词关联关系
7. IF 数据导入过程中发生错误 THEN Data_Importer SHALL 记录错误日志并继续处理剩余数据
8. WHEN 数据导入完成 THEN Data_Importer SHALL 在数据库中标记初始化完成状态
9. THE Data_Importer SHALL 支持增量更新，避免重复导入已存在的数据

### 需求 2：单词数据实体设计

**用户故事：** 作为开发者，我希望设计合理的数据库实体结构，以便高效存储和查询词汇数据。

#### 验收标准

1. THE Word_Entity SHALL 包含以下字段：id、word、phonetic_uk、phonetic_us、frequency、difficulty、acknowledge_rate
2. THE Translation_Entity SHALL 包含以下字段：id、word、translation
3. THE Book_Entity SHALL 包含以下字段：id、parent_id、level、order、name、item_num、author、full_name、comment、organization、publisher、version、flag
4. THE Book_Word_Relation SHALL 包含以下字段：id、book_id、word_id、flag、tag、order
5. WHEN 查询单词时 THE Word_Entity SHALL 支持通过单词、难度、词频进行筛选
6. WHEN 查询词书时 THE Book_Entity SHALL 支持通过父分类ID进行层级查询
7. THE 数据库 SHALL 为常用查询字段建立索引以优化性能

### 需求 3：单词书浏览功能

**用户故事：** 作为用户，我希望能够浏览和选择不同的单词书，以便根据自己的学习需求选择合适的词汇范围。

#### 验收标准

1. WHEN 用户进入单词书页面 THEN Book_Browser SHALL 显示顶级词书分类列表
2. WHEN 用户点击某个分类 THEN Book_Browser SHALL 显示该分类下的子分类或具体词书
3. WHEN 显示词书列表 THEN Book_Browser SHALL 展示词书名称、单词数量、作者信息
4. WHEN 用户选择某本词书 THEN Book_Browser SHALL 显示该词书的详细信息和单词预览
5. THE Book_Browser SHALL 支持搜索功能，允许用户按名称搜索词书
6. WHEN 用户长按词书 THEN Book_Browser SHALL 显示快捷操作菜单（开始学习、查看详情、添加到学习计划）
7. THE Book_Browser SHALL 显示用户对每本词书的学习进度

### 需求 4：词书学习功能

**用户故事：** 作为用户，我希望能够选择特定词书进行学习，以便系统化地掌握目标词汇。

#### 验收标准

1. WHEN 用户选择词书开始学习 THEN Vocabulary_Training_System SHALL 从该词书中加载单词
2. WHEN 加载词书单词 THEN Vocabulary_Training_System SHALL 按照词书中的顺序或随机顺序呈现
3. WHEN 用户完成一个单词的学习 THEN Learning_Progress_Tracker SHALL 记录该单词的学习状态
4. WHEN 用户退出学习 THEN Learning_Progress_Tracker SHALL 保存当前学习进度
5. WHEN 用户再次进入同一词书 THEN Vocabulary_Training_System SHALL 从上次学习位置继续
6. THE Vocabulary_Training_System SHALL 支持设置每次学习的单词数量（10/20/30/50个）
7. WHEN 显示单词 THEN Vocabulary_Training_System SHALL 展示单词、音标、中文释义、词频、难度等信息

### 需求 5：学习进度追踪

**用户故事：** 作为用户，我希望能够查看我的词汇学习进度，以便了解自己的学习情况。

#### 验收标准

1. WHEN 用户查看词书详情 THEN Learning_Progress_Tracker SHALL 显示已学习/总单词数
2. WHEN 用户查看词书详情 THEN Learning_Progress_Tracker SHALL 显示已掌握/未掌握单词数
3. THE Learning_Progress_Tracker SHALL 计算并显示词书学习完成百分比
4. WHEN 用户完成词书学习 THEN Learning_Progress_Tracker SHALL 显示学习完成提示
5. THE Learning_Progress_Tracker SHALL 支持查看每个单词的学习历史（正确次数、错误次数、最后学习时间）
6. WHEN 用户查看学习报告 THEN Learning_Progress_Tracker SHALL 显示按词书分类的学习统计

### 需求 6：智能复习功能

**用户故事：** 作为用户，我希望系统能够智能推荐需要复习的单词，以便巩固记忆。

#### 验收标准

1. THE Vocabulary_Training_System SHALL 基于艾宾浩斯遗忘曲线计算单词复习时间
2. WHEN 单词到达复习时间 THEN Vocabulary_Training_System SHALL 将其加入复习队列
3. WHEN 用户选择复习模式 THEN Vocabulary_Training_System SHALL 优先展示需要复习的单词
4. WHEN 用户答对复习单词 THEN Learning_Progress_Tracker SHALL 延长下次复习间隔
5. WHEN 用户答错复习单词 THEN Learning_Progress_Tracker SHALL 缩短下次复习间隔
6. THE Vocabulary_Training_System SHALL 显示今日需要复习的单词数量

### 需求 7：单词发音功能

**用户故事：** 作为用户，我希望能够听到单词的标准发音，以便学习正确的读音。

#### 验收标准

1. WHEN 用户点击发音按钮 THEN Vocabulary_Training_System SHALL 播放单词的英式或美式发音
2. THE Vocabulary_Training_System SHALL 支持切换英式/美式发音
3. IF 网络不可用 THEN Vocabulary_Training_System SHALL 显示友好的错误提示
4. WHEN 发音正在播放 THEN Vocabulary_Training_System SHALL 禁用发音按钮防止重复点击
5. THE Vocabulary_Training_System SHALL 支持自动播放发音设置

### 需求 8：词汇训练模式增强

**用户故事：** 作为用户，我希望有多种训练模式可选，以便用不同方式巩固词汇。

#### 验收标准

1. THE Vocabulary_Training_System SHALL 支持"看词选义"模式（显示单词，选择中文释义）
2. THE Vocabulary_Training_System SHALL 支持"看义选词"模式（显示中文释义，选择单词）
3. THE Vocabulary_Training_System SHALL 支持"听音选词"模式（播放发音，选择单词）
4. THE Vocabulary_Training_System SHALL 支持"拼写模式"（显示中文释义，用户输入单词）
5. WHEN 用户选择训练模式 THEN Vocabulary_Training_System SHALL 按所选模式呈现题目
6. THE Vocabulary_Training_System SHALL 记录每种模式的训练统计数据

### 需求 9：数据同步与备份

**用户故事：** 作为用户，我希望我的学习数据能够被安全保存，以便在重装应用后恢复。

#### 验收标准

1. THE Learning_Progress_Tracker SHALL 将学习进度保存到本地数据库
2. THE Learning_Progress_Tracker SHALL 支持导出学习数据到JSON文件
3. THE Learning_Progress_Tracker SHALL 支持从JSON文件导入学习数据
4. IF 导入数据与现有数据冲突 THEN Learning_Progress_Tracker SHALL 提供合并或覆盖选项
5. WHEN 数据导出完成 THEN Learning_Progress_Tracker SHALL 显示导出文件路径

### 需求 10：性能优化

**用户故事：** 作为用户，我希望应用能够流畅运行，即使处理大量词汇数据。

#### 验收标准

1. WHEN 加载词书列表 THEN Book_Browser SHALL 使用分页加载，每页最多50条
2. WHEN 搜索单词 THEN Vocabulary_Training_System SHALL 在500ms内返回结果
3. THE Data_Importer SHALL 使用批量插入优化数据导入性能
4. THE 数据库查询 SHALL 使用索引优化，避免全表扫描
5. WHEN 应用启动 THEN Data_Importer SHALL 在后台线程执行数据初始化，不阻塞UI
