# 实现计划

- [x] 1. 更新数据访问层





  - [x] 1.1 扩展TranslationHistoryDao添加分页查询方法


    - 添加 `getPage(int limit, int offset)` 方法
    - 添加 `deleteOldRecords(int keepCount)` 方法
    - 添加 `deleteById(int id)` 方法
    - _Requirements: 1.1, 1.2, 3.1, 3.2, 5.2_

  - [x] 1.2 创建TranslationHistoryRepository类


    - 实现分页获取历史记录方法
    - 实现自动清理超出限制的旧记录
    - 实现搜索历史记录方法
    - 实现删除单条记录方法
    - _Requirements: 1.1, 1.2, 1.3, 5.2, 6.1, 6.2_

  - [ ]* 1.3 编写属性测试：存储容量保证
    - **Property 1: 存储容量保证**
    - **Validates: Requirements 1.1, 1.2**

  - [ ]* 1.4 编写属性测试：持久化往返一致性
    - **Property 2: 持久化往返一致性**
    - **Validates: Requirements 1.3**

  - [ ]* 1.5 编写属性测试：分页加载正确性
    - **Property 3: 分页加载正确性**
    - **Validates: Requirements 3.1, 3.2**

- [x] 2. 实现滑动删除功能





  - [x] 2.1 创建SwipeToDeleteCallback类


    - 继承ItemTouchHelper.SimpleCallback
    - 实现左滑显示删除背景和图标
    - 实现onSwiped回调触发删除
    - _Requirements: 5.1, 5.2_

  - [ ]* 2.2 编写属性测试：删除操作正确性
    - **Property 4: 删除操作正确性**
    - **Validates: Requirements 5.2**

- [x] 3. 实现搜索功能





  - [x] 3.1 在TranslationHistoryDao中添加分页搜索方法


    - 添加 `searchWithPagination(String keyword, int limit, int offset)` 方法
    - _Requirements: 6.1, 6.2_

  - [ ]* 3.2 编写属性测试：搜索结果正确性
    - **Property 5: 搜索结果正确性**
    - **Validates: Requirements 6.1, 6.2**

- [x] 4. 创建时间格式化工具





  - [x] 4.1 创建TimeFormatUtils工具类


    - 实现formatTimestamp方法，支持"今天"、"昨天"、日期格式
    - _Requirements: 7.1_

  - [ ]* 4.2 编写属性测试：时间格式化正确性
    - **Property 6: 时间格式化正确性**
    - **Validates: Requirements 7.1**

- [x] 5. Checkpoint - 确保所有测试通过





  - 确保所有测试通过，如有问题请询问用户。

- [x] 6. 创建增强版历史记录适配器





  - [x] 6.1 创建HistoryFullAdapter类


    - 支持两种ViewType：普通项和加载中项
    - 实现addItems方法追加数据
    - 实现removeItem方法移除单条数据
    - 实现setLoading和setHasMoreData方法控制加载状态
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 5.3_

  - [x] 6.2 创建item_history_full.xml布局文件


    - 显示语言标签、源文本、翻译文本
    - 显示时间戳
    - 支持最多3行预览
    - _Requirements: 7.1, 7.2_

  - [x] 6.3 创建item_loading.xml布局文件


    - 显示加载进度指示器
    - _Requirements: 3.3_

- [x] 7. 创建独立历史记录视图





  - [x] 7.1 创建HistoryBottomSheetFragment类


    - 继承BottomSheetDialogFragment
    - 实现分页加载逻辑
    - 实现滑动删除功能
    - 实现搜索功能
    - _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 6.1_

  - [x] 7.2 创建fragment_history_bottom_sheet.xml布局文件


    - 包含搜索框
    - 包含RecyclerView
    - 包含空状态提示
    - _Requirements: 4.2, 6.1_

- [x] 8. 更新主翻译页面





  - [x] 8.1 修改activity_text_translation.xml布局


    - 为历史记录区域设置固定高度或最大高度
    - 添加"查看全部"按钮
    - 优化嵌套滚动配置
    - _Requirements: 2.1, 2.2, 2.3, 4.1_

  - [x] 8.2 更新TextTranslationActivity


    - 集成TranslationHistoryRepository
    - 实现"查看全部"按钮点击打开BottomSheet
    - 更新历史记录加载逻辑使用Repository
    - 实现自动清理旧记录
    - _Requirements: 1.2, 2.1, 4.1_

  - [x] 8.3 更新TranslationHistoryAdapter


    - 添加时间戳显示
    - 优化文本预览显示
    - _Requirements: 7.1, 7.2_

  - [x] 8.4 更新item_translation_history.xml布局


    - 添加时间戳TextView
    - 调整文本行数限制为3行
    - _Requirements: 7.1, 7.2_

- [x] 9. 实现历史记录详情展开功能






  - [x] 9.1 创建HistoryDetailDialog类

    - 显示完整的源文本和翻译文本
    - 提供复制和删除操作
    - _Requirements: 7.3_

  - [x] 9.2 创建dialog_history_detail.xml布局文件


    - 显示完整内容
    - 包含操作按钮
    - _Requirements: 7.3_

- [x] 10. Final Checkpoint - 确保所有测试通过





  - 确保所有测试通过，如有问题请询问用户。
