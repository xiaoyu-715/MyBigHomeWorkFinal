package com.example.mybighomework.di;

import android.content.Context;
import android.app.Application;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.repository.DailySentenceRepository;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.QuestionRepository;
import com.example.mybighomework.repository.StudyPlanRepository;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import com.example.mybighomework.repository.WrongQuestionRepository;

/**
 * 简单的服务定位器模式实现依赖注入
 * 用于管理所有Repository实例，避免在每个Activity中重复创建
 * 
 * 使用方式:
 * ServiceLocatorYSJ.init(applicationContext);
 * VocabularyRecordRepository repo = ServiceLocatorYSJ.getVocabularyRecordRepository();
 */
public class ServiceLocatorYSJ {
    
    private static volatile ServiceLocatorYSJ INSTANCE;
    
    private final Context applicationContext;
    private final AppDatabase database;
    
    // Repository实例（懒加载）
    private VocabularyRecordRepository vocabularyRecordRepository;
    private StudyPlanRepository studyPlanRepository;
    private StudyRecordRepository studyRecordRepository;
    private ExamRecordRepository examRecordRepository;
    private WrongQuestionRepository wrongQuestionRepository;
    private DailySentenceRepository dailySentenceRepository;
    private UserSettingsRepository userSettingsRepository;
    private QuestionRepository questionRepository;
    
    private ServiceLocatorYSJ(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(applicationContext);
    }
    
    /**
     * 初始化服务定位器（在Application中调用）
     */
    public static void init(Context context) {
        if (INSTANCE == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocatorYSJ(context);
                }
            }
        }
    }
    
    /**
     * 获取服务定位器实例
     */
    public static ServiceLocatorYSJ getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ServiceLocatorYSJ未初始化，请在Application中调用init()");
        }
        return INSTANCE;
    }
    
    // ==================== Repository Getters ====================
    
    public static VocabularyRecordRepository getVocabularyRecordRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.vocabularyRecordRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.vocabularyRecordRepository == null) {
                    instance.vocabularyRecordRepository = new VocabularyRecordRepository(
                        instance.database.vocabularyDao()
                    );
                }
            }
        }
        return instance.vocabularyRecordRepository;
    }
    
    public static StudyPlanRepository getStudyPlanRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.studyPlanRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.studyPlanRepository == null) {
                    instance.studyPlanRepository = new StudyPlanRepository(
                        (Application) instance.applicationContext,
                        instance.database.studyPlanDao(),
                        instance.database.studyPhaseDao(),
                        instance.database.dailyTaskDao()
                    );
                }
            }
        }
        return instance.studyPlanRepository;
    }
    
    public static StudyRecordRepository getStudyRecordRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.studyRecordRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.studyRecordRepository == null) {
                    instance.studyRecordRepository = new StudyRecordRepository(
                        instance.database.studyRecordDao()
                    );
                }
            }
        }
        return instance.studyRecordRepository;
    }
    
    public static ExamRecordRepository getExamRecordRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.examRecordRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.examRecordRepository == null) {
                    instance.examRecordRepository = new ExamRecordRepository(
                        instance.database.examDao()
                    );
                }
            }
        }
        return instance.examRecordRepository;
    }
    
    public static WrongQuestionRepository getWrongQuestionRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.wrongQuestionRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.wrongQuestionRepository == null) {
                    instance.wrongQuestionRepository = new WrongQuestionRepository(
                        instance.database.wrongQuestionDao()
                    );
                }
            }
        }
        return instance.wrongQuestionRepository;
    }
    
    public static DailySentenceRepository getDailySentenceRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.dailySentenceRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.dailySentenceRepository == null) {
                    instance.dailySentenceRepository = new DailySentenceRepository(
                        instance.applicationContext
                    );
                }
            }
        }
        return instance.dailySentenceRepository;
    }
    
    public static UserSettingsRepository getUserSettingsRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.userSettingsRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.userSettingsRepository == null) {
                    instance.userSettingsRepository = new UserSettingsRepository(
                        instance.applicationContext
                    );
                }
            }
        }
        return instance.userSettingsRepository;
    }
    
    public static QuestionRepository getQuestionRepository() {
        ServiceLocatorYSJ instance = getInstance();
        if (instance.questionRepository == null) {
            synchronized (ServiceLocatorYSJ.class) {
                if (instance.questionRepository == null) {
                    instance.questionRepository = new QuestionRepository(
                        (android.app.Application) instance.applicationContext
                    );
                }
            }
        }
        return instance.questionRepository;
    }
    
    public static AppDatabase getDatabase() {
        return getInstance().database;
    }
    
    public static Context getApplicationContext() {
        return getInstance().applicationContext;
    }
}
