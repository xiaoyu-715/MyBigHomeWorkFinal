# 词汇训练模块任务清单

## 任务概述
根据需求文档和设计文档,将所有词汇学习功能整合到词汇训练模块中。

## 阶段一: 数据库和Repository层改造

### 1.1 数据库表结构验证和优化 ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 验证所有必需的表是否存在
- [ ] 检查表结构是否符合设计要求
- [ ] 添加缺失的索引
- [ ] 验证外键约束

**验收标准**:
- 所有表结构正确
- 索引创建成功
- 外键约束正常工作

---

### 1.2 扩展BookDao ⏳
**优先级**: 高
**预计时间**: 1小时
**任务内容**:
- [ ] 添加 `getAllLearnableBooks()` 方法
- [ ] 添加 `getBooksWithProgress()` 方法
- [ ] 创建 `BookWithProgress` 数据类
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/database/dao/BookDao.java`
- `app/src/main/java/com/example/mybighomework/database/entity/BookWithProgress.java`

**验收标准**:
- 方法正确返回词书列表
- 包含学习进度信息
- 单元测试通过

---

### 1.3 扩展WordLearningProgressDao ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 添加 `getUnlearnedWordIds()` 方法
- [ ] 添加 `getReviewWordIds()` 方法
- [ ] 添加 `getCountByStatus()` 方法
- [ ] 添加 `insertOrUpdate()` 方法
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/database/dao/WordLearningProgressDao.java`

**验收标准**:
- 方法正确返回数据
- 支持遗忘曲线查询
- 单元测试通过

---

### 1.4 扩展BookRepository ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 添加 `getBooksWithProgress()` 方法
- [ ] 添加 `getBookStatistics()` 方法
- [ ] 创建 `BookStatistics` 数据类
- [ ] 优化现有方法
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/database/repository/BookRepository.java`
- `app/src/main/java/com/example/mybighomework/model/BookStatistics.java`

**验收标准**:
- 方法正确返回数据
- 统计信息准确
- 单元测试通过

---

### 1.5 创建WordSelector工具类 ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 创建 `WordSelector` 类
- [ ] 实现 `selectNewWords()` 方法
- [ ] 实现 `selectReviewWords()` 方法
- [ ] 实现 `selectRandomWords()` 方法
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/WordSelector.java`

**验收标准**:
- 正确筛选新词
- 正确筛选复习词
- 支持随机选择
- 单元测试通过

---

### 1.6 创建QuestionGenerator工具类 ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 创建 `QuestionGenerator` 类
- [ ] 实现 `generateQuestion()` 方法
- [ ] 实现 `generateDistractors()` 方法
- [ ] 创建 `VocabularyQuestion` 数据类
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/QuestionGenerator.java`
- `app/src/main/java/com/example/mybighomework/model/VocabularyQuestion.java`

**验收标准**:
- 正确生成选择题
- 干扰项合理
- 选项随机排列
- 单元测试通过

---

### 1.7 创建ProgressManager工具类 ⏳
**优先级**: 高
**预计时间**: 3小时
**任务内容**:
- [ ] 创建 `ProgressManager` 类
- [ ] 实现 `updateProgress()` 方法
- [ ] 实现 `updateProficiency()` 方法
- [ ] 实现 `updateStatus()` 方法
- [ ] 实现 `calculateNextReviewTime()` 方法(遗忘曲线)
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/ProgressManager.java`

**验收标准**:
- 正确更新学习进度
- 熟练度计算准确
- 遗忘曲线实现正确
- 单元测试通过

---

### 1.8 优化AudioPlayer工具类 ⏳
**优先级**: 中
**预计时间**: 1小时
**任务内容**:
- [ ] 创建独立的 `AudioPlayer` 类
- [ ] 实现 `playWordPronunciation()` 方法
- [ ] 添加回调接口
- [ ] 添加错误处理
- [ ] 编写单元测试

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/AudioPlayer.java`

**验收标准**:
- 发音播放正常
- 错误处理完善
- 资源正确释放

---

## 阶段二: VocabularyActivity扩展

### 2.1 添加必要的导入和字段 ✅
**优先级**: 高
**预计时间**: 0.5小时
**任务内容**:
- [x] 添加数据源类型常量
- [x] 添加词书相关字段
- [x] 添加必要的导入语句

**状态**: 已完成

---

### 2.2 修改onCreate方法 ✅
**优先级**: 高
**预计时间**: 0.5小时
**任务内容**:
- [x] 从Intent获取数据源参数
- [x] 验证词书参数
- [x] 根据数据源类型初始化

**状态**: 已完成

---

### 2.3 重构initVocabularyData方法 ⏳
**优先级**: 高
**预计时间**: 3小时
**任务内容**:
- [ ] 拆分为 `loadDefaultVocabulary()` 和 `loadWordsFromBook()`
- [ ] 实现从词书加载单词逻辑
- [ ] 实现 `DictionaryWordEntity` 到 `VocabularyItem` 的转换
- [ ] 集成 `WordSelector` 选择单词
- [ ] 集成 `QuestionGenerator` 生成题目
- [ ] 添加加载状态显示
- [ ] 添加错误处理

**文件位置**:
- `app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**验收标准**:
- 支持固定词汇列表模式
- 支持词书学习模式
- 正确生成题目
- 加载流畅无卡顿

---

### 2.4 集成ProgressManager ⏳
**优先级**: 高
**预计时间**: 1小时
**任务内容**:
- [ ] 在答题后调用 `ProgressManager.updateProgress()`
- [ ] 保存学习进度到数据库
- [ ] 更新UI显示

**文件位置**:
- `app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**验收标准**:
- 学习进度正确保存
- 熟练度正确更新
- 下次复习时间正确计算

---

### 2.5 优化AudioPlayer集成 ⏳
**优先级**: 中
**预计时间**: 1小时
**任务内容**:
- [ ] 使用独立的 `AudioPlayer` 类
- [ ] 优化播放状态管理
- [ ] 添加播放错误提示

**文件位置**:
- `app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**验收标准**:
- 发音播放流畅
- 状态显示正确
- 错误提示友好

---

### 2.6 优化任务进度同步 ⏳
**优先级**: 高
**预计时间**: 1小时
**任务内容**:
- [ ] 确保每答对一题调用 `TaskProgressTracker.recordProgress()`
- [ ] 验证任务类型为 `vocabulary_training`
- [ ] 添加日志记录
- [ ] 测试任务同步功能

**文件位置**:
- `app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**验收标准**:
- 任务进度正确同步
- 达到目标自动完成
- 日志记录完整

---

### 2.7 添加学习模式支持 ⏳
**优先级**: 中
**预计时间**: 2小时
**任务内容**:
- [ ] 支持新词学习模式
- [ ] 支持复习模式
- [ ] 支持随机练习模式
- [ ] 根据模式调用不同的单词选择逻辑

**文件位置**:
- `app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**验收标准**:
- 三种模式正常工作
- 单词筛选正确
- 模式切换流畅

---

## 阶段三: 词书列表界面开发

### 3.1 创建BookListActivity ⏳
**优先级**: 高
**预计时间**: 3小时
**任务内容**:
- [ ] 创建 `BookListActivity` 类
- [ ] 设计布局文件 `activity_book_list.xml`
- [ ] 实现UI初始化
- [ ] 实现搜索功能
- [ ] 实现筛选功能
- [ ] 实现点击跳转

**文件位置**:
- `app/src/main/java/com/example/mybighomework/BookListActivity.java`
- `app/src/main/res/layout/activity_book_list.xml`

**验收标准**:
- 界面美观
- 搜索功能正常
- 筛选功能正常
- 点击跳转正确

---

### 3.2 创建BookListAdapter ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 创建 `BookListAdapter` 类
- [ ] 设计词书卡片布局 `item_book_card.xml`
- [ ] 实现ViewHolder
- [ ] 实现数据绑定
- [ ] 实现点击事件

**文件位置**:
- `app/src/main/java/com/example/mybighomework/adapter/BookListAdapter.java`
- `app/src/main/res/layout/item_book_card.xml`

**验收标准**:
- 卡片显示正确
- 进度显示准确
- 点击响应正常

---

### 3.3 创建BookListViewModel ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 创建 `BookListViewModel` 类
- [ ] 实现数据加载逻辑
- [ ] 实现搜索逻辑
- [ ] 实现筛选逻辑
- [ ] 使用LiveData暴露数据

**文件位置**:
- `app/src/main/java/com/example/mybighomework/viewmodel/BookListViewModel.java`

**验收标准**:
- 数据加载正确
- 搜索功能正常
- 筛选功能正常
- LiveData更新及时

---

### 3.4 优化BookDetailActivity ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 修改"开始学习"按钮调用 `VocabularyActivity`
- [ ] 传递正确的Intent参数
- [ ] 添加学习模式选择
- [ ] 优化统计信息显示

**文件位置**:
- `app/src/main/java/com/example/mybighomework/BookDetailActivity.java`

**验收标准**:
- 调用 `VocabularyActivity` 成功
- 参数传递正确
- 学习模式选择正常

---

### 3.5 添加入口点 ⏳
**优先级**: 中
**预计时间**: 1小时
**任务内容**:
- [ ] 在 `MainActivity` 添加词汇训练入口
- [ ] 在底部导航添加词汇训练图标
- [ ] 更新导航逻辑

**文件位置**:
- `app/src/main/java/com/example/mybighomework/MainActivity.java`
- `app/src/main/res/menu/bottom_navigation.xml`

**验收标准**:
- 入口点可见
- 点击跳转正确
- 导航流畅

---

## 阶段四: 集成测试和优化

### 4.1 修复日期匹配问题 ⏳
**优先级**: 高
**预计时间**: 2小时
**任务内容**:
- [ ] 分析日期不匹配的原因
- [ ] 统一日期格式和时区
- [ ] 添加详细的日志记录
- [ ] 测试任务创建和查询

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/TaskProgressTracker.java`
- `app/src/main/java/com/example/mybighomework/DailyTaskActivity.java`

**验收标准**:
- 任务日期匹配正确
- 任务进度正常同步
- 日志记录完整

---

### 4.2 端到端测试 ⏳
**优先级**: 高
**预计时间**: 3小时
**任务内容**:
- [ ] 测试词书选择流程
- [ ] 测试新词学习流程
- [ ] 测试复习流程
- [ ] 测试任务同步
- [ ] 测试学习进度保存
- [ ] 测试错题记录
- [ ] 测试发音播放

**验收标准**:
- 所有流程正常工作
- 无明显bug
- 数据保存正确

---

### 4.3 性能优化 ⏳
**优先级**: 中
**预计时间**: 2小时
**任务内容**:
- [ ] 优化数据库查询
- [ ] 添加数据缓存
- [ ] 优化UI渲染
- [ ] 减少内存占用
- [ ] 性能测试

**验收标准**:
- 界面流畅无卡顿
- 内存占用合理
- 电池消耗正常

---

### 4.4 异常处理完善 ⏳
**优先级**: 中
**预计时间**: 2小时
**任务内容**:
- [ ] 添加网络异常处理
- [ ] 添加数据库异常处理
- [ ] 添加用户操作异常处理
- [ ] 优化错误提示
- [ ] 添加崩溃日志

**验收标准**:
- 异常不导致崩溃
- 错误提示友好
- 日志记录完整

---

### 4.5 UI优化 ⏳
**优先级**: 中
**预计时间**: 2小时
**任务内容**:
- [ ] 优化界面布局
- [ ] 优化颜色和字体
- [ ] 添加动画效果
- [ ] 优化加载状态显示
- [ ] 适配不同屏幕尺寸

**验收标准**:
- 界面美观
- 动画流畅
- 适配良好

---

## 阶段五: 清理和文档

### 5.1 标记废弃代码 ⏳
**优先级**: 低
**预计时间**: 1小时
**任务内容**:
- [ ] 标记 `BookLearningActivity` 为 `@Deprecated`
- [ ] 添加迁移说明注释
- [ ] 更新相关文档

**文件位置**:
- `app/src/main/java/com/example/mybighomework/BookLearningActivity.java`

**验收标准**:
- 代码标记正确
- 注释清晰
- 文档更新

---

### 5.2 更新ActionTypeInferrer ⏳
**优先级**: 中
**预计时间**: 0.5小时
**任务内容**:
- [ ] 确保 `vocabulary_training` 映射到 `VocabularyActivity`
- [ ] 移除 `BookLearningActivity` 的映射
- [ ] 测试任务跳转

**文件位置**:
- `app/src/main/java/com/example/mybighomework/utils/ActionTypeInferrer.java`

**验收标准**:
- 映射正确
- 跳转正常

---

### 5.3 编写使用文档 ⏳
**优先级**: 低
**预计时间**: 2小时
**任务内容**:
- [ ] 编写用户使用指南
- [ ] 编写开发者文档
- [ ] 更新README
- [ ] 添加代码注释

**验收标准**:
- 文档完整
- 说明清晰
- 注释充分

---

### 5.4 最终测试 ⏳
**优先级**: 高
**预计时间**: 3小时
**任务内容**:
- [ ] 全功能回归测试
- [ ] 兼容性测试
- [ ] 压力测试
- [ ] 用户验收测试

**验收标准**:
- 所有功能正常
- 无严重bug
- 用户满意

---

## 总结

### 任务统计
- **总任务数**: 33个
- **已完成**: 2个
- **进行中**: 0个
- **待开始**: 31个

### 预计时间
- **阶段一**: 15小时
- **阶段二**: 9.5小时
- **阶段三**: 12小时
- **阶段四**: 11小时
- **阶段五**: 6.5小时
- **总计**: 54小时

### 关键里程碑
1. **阶段一完成**: 数据库和Repository层改造完成
2. **阶段二完成**: VocabularyActivity支持词书学习
3. **阶段三完成**: 词书列表界面可用
4. **阶段四完成**: 所有功能测试通过
5. **阶段五完成**: 项目交付

### 风险提示
- 数据库迁移可能影响现有数据
- 任务同步逻辑需要仔细测试
- 性能优化需要实际测试验证
- UI适配需要在多种设备上测试

### 建议
- 每完成一个阶段进行一次完整测试
- 保持代码提交频率,便于回滚
- 及时记录遇到的问题和解决方案
- 与用户保持沟通,及时调整需求
