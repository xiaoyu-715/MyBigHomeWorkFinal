# 🤖 机器学习增强功能使用指南

## 🚀 快速开始

### **1. 启用ML增强推荐**

```java
public class MainActivity extends AppCompatActivity {
    
    private PersonalizedRecommendationEngine mlEngine;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化ML增强推荐引擎
        mlEngine = new PersonalizedRecommendationEngine(this);
        
        // 检查ML状态
        checkMLStatus();
        
        // 生成智能推荐
        generateMLRecommendations();
    }
    
    private void checkMLStatus() {
        // 获取ML性能报告
        MLHelperMethods.ModelPerformanceReport report = mlEngine.getMLPerformanceReport();
        
        Log.d("ML_STATUS", report.toString());
        
        // 显示ML状态
        if (report.sampleCount >= 10) {
            showMLEnabledUI();
        } else {
            showMLLearningUI(report.sampleCount);
        }
    }
}
```

### **2. ML增强推荐调用**

```java
private void generateMLRecommendations() {
    // 显示智能分析进度
    ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage("AI正在分析您的学习数据...");
    dialog.show();
    
    mlEngine.generateRecommendations(new PersonalizedRecommendationEngine.OnRecommendationListener() {
        @Override
        public void onRecommendationGenerated(PersonalizedRecommendationEngine.RecommendationResult result) {
            runOnUiThread(() -> {
                dialog.dismiss();
                
                // 显示ML增强结果
                displayMLEnhancedRecommendations(result);
                
                // 显示置信度
                showConfidenceScore(result.confidenceScore);
                
                // 记录用户交互
                recordUserInteraction(result);
            });
        }
        
        @Override
        public void onError(String error) {
            runOnUiThread(() -> {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, 
                    "AI分析失败，使用标准推荐: " + error, 
                    Toast.LENGTH_SHORT).show();
            });
        }
    });
}
```

### **3. A/B测试效果展示**

```java
private void displayMLEnhancedRecommendations(PersonalizedRecommendationEngine.RecommendationResult result) {
    // 创建ML增强结果展示
    LinearLayout container = findViewById(R.id.recommendations_container);
    container.removeAllViews();
    
    // 显示AI分析摘要
    addAIAnalysisSummary(container, result.userProfile);
    
    // 显示推荐计划
    for (StudyPlan plan : result.recommendedPlans) {
        View planCard = createMLEnhancedPlanCard(plan);
        container.addView(planCard);
    }
    
    // 显示A/B测试信息
    addABTestInfo(container);
    
    // 显示置信度指示器
    addConfidenceIndicator(container, result.confidenceScore);
}

private View createMLEnhancedPlanCard(StudyPlan plan) {
    View card = getLayoutInflater().inflate(R.layout.ml_enhanced_plan_card, null);
    
    // 基础信息
    TextView title = card.findViewById(R.id.tv_plan_title);
    TextView description = card.findViewById(R.id.tv_plan_description);
    title.setText(plan.getTitle());
    description.setText(plan.getDescription());
    
    // ML增强标识
    ImageView mlBadge = card.findViewById(R.id.iv_ml_badge);
    mlBadge.setVisibility(View.VISIBLE);
    
    // 个性化推荐理由
    TextView reason = card.findViewById(R.id.tv_ml_reason);
    reason.setText("🤖 AI推荐: " + generatePlanReason(plan));
    
    return card;
}
```

## 🎯 **高级功能使用**

### **1. 手动触发模型重训练**

```java
private void retrainMLModel() {
    // 显示重训练进度
    ProgressDialog retrainDialog = new ProgressDialog(this);
    retrainDialog.setMessage("AI正在学习您的最新数据...");
    retrainDialog.setCancelable(false);
    retrainDialog.show();
    
    mlEngine.retrainModel(new PersonalizedRecommendationEngine.OnModelRetrainListener() {
        @Override
        public void onRetrainCompleted(int sampleCount) {
            runOnUiThread(() -> {
                retrainDialog.dismiss();
                
                // 显示重训练结果
                showRetrainSuccess(sampleCount);
                
                // 自动刷新推荐
                generateMLRecommendations();
            });
        }
        
        @Override
        public void onError(String error) {
            runOnUiThread(() -> {
                retrainDialog.dismiss();
                Toast.makeText(MainActivity.this, 
                    "AI学习失败: " + error, Toast.LENGTH_LONG).show();
            });
        }
    });
}

private void showRetrainSuccess(int sampleCount) {
    new AlertDialog.Builder(this)
        .setTitle("🎉 AI学习完成")
        .setMessage(String.format("AI已基于 %d 个学习样本完成训练，推荐精度进一步提升！", sampleCount))
        .setPositiveButton("查看效果", (dialog, which) -> {
            generateMLRecommendations();
        })
        .setNegativeButton("稍后", null)
        .show();
}
```

### **2. ML性能监控面板**

```java
private void showMLPerformancePanel() {
    // 获取详细性能报告
    MLHelperMethods.ModelPerformanceReport report = mlEngine.getMLPerformanceReport();
    PersonalizedRecommendationEngine.CacheStats cacheStats = mlEngine.getCacheStats();
    
    // 创建性能面板
    View panel = getLayoutInflater().inflate(R.layout.ml_performance_panel, null);
    
    // 显示准确率
    ProgressBar accuracyProgress = panel.findViewById(R.id.progress_accuracy);
    TextView accuracyText = panel.findViewById(R.id.tv_accuracy);
    accuracyProgress.setProgress((int)(report.averageAccuracy * 100));
    accuracyText.setText(String.format("%.1f%%", report.averageAccuracy * 100));
    
    // 显示响应时间
    TextView responseTimeText = panel.findViewById(R.id.tv_response_time);
    responseTimeText.setText(String.format("%dms", report.averageResponseTime));
    
    // 显示缓存命中率
    ProgressBar cacheProgress = panel.findViewById(R.id.progress_cache_hit);
    TextView cacheText = panel.findViewById(R.id.tv_cache_hit);
    cacheProgress.setProgress((int)(cacheStats.hitRate * 100));
    cacheText.setText(String.format("%.1f%%", cacheStats.hitRate * 100));
    
    // 显示学习样本数
    TextView samplesText = panel.findViewById(R.id.tv_samples);
    samplesText.setText(String.format("%d 个样本", report.sampleCount));
    
    // 显示面板
    new AlertDialog.Builder(this)
        .setTitle("📊 AI性能监控")
        .setView(panel)
        .setPositiveButton("刷新数据", (dialog, which) -> {
            mlEngine.refreshUserProfile(new PersonalizedRecommendationEngine.OnProfileRefreshListener() {
                @Override
                public void onProfileRefreshed(PersonalizedRecommendationEngine.UserProfile profile) {
                    Toast.makeText(MainActivity.this, "数据已刷新", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, "刷新失败: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        })
        .setNegativeButton("关闭", null)
        .show();
}
```

### **3. 用户反馈收集**

```java
private void collectUserFeedback(StudyPlan plan, boolean isPositive) {
    // 记录用户反馈，用于ML模型训练
    String feedbackType = isPositive ? "positive" : "negative";
    
    // 这里应该调用反馈记录API
    recordFeedbackForML(plan, feedbackType);
    
    // 显示感谢信息
    String message = isPositive ? 
        "😊 感谢反馈！AI会记住您的喜好" : 
        "🤔 我们会改进！AI正在学习您的偏好";
    
    Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG)
        .setAction("查看学习进度", v -> showMLPerformancePanel())
        .show();
    
    // 触发增量学习
    triggerIncrementalLearning();
}

private void recordFeedbackForML(StudyPlan plan, String feedbackType) {
    // 在实际应用中，这里应该：
    // 1. 保存反馈到数据库
    // 2. 更新用户画像
    // 3. 为ML模型准备新的训练样本
    
    Log.d("ML_FEEDBACK", String.format("用户对计划 '%s' 的反馈: %s", 
        plan.getTitle(), feedbackType));
}
```

## 🎨 **UI组件示例**

### **1. ML增强推荐卡片布局**

```xml
<!-- res/layout/ml_enhanced_plan_card.xml -->
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="6dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- ML增强标识 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">
            
            <ImageView
                android:id="@+id/iv_ml_badge"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@drawable/ic_ai_chip"
                android:visibility="gone" />
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:text="AI智能推荐"
                android:textSize="12sp"
                android:textColor="@color/ai_accent"
                android:background="@drawable/bg_ai_badge" />
            
            <View
                android:layout_width="0dp"
                android:layout_height="1dp"
                android:layout_weight="1" />
            
            <!-- 置信度指示器 -->
            <TextView
                android:id="@+id/tv_confidence"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="95%"
                android:textSize="12sp"
                android:textColor="@color/success_green" />
            
        </LinearLayout>
        
        <!-- 计划标题 -->
        <TextView
            android:id="@+id/tv_plan_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />
        
        <!-- 计划描述 -->
        <TextView
            android:id="@+id/tv_plan_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:maxLines="3"
            android:ellipsize="end" />
        
        <!-- AI推荐理由 -->
        <TextView
            android:id="@+id/tv_ml_reason"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:padding="12dp"
            android:background="@drawable/bg_ai_reason"
            android:textSize="12sp"
            android:textColor="@color/ai_text"
            android:drawablePadding="8dp" />
        
        <!-- 操作按钮 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">
            
            <Button
                android:id="@+id/btn_apply_plan"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="8dp"
                android:text="应用计划"
                style="@style/Widget.MaterialComponents.Button" />
            
            <ImageButton
                android:id="@+id/btn_thumbs_up"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:layout_marginEnd="4dp"
                android:src="@drawable/ic_thumbs_up"
                android:background="@drawable/bg_circle_button"
                android:contentDescription="好评" />
            
            <ImageButton
                android:id="@+id/btn_thumbs_down"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:src="@drawable/ic_thumbs_down"
                android:background="@drawable/bg_circle_button"
                android:contentDescription="差评" />
            
        </LinearLayout>
        
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### **2. ML性能监控面板**

```xml
<!-- res/layout/ml_performance_panel.xml -->
<ScrollView
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- 总体状态 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="🤖 AI学习系统状态"
            android:textSize="18sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="16dp" />
        
        <!-- 准确率 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="12dp">
            
            <TextView
                android:layout_width="80dp"
                android:layout_height="wrap_content"
                android:text="准确率"
                android:textSize="14sp" />
            
            <ProgressBar
                android:id="@+id/progress_accuracy"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="0dp"
                android:layout_height="8dp"
                android:layout_weight="1"
                android:layout_marginHorizontal="8dp"
                android:max="100" />
            
            <TextView
                android:id="@+id/tv_accuracy"
                android:layout_width="50dp"
                android:layout_height="wrap_content"
                android:text="95%"
                android:textAlignment="textEnd"
                android:textSize="14sp"
                android:textStyle="bold" />
        </LinearLayout>
        
        <!-- 响应时间 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="12dp">
            
            <TextView
                android:layout_width="80dp"
                android:layout_height="wrap_content"
                android:text="响应时间"
                android:textSize="14sp" />
            
            <TextView
                android:id="@+id/tv_response_time"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="65ms"
                android:textAlignment="textEnd"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="@color/success_green" />
        </LinearLayout>
        
        <!-- 缓存命中率 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="12dp">
            
            <TextView
                android:layout_width="80dp"
                android:layout_height="wrap_content"
                android:text="缓存命中"
                android:textSize="14sp" />
            
            <ProgressBar
                android:id="@+id/progress_cache_hit"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="0dp"
                android:layout_height="8dp"
                android:layout_weight="1"
                android:layout_marginHorizontal="8dp"
                android:max="100" />
            
            <TextView
                android:id="@+id/tv_cache_hit"
                android:layout_width="50dp"
                android:layout_height="wrap_content"
                android:text="87%"
                android:textAlignment="textEnd"
                android:textSize="14sp"
                android:textStyle="bold" />
        </LinearLayout>
        
        <!-- 学习样本数 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="16dp">
            
            <TextView
                android:layout_width="80dp"
                android:layout_height="wrap_content"
                android:text="训练样本"
                android:textSize="14sp" />
            
            <TextView
                android:id="@+id/tv_samples"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="156 个样本"
                android:textAlignment="textEnd"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="@color/info_blue" />
        </LinearLayout>
        
        <!-- A/B测试信息 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="📊 当前使用策略 B (增强版)\n🎯 相比基准版本提升 15.8%"
            android:textSize="12sp"
            android:textColor="@color/text_secondary"
            android:background="@drawable/bg_info_panel"
            android:padding="12dp"
            android:layout_marginTop="8dp" />
        
    </LinearLayout>
</ScrollView>
```

## ⚙️ **配置选项**

### **ML模型参数调整**

```java
// 在Application类中配置ML参数
public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 配置ML参数
        configureMLParameters();
    }
    
    private void configureMLParameters() {
        // 可以通过SharedPreferences或配置文件调整这些参数
        
        // 学习率调整 (默认0.01)
        MLConfig.setLearningRate(0.015); // 稍微激进一点
        
        // 最小训练样本数 (默认10)
        MLConfig.setMinSamplesForML(15); // 更严格的启用条件
        
        // 缓存过期时间 (默认30分钟)
        MLConfig.setCacheExpiryTime(45 * 60 * 1000L); // 45分钟
        
        // A/B测试权重 (可动态调整)
        MLConfig.setABTestVariant("variant_b", 1.3); // 进一步增强
    }
}
```

这个指南展示了如何在实际应用中使用ML增强功能，包括完整的代码示例和UI布局。通过这些示例，开发者可以快速集成AI推荐功能并为用户提供智能化的学习体验。
