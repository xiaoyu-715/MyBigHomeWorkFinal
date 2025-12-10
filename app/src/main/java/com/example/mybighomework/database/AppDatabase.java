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
import com.example.mybighomework.database.dao.ExamAnswerDao;
import com.example.mybighomework.database.dao.ExamDao;
import com.example.mybighomework.database.dao.ExamProgressDao;
import com.example.mybighomework.database.dao.ExamResultDao;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.dao.QuestionNoteDao;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.TranslationHistoryDao;
import com.example.mybighomework.database.dao.UserDao;
import com.example.mybighomework.database.dao.UserSettingsDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.dao.WrongQuestionDao;
import com.example.mybighomework.database.entity.DailySentenceEntity;
import com.example.mybighomework.database.entity.ExamAnswerEntity;
import com.example.mybighomework.database.entity.ExamProgressEntity;
import com.example.mybighomework.database.entity.ExamRecordEntity;
import com.example.mybighomework.database.entity.ExamResultEntity;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.database.entity.QuestionNoteEntity;
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
    version = 14,  // 更新版本号（添加vocabularyCorrectCount字段）
    exportSchema = false
)
@TypeConverters({DateConverter.class, StringArrayConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "english_learning_db";
    
    /**
     * 数据库迁移：版本8到版本9
     * 添加totalStudyTime字段到user_settings表，用于统一存储所有学习活动的时长
     */
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加totalStudyTime字段（默认值为0）
            database.execSQL("ALTER TABLE user_settings ADD COLUMN totalStudyTime INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    /**
     * 数据库迁移：版本9到版本10
     * 添加audioUrl, imageUrl, sid字段到daily_sentences表，用于支持金山词霸API数据
     */
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加audioUrl字段（音频URL）
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN audioUrl TEXT");
            // 添加imageUrl字段（图片URL）
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN imageUrl TEXT");
            // 添加sid字段（句子ID）
            database.execSQL("ALTER TABLE daily_sentences ADD COLUMN sid TEXT");
        }
    };
    
    /**
     * 数据库迁移：版本10到版本11
     * 添加exam_progress表，用于保存考试进度支持暂停和继续
     */
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建exam_progress表
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
    
    /**
     * 数据库迁移：版本11到版本12
     * 添加question_notes表，用于保存题目笔记
     */
    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建question_notes表
            database.execSQL("CREATE TABLE IF NOT EXISTS question_notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "exam_title TEXT, " +
                "question_index INTEGER NOT NULL, " +
                "note_content TEXT, " +
                "create_time INTEGER, " +
                "update_time INTEGER)");
        }
    };
    
    /**
     * 数据库迁移：版本12到版本13
     * 添加exam_results表，用于保存考试成绩
     */
    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建exam_results表
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
    
    /**
     * 数据库迁移：版本13到版本14
     * 添加vocabularyCorrectCount字段到user_settings表，用于记录词汇训练答对次数
     */
    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_settings ADD COLUMN vocabularyCorrectCount INTEGER NOT NULL DEFAULT 0");
        }
    };
    
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
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    // 已移除 allowMainThreadQueries()，所有数据库操作必须在后台线程
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                    .fallbackToDestructiveMigration()
                    .build();
                    
                    // 在后台线程初始化默认用户设置
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