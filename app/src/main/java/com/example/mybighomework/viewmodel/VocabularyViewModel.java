package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VocabularyActivity 的 ViewModel
 * 负责管理词汇训练的数据和业务逻辑
 */
public class VocabularyViewModel extends AndroidViewModel {
    
    private final VocabularyRecordRepository vocabularyRepository;
    private final StudyRecordRepository studyRecordRepository;
    
    // 训练数据
    private final MutableLiveData<List<VocabularyItem>> vocabularyList = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isAnswered = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // 统计数据
    private final MutableLiveData<Integer> correctAnswers = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> wrongAnswers = new MutableLiveData<>(0);
    private long trainingStartTime;
    
    private static final int TOTAL_QUESTIONS = 10;
    
    /**
     * 词汇数据类
     */
    public static class VocabularyItem {
        public String word;
        public String phonetic;
        public String meaning;
        public String[] options;
        public int correctAnswer;
        
        public VocabularyItem(String word, String phonetic, String meaning, 
                            String[] options, int correctAnswer) {
            this.word = word;
            this.phonetic = phonetic;
            this.meaning = meaning;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }
    
    public VocabularyViewModel(@NonNull Application application) {
        super(application);
        
        // 初始化 Repository
        AppDatabase database = AppDatabase.getInstance(application);
        vocabularyRepository = new VocabularyRecordRepository(database.vocabularyDao());
        studyRecordRepository = new StudyRecordRepository(database.studyRecordDao());
        
        // 记录训练开始时间
        trainingStartTime = System.currentTimeMillis();
    }
    
    // ==================== LiveData Getters ====================
    
    public LiveData<List<VocabularyItem>> getVocabularyList() {
        return vocabularyList;
    }
    
    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    public LiveData<Integer> getScore() {
        return score;
    }
    
    public LiveData<Boolean> getIsAnswered() {
        return isAnswered;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Integer> getCorrectAnswers() {
        return correctAnswers;
    }
    
    public LiveData<Integer> getWrongAnswers() {
        return wrongAnswers;
    }
    
    public int getTotalQuestions() {
        return TOTAL_QUESTIONS;
    }
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 初始化词汇数据
     */
    public void initVocabularyData() {
        isLoading.setValue(true);
        
        new Thread(() -> {
            try {
                List<VocabularyItem> items = generateVocabularyData();
                vocabularyList.postValue(items);
                isLoading.postValue(false);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("加载词汇数据失败: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }
    
    /**
     * 生成词汇数据（示例数据，实际应从数据库或网络获取）
     */
    private List<VocabularyItem> generateVocabularyData() {
        List<VocabularyItem> items = new ArrayList<>();
        
        // 示例词汇数据
        items.add(new VocabularyItem("abandon", "/əˈbændən/", "放弃，遗弃",
            new String[]{"放弃", "保持", "继续", "开始"}, 0));
        items.add(new VocabularyItem("ability", "/əˈbɪləti/", "能力，才能",
            new String[]{"困难", "能力", "责任", "机会"}, 1));
        items.add(new VocabularyItem("abroad", "/əˈbrɔːd/", "到国外，在国外",
            new String[]{"在家", "在学校", "在国外", "在公司"}, 2));
        items.add(new VocabularyItem("absence", "/ˈæbsəns/", "缺席，不在场",
            new String[]{"出席", "存在", "缺席", "到达"}, 2));
        items.add(new VocabularyItem("academic", "/ˌækəˈdemɪk/", "学术的，学院的",
            new String[]{"商业的", "政治的", "学术的", "娱乐的"}, 2));
        items.add(new VocabularyItem("accept", "/əkˈsept/", "接受，承认",
            new String[]{"拒绝", "接受", "忽略", "怀疑"}, 1));
        items.add(new VocabularyItem("accident", "/ˈæksɪdənt/", "事故，意外",
            new String[]{"计划", "成功", "事故", "目标"}, 2));
        items.add(new VocabularyItem("achieve", "/əˈtʃiːv/", "达到，完成",
            new String[]{"失败", "放弃", "尝试", "达到"}, 3));
        items.add(new VocabularyItem("acquire", "/əˈkwaɪər/", "获得，学到",
            new String[]{"失去", "忘记", "获得", "借用"}, 2));
        items.add(new VocabularyItem("active", "/ˈæktɪv/", "积极的，活跃的",
            new String[]{"消极的", "被动的", "积极的", "安静的"}, 2));
        
        // 打乱顺序
        Collections.shuffle(items);
        
        // 返回前10个
        return items.size() > TOTAL_QUESTIONS ? 
            items.subList(0, TOTAL_QUESTIONS) : items;
    }
    
    /**
     * 选择答案
     */
    public void selectOption(int selectedOption) {
        if (Boolean.TRUE.equals(isAnswered.getValue())) {
            return;
        }
        
        isAnswered.setValue(true);
        
        List<VocabularyItem> items = vocabularyList.getValue();
        if (items == null || items.isEmpty()) {
            return;
        }
        
        int index = currentQuestionIndex.getValue() != null ? 
            currentQuestionIndex.getValue() : 0;
        
        if (index >= items.size()) {
            return;
        }
        
        VocabularyItem currentItem = items.get(index);
        boolean isCorrect = selectedOption == currentItem.correctAnswer;
        
        if (isCorrect) {
            // 答对了
            int currentScore = score.getValue() != null ? score.getValue() : 0;
            score.setValue(currentScore + 10);
            
            int correct = correctAnswers.getValue() != null ? correctAnswers.getValue() : 0;
            correctAnswers.setValue(correct + 1);
        } else {
            // 答错了
            int wrong = wrongAnswers.getValue() != null ? wrongAnswers.getValue() : 0;
            wrongAnswers.setValue(wrong + 1);
        }
        
        // 保存词汇学习记录
        saveVocabularyRecord(currentItem, isCorrect);
    }
    
    /**
     * 下一题
     */
    public void nextQuestion() {
        int index = currentQuestionIndex.getValue() != null ? 
            currentQuestionIndex.getValue() : 0;
        currentQuestionIndex.setValue(index + 1);
        isAnswered.setValue(false);
    }
    
    /**
     * 重新开始
     */
    public void restartTraining() {
        currentQuestionIndex.setValue(0);
        score.setValue(0);
        correctAnswers.setValue(0);
        wrongAnswers.setValue(0);
        isAnswered.setValue(false);
        trainingStartTime = System.currentTimeMillis();
        
        // 重新生成词汇数据
        initVocabularyData();
    }
    
    /**
     * 保存词汇学习记录
     */
    private void saveVocabularyRecord(VocabularyItem item, boolean isCorrect) {
        new Thread(() -> {
            try {
                // 检查词汇是否已存在
                VocabularyRecordEntity existingRecord = 
                    vocabularyRepository.getVocabularyByWord(item.word);
                
                if (existingRecord != null) {
                    // 更新现有记录
                    if (isCorrect) {
                        vocabularyRepository.incrementCorrectCountAsync(
                            existingRecord.getId(), 
                            System.currentTimeMillis()
                        );
                    } else {
                        vocabularyRepository.incrementWrongCountAsync(
                            existingRecord.getId(), 
                            System.currentTimeMillis()
                        );
                    }
                } else {
                    // 创建新记录
                    VocabularyRecordEntity record = new VocabularyRecordEntity();
                    record.setWord(item.word);
                    record.setPronunciation(item.phonetic);
                    record.setMeaning(item.meaning);
                    record.setCorrectCount(isCorrect ? 1 : 0);
                    record.setWrongCount(isCorrect ? 0 : 1);
                    record.setLastStudyTime(System.currentTimeMillis());
                    
                    vocabularyRepository.addVocabularyRecordAsync(record, 
                        new VocabularyRecordRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess(long result) {
                                // 保存成功
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                                errorMessage.postValue("保存词汇记录失败");
                            }
                        }
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("保存词汇记录时出错");
            }
        }).start();
    }
    
    /**
     * 保存训练记录
     */
    public void saveTrainingRecord(OnSaveCompleteListener listener) {
        new Thread(() -> {
            try {
                long trainingEndTime = System.currentTimeMillis();
                long duration = trainingEndTime - trainingStartTime;
                
                StudyRecordEntity record = new StudyRecordEntity();
                record.setStudyType("词汇训练");
                record.setQuestionId(0);
                record.setVocabularyId(0);
                record.setCorrect((correctAnswers.getValue() != null ? 
                    correctAnswers.getValue() : 0) > 
                    (wrongAnswers.getValue() != null ? wrongAnswers.getValue() : 0));
                record.setResponseTime(duration);
                record.setScore(score.getValue() != null ? score.getValue() : 0);
                record.setCreatedTime(trainingStartTime);
                record.setStudyDate(new java.util.Date()); // 显式设置学习日期
                record.setNotes("词汇训练 - 正确:" + 
                    (correctAnswers.getValue() != null ? correctAnswers.getValue() : 0) + 
                    " 错误:" + 
                    (wrongAnswers.getValue() != null ? wrongAnswers.getValue() : 0));
                
                studyRecordRepository.addStudyRecord(record);
                
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * 保存完成回调接口
     */
    public interface OnSaveCompleteListener {
        void onSuccess();
        void onError(Exception e);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // ViewModel 被销毁时，清理资源
        vocabularyRepository.shutdown();
    }
}

