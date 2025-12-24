package com.example.mybighomework.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mybighomework.database.converter.DateConverter;
import com.example.mybighomework.database.converter.StringArrayConverter;
import com.example.mybighomework.database.dao.DailySentenceDao;
import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.dao.ExamAnswerDao;
import com.example.mybighomework.database.dao.ExamDao;
import com.example.mybighomework.database.dao.ExamProgressDao;
import com.example.mybighomework.database.dao.ExamResultDao;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.dao.QuestionNoteDao;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.TranslationHistoryDao;
import com.example.mybighomework.database.dao.UserDao;
import com.example.mybighomework.database.dao.UserSettingsDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.dao.WrongQuestionDao;
import com.example.mybighomework.database.entity.DailySentenceEntity;
import com.example.mybighomework.database.entity.DailyTaskEntity;

import java.util.List;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.ExamAnswerEntity;
import com.example.mybighomework.database.entity.ExamProgressEntity;
import com.example.mybighomework.database.entity.ExamRecordEntity;
import com.example.mybighomework.database.entity.ExamResultEntity;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.database.entity.QuestionNoteEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.example.mybighomework.database.entity.UserEntity;
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.example.mybighomework.database.entity.WrongQuestionEntity;

@Database(
    entities = {
        StudyPlanEntity.class,
        StudyPhaseEntity.class,
        DailyTaskEntity.class,
        VocabularyRecordEntity.class,
        ExamRecordEntity.class,
        UserSettingsEntity.class,
        QuestionEntity.class,
        StudyRecordEntity.class,
        UserEntity.class,
        WrongQuestionEntity.class,
        DailySentenceEntity.class,
        TranslationHistoryEntity.class,
        ExamAnswerEntity.class,
        ExamProgressEntity.class,
        QuestionNoteEntity.class,
        ExamResultEntity.class
    },
    version = 19,
    exportSchema = false
)
@TypeConverters({DateConverter.class, StringArrayConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "english_learning_db";

    // 数据库迁移：版本8到9，添加 totalStudyTime
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_settings ADD COLUMN totalStudyTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    // 数据库迁移：版本9到10，添加每日一句扩展字段
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN audioUrl TEXT");
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN imageUrl TEXT");
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN sid TEXT");
        }
    };

    // 版本10到11：考试进度表
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS exam_progress (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "examType TEXT, " +
                "currentQuestionIndex INTEGER NOT NULL, " +
                "timeLeftInMillis INTEGER NOT NULL, " +
                "userAnswersJson TEXT, " +
                "bookmarkedQuestionsJson TEXT, " +
                "startTime INTEGER NOT NULL, " +
                "lastUpdateTime INTEGER NOT NULL, " +
                "isCompleted INTEGER NOT NULL DEFAULT 0)");
        }
    };

    // 版本11到12：题目笔记表
    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS question_notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "exam_title TEXT, " +
                "question_index INTEGER NOT NULL, " +
                "note_content TEXT, " +
                "create_time INTEGER, " +
                "update_time INTEGER)");
        }
    };

    // 版本12到13：考试成绩表
    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS exam_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "examTitle TEXT, " +
                "examYear TEXT, " +
                "examDate INTEGER, " +
                "examDuration INTEGER NOT NULL, " +
                "clozeScore REAL NOT NULL, " +
                "readingScore REAL NOT NULL, " +
                "newTypeScore REAL NOT NULL, " +
                "translationScore REAL NOT NULL, " +
                "writingScore REAL NOT NULL, " +
                "totalScore REAL NOT NULL, " +
                "accuracy REAL NOT NULL, " +
                "totalQuestions INTEGER NOT NULL, " +
                "correctAnswers INTEGER NOT NULL, " +
                "wrongAnswers INTEGER NOT NULL, " +
                "clozeCorrect INTEGER NOT NULL, " +
                "clozeTotal INTEGER NOT NULL, " +
                "readingCorrect INTEGER NOT NULL, " +
                "readingTotal INTEGER NOT NULL, " +
                "newTypeCorrect INTEGER NOT NULL, " +
                "newTypeTotal INTEGER NOT NULL, " +
                "translationComment TEXT, " +
                "writingComment TEXT, " +
                "answerDetails TEXT, " +
                "grade TEXT)");
        }
    };

    // 版本13到14：词汇正确次数
    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_settings ADD COLUMN vocabularyCorrectCount INTEGER NOT NULL DEFAULT 0");
        }
    };

    // 版本14到15：学习计划字段扩展
    static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE study_plans ADD COLUMN summary TEXT");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN totalDays INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN completedDays INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN streakDays INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN totalStudyTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN isAiGenerated INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE study_plans ADD COLUMN dailyMinutes INTEGER NOT NULL DEFAULT 0");
        }
    };

    // 版本15到16：阶段与每日任务表
    static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS study_phases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "planId INTEGER NOT NULL, " +
                "phaseOrder INTEGER NOT NULL, " +
                "phaseName TEXT, " +
                "goal TEXT, " +
                "durationDays INTEGER NOT NULL, " +
                "taskTemplateJson TEXT, " +
                "completedDays INTEGER NOT NULL DEFAULT 0, " +
                "progress INTEGER NOT NULL DEFAULT 0, " +
                "status TEXT, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "FOREIGN KEY(planId) REFERENCES study_plans(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_study_phases_planId ON study_phases(planId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_study_phases_planId_phaseOrder ON study_phases(planId, phaseOrder)");

            database.execSQL("CREATE TABLE IF NOT EXISTS daily_tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "planId INTEGER NOT NULL, " +
                "phaseId INTEGER NOT NULL, " +
                "date TEXT, " +
                "taskContent TEXT, " +
                "estimatedMinutes INTEGER NOT NULL DEFAULT 0, " +
                "actualMinutes INTEGER NOT NULL DEFAULT 0, " +
                "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                "completedAt INTEGER NOT NULL DEFAULT 0, " +
                "taskOrder INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(planId) REFERENCES study_plans(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(phaseId) REFERENCES study_phases(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId ON daily_tasks(planId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_phaseId ON daily_tasks(phaseId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId_date ON daily_tasks(planId, date)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId_date_taskOrder ON daily_tasks(planId, date, taskOrder)");
        }
    };

    // 版本16到17：补救性迁移，确保字段与表存在
    static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN summary TEXT");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN totalDays INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN completedDays INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN streakDays INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN totalStudyTime INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN isAiGenerated INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                database.execSQL("ALTER TABLE study_plans ADD COLUMN dailyMinutes INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {}

            database.execSQL("CREATE TABLE IF NOT EXISTS study_phases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "planId INTEGER NOT NULL, " +
                "phaseOrder INTEGER NOT NULL, " +
                "phaseName TEXT, " +
                "goal TEXT, " +
                "durationDays INTEGER NOT NULL, " +
                "taskTemplateJson TEXT, " +
                "completedDays INTEGER NOT NULL DEFAULT 0, " +
                "progress INTEGER NOT NULL DEFAULT 0, " +
                "status TEXT, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "FOREIGN KEY(planId) REFERENCES study_plans(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_study_phases_planId ON study_phases(planId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_study_phases_planId_phaseOrder ON study_phases(planId, phaseOrder)");

            database.execSQL("CREATE TABLE IF NOT EXISTS daily_tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "planId INTEGER NOT NULL, " +
                "phaseId INTEGER NOT NULL, " +
                "date TEXT, " +
                "taskContent TEXT, " +
                "estimatedMinutes INTEGER NOT NULL DEFAULT 0, " +
                "actualMinutes INTEGER NOT NULL DEFAULT 0, " +
                "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                "completedAt INTEGER NOT NULL DEFAULT 0, " +
                "taskOrder INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(planId) REFERENCES study_plans(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(phaseId) REFERENCES study_phases(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId ON daily_tasks(planId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_phaseId ON daily_tasks(phaseId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId_date ON daily_tasks(planId, date)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_tasks_planId_date_taskOrder ON daily_tasks(planId, date, taskOrder)");
        }
    };

    // 版本17到18：为daily_tasks表添加actionType字段
    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_tasks ADD COLUMN actionType TEXT");
        }
    };

    // 版本18到19：为daily_tasks表添加任务完成条件字段
    static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completionType TEXT DEFAULT 'simple'");
            database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completionTarget INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE daily_tasks ADD COLUMN currentProgress INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    /**
     * 修复旧任务的actionType字段
     * 对于actionType为空的任务，根据任务内容智能推断
     */
    public static void fixOldTasksActionType(Context context) {
        new Thread(() -> {
            try {
                android.util.Log.d("AppDatabase", "========== 开始修复旧任务 ==========");
                AppDatabase db = getInstance(context);
                DailyTaskDao taskDao = db.dailyTaskDao();
                
                // 获取所有actionType为空的任务
                List<DailyTaskEntity> tasks = taskDao.getTasksWithEmptyActionType();
                
                if (tasks == null || tasks.isEmpty()) {
                    android.util.Log.d("AppDatabase", "✓ 没有需要修复的旧任务（所有任务都有actionType）");
                    return;
                }
                
                android.util.Log.d("AppDatabase", "发现 " + tasks.size() + " 个需要修复的任务");
                
                int fixedCount = 0;
                for (DailyTaskEntity task : tasks) {
                    String content = task.getTaskContent();
                    if (content == null || content.isEmpty()) {
                        android.util.Log.w("AppDatabase", "跳过空内容任务 id=" + task.getId());
                        continue;
                    }
                    
                    // 使用ActionTypeInferrer推断actionType
                    String actionType = com.example.mybighomework.utils.ActionTypeInferrer.inferActionType(content);
                    if (actionType == null) {
                        android.util.Log.w("AppDatabase", "无法推断actionType: " + content);
                        continue;
                    }
                    
                    task.setActionType(actionType);
                    
                    // 解析完成条件
                    com.example.mybighomework.utils.CompletionConditionParser.CompletionCondition condition = 
                        com.example.mybighomework.utils.CompletionConditionParser.parse(content);
                    
                    if (task.getCompletionType() == null || task.getCompletionType().isEmpty()) {
                        task.setCompletionType(condition.type);
                    }
                    if (task.getCompletionTarget() <= 0) {
                        task.setCompletionTarget(condition.target);
                    }
                    
                    taskDao.update(task);
                    fixedCount++;
                    android.util.Log.d("AppDatabase", "✓ 修复任务[" + task.getId() + "]: " + content + 
                        " -> actionType=" + actionType + ", type=" + condition.type + ", target=" + condition.target);
                }
                
                android.util.Log.d("AppDatabase", "========== 修复完成: " + fixedCount + "/" + tasks.size() + " ==========");
            } catch (Exception e) {
                android.util.Log.e("AppDatabase", "修复旧任务失败", e);
            }
        }).start();
    }

    public abstract StudyPlanDao studyPlanDao();
    public abstract VocabularyDao vocabularyDao();
    public abstract ExamDao examDao();
    public abstract UserSettingsDao userSettingsDao();
    public abstract QuestionDao questionDao();
    public abstract StudyRecordDao studyRecordDao();
    public abstract UserDao userDao();
    public abstract WrongQuestionDao wrongQuestionDao();
    public abstract DailySentenceDao dailySentenceDao();
    public abstract TranslationHistoryDao translationHistoryDao();
    public abstract ExamAnswerDao examAnswerDao();
    public abstract ExamProgressDao examProgressDao();
    public abstract QuestionNoteDao questionNoteDao();
    public abstract ExamResultDao examResultDao();
    public abstract StudyPhaseDao studyPhaseDao();
    public abstract DailyTaskDao dailyTaskDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .addMigrations(
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19
                    )
                    .build();

                    initializeDefaultSettingsAsync(INSTANCE);
                }
            }
        }
        return INSTANCE;
    }

    private static void initializeDefaultSettingsAsync(AppDatabase database) {
        new Thread(() -> {
            try {
                UserSettingsEntity settings = database.userSettingsDao().getUserSettings();
                if (settings == null) {
                    settings = new UserSettingsEntity();
                    database.userSettingsDao().insert(settings);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}