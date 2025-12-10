package com.example.mybighomework.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mybighomework.StudyPlan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习计划模板管理器
 * 提供专业的预设学习计划模板和用户自定义模板管理
 */
public class StudyPlanTemplateManager {
    
    private static final String TAG = "StudyPlanTemplateManager";
    private static final String PREF_NAME = "study_plan_templates";
    private static final String KEY_CUSTOM_TEMPLATES = "custom_templates";
    
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public StudyPlanTemplateManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * 模板类别枚举
     */
    public enum TemplateCategory {
        CET4("大学英语四级"),
        CET6("大学英语六级"),
        TOEFL("托福考试"),
        IELTS("雅思考试"),
        POSTGRADUATE("考研英语"),
        BUSINESS("商务英语"),
        DAILY("日常英语"),
        CUSTOM("自定义模板");
        
        private final String displayName;
        
        TemplateCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 学习计划模板
     */
    public static class StudyPlanTemplate {
        private String id;
        private String name;
        private String description;
        private TemplateCategory category;
        private String targetLevel;
        private int estimatedDays;
        private List<StudyPlan> plans;
        private long createdTime;
        private boolean isBuiltIn; // 是否为内置模板
        
        public StudyPlanTemplate() {
            this.plans = new ArrayList<>();
            this.createdTime = System.currentTimeMillis();
        }
        
        public StudyPlanTemplate(String id, String name, String description, 
                               TemplateCategory category, String targetLevel, int estimatedDays) {
            this();
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.targetLevel = targetLevel;
            this.estimatedDays = estimatedDays;
            this.isBuiltIn = true;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public TemplateCategory getCategory() { return category; }
        public void setCategory(TemplateCategory category) { this.category = category; }
        
        public String getTargetLevel() { return targetLevel; }
        public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
        
        public int getEstimatedDays() { return estimatedDays; }
        public void setEstimatedDays(int estimatedDays) { this.estimatedDays = estimatedDays; }
        
        public List<StudyPlan> getPlans() { return plans; }
        public void setPlans(List<StudyPlan> plans) { this.plans = plans; }
        
        public long getCreatedTime() { return createdTime; }
        public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
        
        public boolean isBuiltIn() { return isBuiltIn; }
        public void setBuiltIn(boolean builtIn) { isBuiltIn = builtIn; }
    }
    
    /**
     * 获取所有可用的模板
     */
    public List<StudyPlanTemplate> getAllTemplates() {
        List<StudyPlanTemplate> allTemplates = new ArrayList<>();
        
        // 添加内置模板
        allTemplates.addAll(getBuiltInTemplates());
        
        // 添加用户自定义模板
        allTemplates.addAll(getCustomTemplates());
        
        return allTemplates;
    }
    
    /**
     * 根据类别获取模板
     */
    public List<StudyPlanTemplate> getTemplatesByCategory(TemplateCategory category) {
        List<StudyPlanTemplate> templates = new ArrayList<>();
        
        for (StudyPlanTemplate template : getAllTemplates()) {
            if (template.getCategory() == category) {
                templates.add(template);
            }
        }
        
        return templates;
    }
    
    /**
     * 根据ID获取模板
     */
    public StudyPlanTemplate getTemplateById(String templateId) {
        for (StudyPlanTemplate template : getAllTemplates()) {
            if (templateId.equals(template.getId())) {
                return template;
            }
        }
        return null;
    }
    
    /**
     * 获取内置模板
     */
    public List<StudyPlanTemplate> getBuiltInTemplates() {
        List<StudyPlanTemplate> templates = new ArrayList<>();
        
        // CET4 模板
        templates.add(createCET4Template());
        
        // CET6 模板
        templates.add(createCET6Template());
        
        // 托福模板
        templates.add(createTOEFLTemplate());
        
        // 雅思模板
        templates.add(createIELTSTemplate());
        
        // 考研英语模板
        templates.add(createPostgraduateTemplate());
        
        // 商务英语模板
        templates.add(createBusinessTemplate());
        
        // 日常英语模板
        templates.add(createDailyTemplate());
        
        return templates;
    }
    
    /**
     * 创建CET4模板
     */
    private StudyPlanTemplate createCET4Template() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "cet4_standard",
            "大学英语四级标准计划",
            "针对英语四级考试的全面备考计划，涵盖词汇、语法、阅读、听力、写作五大模块，适合有一定英语基础的学生。",
            TemplateCategory.CET4,
            "四级水平（425+）",
            120
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        // 词汇计划
        plans.add(new StudyPlan(
            "四级核心词汇突破",
            "词汇",
            "掌握四级核心词汇4500个，采用分层记忆法，每天学习新词汇50个，复习旧词汇100个。" +
            "第一阶段：基础词汇巩固（30天）\n" +
            "第二阶段：核心词汇突破（45天）\n" +
            "第三阶段：高频词汇强化（30天）\n" +
            "第四阶段：考前冲刺复习（15天）",
            generateTimeRange(4),
            "40分钟/天",
            "高"
        ));
        
        // 阅读计划
        plans.add(new StudyPlan(
            "四级阅读理解专训",
            "阅读",
            "系统提升阅读理解能力，掌握快速阅读技巧和答题策略。" +
            "训练内容：\n" +
            "• 长篇阅读：每天1篇，重点训练信息匹配\n" +
            "• 深度阅读：每天2篇，练习细节理解和推理判断\n" +
            "• 选词填空：每周3次，掌握词汇搭配和语法知识\n" +
            "目标：阅读部分达到160分以上",
            generateTimeRange(4),
            "35分钟/天",
            "高"
        ));
        
        // 听力计划
        plans.add(new StudyPlan(
            "四级听力提升训练",
            "听力",
            "全面提升听力理解能力，熟悉考试题型和答题技巧。" +
            "训练安排：\n" +
            "• 短篇新闻：每天15分钟精听练习\n" +
            "• 长对话：每天1-2篇完整对话\n" +
            "• 短文听写：每周2次听写训练\n" +
            "• 模拟测试：每周1次完整听力测试",
            generateTimeRange(4),
            "30分钟/天",
            "中"
        ));
        
        // 写作计划
        plans.add(new StudyPlan(
            "四级写作能力提升",
            "写作",
            "掌握四级写作技巧，提高英语表达能力。" +
            "学习内容：\n" +
            "• 写作模板学习：议论文、应用文、图表作文\n" +
            "• 素材积累：常用句型、连接词、高级词汇\n" +
            "• 实战练习：每周2-3篇完整作文\n" +
            "• 批改反馈：重点关注语法、逻辑、表达",
            generateTimeRange(4),
            "25分钟/天",
            "中"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    /**
     * 创建CET6模板
     */
    private StudyPlanTemplate createCET6Template() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "cet6_standard",
            "大学英语六级进阶计划",
            "专为英语六级考试设计的进阶学习计划，在四级基础上进一步提升英语综合能力。",
            TemplateCategory.CET6,
            "六级水平（425+）",
            150
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "六级高难度词汇突破",
            "词汇",
            "掌握六级高难度词汇6000+个，重点攻克学术词汇和高频短语。" +
            "学习策略：\n" +
            "• 词汇分类记忆：学术、商务、文学类词汇\n" +
            "• 词根词缀法：系统学习构词规律\n" +
            "• 语境记忆：通过真题例句掌握用法\n" +
            "• 定期测试：每周词汇测试巩固效果",
            generateTimeRange(5),
            "45分钟/天",
            "高"
        ));
        
        plans.add(new StudyPlan(
            "六级深度阅读提升",
            "阅读",
            "提升阅读理解的深度和广度，掌握复杂文本分析能力。" +
            "训练重点：\n" +
            "• 学术文章理解：科技、经济、社会类文章\n" +
            "• 批判性思维：分析作者观点和论证逻辑\n" +
            "• 快速定位：提高信息检索和匹配能力\n" +
            "• 推理判断：培养深层理解和推理能力",
            generateTimeRange(5),
            "40分钟/天",
            "高"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    /**
     * 创建托福模板
     */
    private StudyPlanTemplate createTOEFLTemplate() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "toefl_standard",
            "托福考试备考计划",
            "全面的托福考试备考方案，针对听、说、读、写四项技能进行系统训练。",
            TemplateCategory.TOEFL,
            "托福80+分",
            180
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "托福综合技能提升",
            "综合",
            "全面提升托福考试所需的听说读写四项技能。" +
            "训练计划：\n" +
            "• 词汇积累：每天背诵托福核心词汇100个\n" +
            "• 听力训练：学术讲座、校园对话专项练习\n" +
            "• 口语练习：独立口语和综合口语任务\n" +
            "• 阅读理解：学术文章快速阅读和精读结合\n" +
            "• 写作训练：独立写作和综合写作技巧",
            generateTimeRange(6),
            "90分钟/天",
            "高"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    /**
     * 创建其他模板的方法...
     */
    private StudyPlanTemplate createIELTSTemplate() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "ielts_standard",
            "雅思考试全能计划",
            "针对雅思考试的专业备考计划，注重实用性和学术性并重。",
            TemplateCategory.IELTS,
            "雅思6.5+分",
            180
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "雅思四项技能均衡发展",
            "综合",
            "平衡发展听说读写四项技能，特别注重口语和写作的表达能力。" +
            "学习重点：\n" +
            "• 听力：多场景对话和学术讲座理解\n" +
            "• 阅读：快速浏览和细节定位技巧\n" +
            "• 写作：Task1图表描述和Task2议论文写作\n" +
            "• 口语：流利度、准确性和连贯性提升",
            generateTimeRange(6),
            "80分钟/天",
            "高"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    private StudyPlanTemplate createPostgraduateTemplate() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "postgrad_standard",
            "考研英语攻克计划",
            "专门针对考研英语的高效备考方案，重点突破阅读理解和写作。",
            TemplateCategory.POSTGRADUATE,
            "考研英语70+分",
            240
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "考研英语重点突破",
            "阅读",
            "以阅读理解为核心，全面提升考研英语应试能力。" +
            "备考策略：\n" +
            "• 词汇建设：考研大纲词汇5500个系统掌握\n" +
            "• 阅读理解：精读与泛读结合，提高理解准确度\n" +
            "• 新题型：七选五、排序题专项训练\n" +
            "• 翻译写作：英译汉技巧和大小作文模板",
            generateTimeRange(8),
            "60分钟/天",
            "高"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    private StudyPlanTemplate createBusinessTemplate() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "business_standard",
            "商务英语实用计划",
            "面向职场人士的商务英语学习计划，提升商务沟通和写作能力。",
            TemplateCategory.BUSINESS,
            "商务英语中级",
            120
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "商务英语沟通技能",
            "口语",
            "掌握商务场景下的英语沟通技巧。" +
            "学习内容：\n" +
            "• 商务会议：会议主持、发言、讨论技巧\n" +
            "• 谈判技巧：商务谈判用语和策略\n" +
            "• 邮件写作：商务邮件格式和表达方式\n" +
            "• 报告presentation：数据展示和汇报技巧",
            generateTimeRange(4),
            "45分钟/天",
            "中"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    private StudyPlanTemplate createDailyTemplate() {
        StudyPlanTemplate template = new StudyPlanTemplate(
            "daily_standard",
            "日常英语提升计划",
            "适合英语爱好者的日常学习计划，注重实用性和趣味性。",
            TemplateCategory.DAILY,
            "日常交流无障碍",
            90
        );
        
        List<StudyPlan> plans = new ArrayList<>();
        
        plans.add(new StudyPlan(
            "日常英语交流能力",
            "口语",
            "提升日常英语交流能力，掌握生活中常用的英语表达。" +
            "学习安排：\n" +
            "• 生活场景对话：购物、餐饮、交通等\n" +
            "• 话题讨论：兴趣爱好、文化差异等\n" +
            "• 影视英语：通过电影电视剧学习地道表达\n" +
            "• 新闻阅读：关注时事，扩展词汇量",
            generateTimeRange(3),
            "30分钟/天",
            "低"
        ));
        
        template.setPlans(plans);
        return template;
    }
    
    /**
     * 获取用户自定义模板
     */
    public List<StudyPlanTemplate> getCustomTemplates() {
        String jsonString = sharedPreferences.getString(KEY_CUSTOM_TEMPLATES, "[]");
        Type listType = new TypeToken<List<StudyPlanTemplate>>(){}.getType();
        List<StudyPlanTemplate> templates = gson.fromJson(jsonString, listType);
        return templates != null ? templates : new ArrayList<>();
    }
    
    /**
     * 保存用户自定义模板
     */
    public void saveCustomTemplate(StudyPlanTemplate template) {
        List<StudyPlanTemplate> customTemplates = getCustomTemplates();
        
        // 设置为非内置模板
        template.setBuiltIn(false);
        
        // 如果是更新现有模板
        boolean updated = false;
        for (int i = 0; i < customTemplates.size(); i++) {
            if (customTemplates.get(i).getId().equals(template.getId())) {
                customTemplates.set(i, template);
                updated = true;
                break;
            }
        }
        
        // 如果是新模板
        if (!updated) {
            template.setId("custom_" + System.currentTimeMillis());
            customTemplates.add(template);
        }
        
        // 保存到SharedPreferences
        String jsonString = gson.toJson(customTemplates);
        sharedPreferences.edit().putString(KEY_CUSTOM_TEMPLATES, jsonString).apply();
        
        Log.d(TAG, "自定义模板已保存: " + template.getName());
    }
    
    /**
     * 删除用户自定义模板
     */
    public boolean deleteCustomTemplate(String templateId) {
        List<StudyPlanTemplate> customTemplates = getCustomTemplates();
        
        boolean removed = customTemplates.removeIf(template -> 
            templateId.equals(template.getId()) && !template.isBuiltIn());
        
        if (removed) {
            String jsonString = gson.toJson(customTemplates);
            sharedPreferences.edit().putString(KEY_CUSTOM_TEMPLATES, jsonString).apply();
            Log.d(TAG, "自定义模板已删除: " + templateId);
        }
        
        return removed;
    }
    
    /**
     * 从现有计划创建模板
     */
    public StudyPlanTemplate createTemplateFromPlans(String name, String description, 
                                                   List<StudyPlan> plans) {
        StudyPlanTemplate template = new StudyPlanTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setCategory(TemplateCategory.CUSTOM);
        template.setTargetLevel("自定义目标");
        template.setEstimatedDays(calculateEstimatedDays(plans));
        template.setPlans(new ArrayList<>(plans));
        template.setBuiltIn(false);
        
        return template;
    }
    
    /**
     * 应用模板到学习计划
     */
    public List<StudyPlan> applyTemplate(StudyPlanTemplate template) {
        List<StudyPlan> appliedPlans = new ArrayList<>();
        
        for (StudyPlan originalPlan : template.getPlans()) {
            // 创建新的计划实例，避免修改模板
            StudyPlan newPlan = new StudyPlan(
                originalPlan.getTitle(),
                originalPlan.getCategory(),
                originalPlan.getDescription(),
                originalPlan.getTimeRange(),
                originalPlan.getDuration(),
                originalPlan.getPriority()
            );
            
            // 更新时间范围为当前时间
            newPlan.setTimeRange(updateTimeRangeToNow(originalPlan.getTimeRange()));
            
            appliedPlans.add(newPlan);
        }
        
        Log.d(TAG, "模板已应用: " + template.getName() + "，生成计划数: " + appliedPlans.size());
        
        return appliedPlans;
    }
    
    /**
     * 搜索模板
     */
    public List<StudyPlanTemplate> searchTemplates(String keyword) {
        List<StudyPlanTemplate> results = new ArrayList<>(); if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTemplates();
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        
        for (StudyPlanTemplate template : getAllTemplates()) {
            if (template.getName().toLowerCase().contains(lowercaseKeyword) ||
                template.getDescription().toLowerCase().contains(lowercaseKeyword) ||
                template.getTargetLevel().toLowerCase().contains(lowercaseKeyword)) {
                results.add(template);
            }
        }
        
        return results;
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 生成时间范围字符串
     */
    private String generateTimeRange(int months) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        calendar.add(Calendar.MONTH, months);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH) + 1;
        
        return String.format("%d-%02d至%d-%02d", 
            currentYear, currentMonth, futureYear, futureMonth);
    }
    
    /**
     * 计算预估学习天数
     */
    private int calculateEstimatedDays(List<StudyPlan> plans) {
        // 简化计算：基于计划数量和复杂度估算
        int baseDays = plans.size() * 30; // 每个计划平均30天
        
        for (StudyPlan plan : plans) {
            // 根据优先级调整
            if ("高".equals(plan.getPriority())) {
                baseDays += 15;
            } else if ("低".equals(plan.getPriority())) {
                baseDays -= 10;
            }
        }
        
        return Math.max(30, baseDays); // 最少30天
    }
    
    /**
     * 更新时间范围到当前时间
     */
    private String updateTimeRangeToNow(String originalTimeRange) {
        // 简化实现：如果原时间范围包含月份信息，更新为当前时间开始
        if (originalTimeRange.contains("至")) {
            String[] parts = originalTimeRange.split("至");
            if (parts.length == 2) {
                // 提取原来的月份数
                try {
                    String[] startParts = parts[0].split("-");
                    String[] endParts = parts[1].split("-");
                    
                    int originalStartYear = Integer.parseInt(startParts[0]);
                    int originalStartMonth = Integer.parseInt(startParts[1]);
                    int originalEndYear = Integer.parseInt(endParts[0]);
                    int originalEndMonth = Integer.parseInt(endParts[1]);
                    
                    // 计算原来的月份差
                    int monthsDiff = (originalEndYear - originalStartYear) * 12 + 
                                   (originalEndMonth - originalStartMonth);
                    
                    // 生成新的时间范围
                    return generateTimeRange(Math.max(1, monthsDiff));
                    
                } catch (NumberFormatException e) {
                    Log.w(TAG, "解析时间范围失败，使用默认值", e);
                }
            }
        }
        
        // 默认返回3个月的时间范围
        return generateTimeRange(3);
    }
}
