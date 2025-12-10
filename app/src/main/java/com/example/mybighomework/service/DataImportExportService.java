package com.example.mybighomework.service;

import android.content.Context;
import android.util.Log;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据导入导出服务类
 * 支持批量导入导出词汇和题目数据
 */
public class DataImportExportService {
    
    private static final String TAG = "DataImportExportService";
    private final QuestionDao questionDao;
    private final VocabularyDao vocabularyDao;
    private final Gson gson;
    
    public DataImportExportService(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.questionDao = database.questionDao();
        this.vocabularyDao = database.vocabularyDao();
        this.gson = new Gson();
    }
    
    /**
     * 从JSON文件导入词汇数据
     */
    public ImportResult importVocabulariesFromJson(String filePath) {
        ImportResult result = new ImportResult();
        
        try {
            String jsonContent = readFileContent(filePath);
            Type listType = new TypeToken<List<VocabularyImportData>>(){}.getType();
            List<VocabularyImportData> importDataList = gson.fromJson(jsonContent, listType);
            
            List<VocabularyRecordEntity> vocabularies = new ArrayList<>();
            
            for (VocabularyImportData data : importDataList) {
                VocabularyRecordEntity vocabulary = new VocabularyRecordEntity();
                vocabulary.setWord(data.word);
                vocabulary.setMeaning(data.meaning);
                vocabulary.setPronunciation(data.pronunciation);
                vocabulary.setExample(data.example);
                vocabulary.setSynonyms(data.synonyms);
                vocabulary.setAntonyms(data.antonyms);
                vocabulary.setWordType(data.wordType);
                vocabulary.setCollocations(data.collocations);
                vocabulary.setExampleSentences(data.exampleSentences);
                vocabulary.setEtymology(data.etymology);
                vocabulary.setTags(data.tags);
                vocabulary.setLevel(data.level);
                vocabulary.setFrequency(data.frequency);
                vocabulary.setCreatedTime(System.currentTimeMillis());
                
                vocabularies.add(vocabulary);
                result.successCount++;
            }
            
            vocabularyDao.insertVocabularies(vocabularies);
            result.success = true;
            
        } catch (Exception e) {
            Log.e(TAG, "导入词汇数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 从JSON文件导入题目数据
     */
    public ImportResult importQuestionsFromJson(String filePath) {
        ImportResult result = new ImportResult();
        
        try {
            String jsonContent = readFileContent(filePath);
            Type listType = new TypeToken<List<QuestionImportData>>(){}.getType();
            List<QuestionImportData> importDataList = gson.fromJson(jsonContent, listType);
            
            List<QuestionEntity> questions = new ArrayList<>();
            
            for (QuestionImportData data : importDataList) {
                QuestionEntity question = new QuestionEntity();
                question.setQuestionText(data.questionText);
                question.setOptions(data.options);
                question.setCorrectAnswer(Integer.parseInt(data.correctAnswer));
                question.setExplanation(data.explanation);
                question.setCategory(data.category);
                question.setExamType(data.examType);
                question.setDifficulty(data.difficulty);
                question.setSource(data.source);
                question.setYear(Integer.parseInt(data.year));
                question.setTags(String.join(",", data.tags));
                question.setCreatedTime(new Date());
                question.setActive(true);
                
                questions.add(question);
                result.successCount++;
            }
            
            questionDao.insertQuestions(questions);
            result.success = true;
            
        } catch (Exception e) {
            Log.e(TAG, "导入题目数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 从CSV文件导入词汇数据
     */
    public ImportResult importVocabulariesFromCsv(String filePath) {
        ImportResult result = new ImportResult();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            List<VocabularyRecordEntity> vocabularies = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳过标题行
                }
                
                String[] fields = line.split(",");
                if (fields.length >= 4) {
                    VocabularyRecordEntity vocabulary = new VocabularyRecordEntity();
                    vocabulary.setWord(fields[0].trim());
                    vocabulary.setMeaning(fields[1].trim());
                    vocabulary.setPronunciation(fields[2].trim());
                    vocabulary.setExample(fields[3].trim());
                    
                    // 可选字段
                    if (fields.length > 4) vocabulary.setWordType(fields[4].trim());
                    if (fields.length > 5) vocabulary.setLevel(fields[5].trim());
                    if (fields.length > 6) {
                        try {
                            vocabulary.setFrequency(Integer.parseInt(fields[6].trim()));
                        } catch (NumberFormatException e) {
                            vocabulary.setFrequency(0);
                        }
                    }
                    
                    vocabulary.setCreatedTime(System.currentTimeMillis());
                    vocabularies.add(vocabulary);
                    result.successCount++;
                }
            }
            
            vocabularyDao.insertVocabularies(vocabularies);
            result.success = true;
            
        } catch (Exception e) {
            Log.e(TAG, "从CSV导入词汇数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 从Assets文件夹导入预设数据
     */
    public ImportResult importFromAssets(Context context, String assetFileName, DataType dataType) {
        ImportResult result = new ImportResult();
        
        try {
            InputStream inputStream = context.getAssets().open(assetFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            
            reader.close();
            inputStream.close();
            
            if (dataType == DataType.VOCABULARY) {
                result = importVocabulariesFromJsonContent(content.toString());
            } else if (dataType == DataType.QUESTION) {
                result = importQuestionsFromJsonContent(content.toString());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "从Assets导入数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 导出词汇数据到JSON文件
     */
    public ExportResult exportVocabulariesToJson(String filePath) {
        ExportResult result = new ExportResult();
        
        try {
            List<VocabularyRecordEntity> vocabularies = vocabularyDao.getAllVocabulary();
            List<VocabularyExportData> exportDataList = new ArrayList<>();
            
            for (VocabularyRecordEntity vocabulary : vocabularies) {
                VocabularyExportData data = new VocabularyExportData();
                data.word = vocabulary.getWord();
                data.meaning = vocabulary.getMeaning();
                data.pronunciation = vocabulary.getPronunciation();
                data.example = vocabulary.getExample();
                data.synonyms = vocabulary.getSynonyms();
                data.antonyms = vocabulary.getAntonyms();
                data.wordType = vocabulary.getWordType();
                data.collocations = vocabulary.getCollocations();
                data.exampleSentences = vocabulary.getExampleSentences();
                data.etymology = vocabulary.getEtymology();
                data.tags = vocabulary.getTags();
                data.level = vocabulary.getLevel();
                data.frequency = vocabulary.getFrequency();
                data.correctCount = vocabulary.getCorrectCount();
                data.wrongCount = vocabulary.getWrongCount();
                data.isMastered = vocabulary.isMastered();
                
                exportDataList.add(data);
                result.exportedCount++;
            }
            
            String jsonContent = gson.toJson(exportDataList);
            writeFileContent(filePath, jsonContent);
            result.success = true;
            
        } catch (Exception e) {
            Log.e(TAG, "导出词汇数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 导出题目数据到JSON文件
     */
    public ExportResult exportQuestionsToJson(String filePath) {
        ExportResult result = new ExportResult();
        
        try {
            List<QuestionEntity> questions = questionDao.getAllActiveQuestions();
            List<QuestionExportData> exportDataList = new ArrayList<>();
            
            for (QuestionEntity question : questions) {
                QuestionExportData data = new QuestionExportData();
                data.questionText = question.getQuestionText();
                data.options = question.getOptions();
                data.correctAnswer = String.valueOf(question.getCorrectAnswer());
                data.explanation = question.getExplanation();
                data.category = question.getCategory();
                data.examType = question.getExamType();
                data.difficulty = question.getDifficulty();
                data.source = question.getSource();
                data.year = String.valueOf(question.getYear());
                data.tags = question.getTags() != null ? question.getTags().split(",") : new String[0];
                data.totalAttempts = question.getTotalAttempts();
                data.correctAttempts = question.getCorrectAttempts();
                data.accuracyRate = question.getAccuracyRate();
                
                exportDataList.add(data);
                result.exportedCount++;
            }
            
            String jsonContent = gson.toJson(exportDataList);
            writeFileContent(filePath, jsonContent);
            result.success = true;
            
        } catch (Exception e) {
            Log.e(TAG, "导出题目数据失败", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 批量导入预设的英语词汇数据
     */
    public ImportResult importPresetVocabularies() {
        List<VocabularyRecordEntity> vocabularies = createPresetVocabularies();
        
        try {
            vocabularyDao.insertVocabularies(vocabularies);
            
            ImportResult result = new ImportResult();
            result.success = true;
            result.successCount = vocabularies.size();
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "导入预设词汇失败", e);
            ImportResult result = new ImportResult();
            result.success = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }
    
    /**
     * 批量导入预设的题目数据
     */
    public ImportResult importPresetQuestions() {
        List<QuestionEntity> questions = createPresetQuestions();
        
        try {
            questionDao.insertQuestions(questions);
            
            ImportResult result = new ImportResult();
            result.success = true;
            result.successCount = questions.size();
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "导入预设题目失败", e);
            ImportResult result = new ImportResult();
            result.success = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }
    
    // 辅助方法
    private String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
    
    private void writeFileContent(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }
    
    private ImportResult importVocabulariesFromJsonContent(String jsonContent) {
        ImportResult result = new ImportResult();
        
        try {
            Type listType = new TypeToken<List<VocabularyImportData>>(){}.getType();
            List<VocabularyImportData> importDataList = gson.fromJson(jsonContent, listType);
            
            List<VocabularyRecordEntity> vocabularies = new ArrayList<>();
            
            for (VocabularyImportData data : importDataList) {
                VocabularyRecordEntity vocabulary = new VocabularyRecordEntity();
                vocabulary.setWord(data.word);
                vocabulary.setMeaning(data.meaning);
                vocabulary.setPronunciation(data.pronunciation);
                vocabulary.setExample(data.example);
                vocabulary.setCreatedTime(System.currentTimeMillis());
                
                vocabularies.add(vocabulary);
                result.successCount++;
            }
            
            vocabularyDao.insertVocabularies(vocabularies);
            result.success = true;
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    private ImportResult importQuestionsFromJsonContent(String jsonContent) {
        ImportResult result = new ImportResult();
        
        try {
            Type listType = new TypeToken<List<QuestionImportData>>(){}.getType();
            List<QuestionImportData> importDataList = gson.fromJson(jsonContent, listType);
            
            List<QuestionEntity> questions = new ArrayList<>();
            
            for (QuestionImportData data : importDataList) {
                QuestionEntity question = new QuestionEntity();
                question.setQuestionText(data.questionText);
                question.setOptions(data.options);
                question.setCorrectAnswer(Integer.parseInt(data.correctAnswer));
                question.setExplanation(data.explanation);
                question.setCategory(data.category);
                question.setExamType(data.examType);
                question.setDifficulty(data.difficulty);
                question.setSource(data.source);
                question.setYear(Integer.parseInt(data.year));
                question.setTags(String.join(",", data.tags));
                question.setCreatedTime(new Date());
                question.setActive(true);
                
                questions.add(question);
                result.successCount++;
            }
            
            questionDao.insertQuestions(questions);
            result.success = true;
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    private List<VocabularyRecordEntity> createPresetVocabularies() {
        List<VocabularyRecordEntity> vocabularies = new ArrayList<>();
        
        // 添加更多预设词汇
        String[][] presetData = {
            {"abandon", "放弃", "/əˈbændən/", "He decided to abandon his plan.", "noun", "CET4", "verb"},
            {"ability", "能力", "/əˈbɪləti/", "She has the ability to solve problems.", "noun", "CET4", "noun"},
            {"absolute", "绝对的", "/ˈæbsəluːt/", "There is no absolute truth.", "adjective", "CET4", "adjective"},
            {"academic", "学术的", "/ˌækəˈdemɪk/", "He has strong academic performance.", "adjective", "CET4", "adjective"},
            {"accept", "接受", "/əkˈsept/", "I accept your invitation.", "verb", "CET4", "verb"},
            {"access", "访问", "/ˈækses/", "Students have access to the library.", "noun", "CET4", "noun"},
            {"accident", "事故", "/ˈæksɪdənt/", "There was a car accident yesterday.", "noun", "CET4", "noun"},
            {"accompany", "陪伴", "/əˈkʌmpəni/", "I will accompany you to the station.", "verb", "CET4", "verb"},
            {"accomplish", "完成", "/əˈkʌmplɪʃ/", "We accomplished our mission.", "verb", "CET4", "verb"},
            {"account", "账户", "/əˈkaʊnt/", "Please check your bank account.", "noun", "CET4", "noun"}
        };
        
        for (String[] data : presetData) {
            VocabularyRecordEntity vocabulary = new VocabularyRecordEntity();
            vocabulary.setWord(data[0]);
            vocabulary.setMeaning(data[1]);
            vocabulary.setPronunciation(data[2]);
            vocabulary.setExample(data[3]);
            vocabulary.setWordType(data[6]);
            vocabulary.setLevel(data[5]);
            vocabulary.setFrequency(1000);
            vocabulary.setCreatedTime(System.currentTimeMillis());
            
            vocabularies.add(vocabulary);
        }
        
        return vocabularies;
    }
    
    private List<QuestionEntity> createPresetQuestions() {
        List<QuestionEntity> questions = new ArrayList<>();
        
        // 添加更多预设题目
        String[][] questionData = {
            {"What does 'abandon' mean?", "A. keep|B. give up|C. find|D. create", "B", "Abandon means to give up or leave behind.", "vocabulary", "CET4", "easy"},
            {"Choose the correct form: 'She has the _____ to succeed.'", "A. able|B. ability|C. abilities|D. abled", "B", "Ability is the correct noun form.", "grammar", "CET4", "medium"},
            {"Which word means 'complete'?", "A. abandon|B. accept|C. accomplish|D. access", "C", "Accomplish means to complete or achieve.", "vocabulary", "CET4", "easy"},
            {"Fill in the blank: 'Students have _____ to the library.'", "A. access|B. accident|C. account|D. accept", "A", "Access means the right or opportunity to use something.", "vocabulary", "CET4", "medium"},
            {"What is the past tense of 'accompany'?", "A. accompanyed|B. accompanied|C. accompanying|D. accompanies", "B", "The past tense of accompany is accompanied.", "grammar", "CET4", "medium"}
        };
        
        for (String[] data : questionData) {
            QuestionEntity question = new QuestionEntity();
            question.setQuestionText(data[0]);
            question.setOptions(data[1].split("\\|"));
            // Convert answer letter to index (A=0, B=1, C=2, D=3)
            int correctAnswerIndex = data[2].charAt(0) - 'A';
            question.setCorrectAnswer(correctAnswerIndex);
            question.setExplanation(data[3]);
            question.setCategory(data[4]);
            question.setExamType(data[5]);
            question.setDifficulty(data[6]);
            question.setSource("preset");
            question.setYear(2024);
            question.setTags("");
            question.setCreatedTime(new Date());
            question.setActive(true);
            
            questions.add(question);
        }
        
        return questions;
    }
    
    // 数据类型枚举
    public enum DataType {
        VOCABULARY, QUESTION
    }
    
    // 导入结果类
    public static class ImportResult {
        public boolean success;
        public int successCount;
        public String errorMessage;
    }
    
    // 导出结果类
    public static class ExportResult {
        public boolean success;
        public int exportedCount;
        public String errorMessage;
    }
    
    // 词汇导入数据类
    public static class VocabularyImportData {
        public String word;
        public String meaning;
        public String pronunciation;
        public String example;
        public String[] synonyms;
        public String[] antonyms;
        public String wordType;
        public String[] collocations;
        public String[] exampleSentences;
        public String etymology;
        public String[] tags;
        public String level;
        public int frequency;
    }
    
    // 题目导入数据类
    public static class QuestionImportData {
        public String questionText;
        public String[] options;
        public String correctAnswer;
        public String explanation;
        public String category;
        public String examType;
        public String difficulty;
        public String source;
        public String year;
        public String[] tags;
    }
    
    // 词汇导出数据类
    public static class VocabularyExportData {
        public String word;
        public String meaning;
        public String pronunciation;
        public String example;
        public String[] synonyms;
        public String[] antonyms;
        public String wordType;
        public String[] collocations;
        public String[] exampleSentences;
        public String etymology;
        public String[] tags;
        public String level;
        public int frequency;
        public int correctCount;
        public int wrongCount;
        public boolean isMastered;
    }
    
    // 题目导出数据类
    public static class QuestionExportData {
        public String questionText;
        public String[] options;
        public String correctAnswer;
        public String explanation;
        public String category;
        public String examType;
        public String difficulty;
        public String source;
        public String year;
        public String[] tags;
        public int totalAttempts;
        public int correctAttempts;
        public double accuracyRate;
    }
}