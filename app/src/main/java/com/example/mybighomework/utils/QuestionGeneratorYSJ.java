package com.example.mybighomework.utils;

import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 题目生成器
 * 根据单词列表生成选择题
 */
public class QuestionGeneratorYSJ {
    
    private Random random;
    
    public QuestionGeneratorYSJ() {
        this.random = new Random();
    }
    
    /**
     * 生成单个选择题
     * @param targetWord 目标单词
     * @param allWords 所有单词列表(用于生成干扰项)
     * @return 题目数据
     */
    public VocabularyQuestion generateQuestion(DictionaryWordEntity targetWord, List<DictionaryWordEntity> allWords) {
        // 正确答案
        String correctAnswer = targetWord.getTranslation();
        
        // 生成3个干扰项
        List<String> distractors = generateDistractors(targetWord, allWords, 3);
        
        // 组合选项并打乱
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.addAll(distractors);
        Collections.shuffle(options);
        
        // 找到正确答案的索引
        int correctIndex = options.indexOf(correctAnswer);
        
        return new VocabularyQuestion(
            targetWord.getWord(),
            targetWord.getDisplayPhonetic(),
            targetWord.getTranslation(),
            options.toArray(new String[0]),
            correctIndex
        );
    }
    
    /**
     * 生成干扰项
     * @param targetWord 目标单词
     * @param allWords 所有单词列表
     * @param count 需要的干扰项数量
     * @return 干扰项列表
     */
    private List<String> generateDistractors(DictionaryWordEntity targetWord, List<DictionaryWordEntity> allWords, int count) {
        List<String> distractors = new ArrayList<>();
        List<DictionaryWordEntity> candidates = new ArrayList<>(allWords);
        
        // 移除目标单词
        candidates.removeIf(word -> word.getId().equals(targetWord.getId()));
        
        // 打乱候选列表
        Collections.shuffle(candidates);
        
        // 选择前N个作为干扰项
        for (int i = 0; i < Math.min(count, candidates.size()); i++) {
            String translation = candidates.get(i).getTranslation();
            if (translation != null && !translation.isEmpty()) {
                distractors.add(translation);
            }
        }
        
        // 如果干扰项不够,补充默认选项
        while (distractors.size() < count) {
            distractors.add("其他选项 " + (distractors.size() + 1));
        }
        
        return distractors;
    }
    
    /**
     * 批量生成题目
     * @param targetWords 目标单词列表
     * @param allWords 所有单词列表(用于生成干扰项)
     * @return 题目列表
     */
    public List<VocabularyQuestion> generateQuestions(List<DictionaryWordEntity> targetWords, List<DictionaryWordEntity> allWords) {
        List<VocabularyQuestion> questions = new ArrayList<>();
        
        for (DictionaryWordEntity word : targetWords) {
            VocabularyQuestion question = generateQuestion(word, allWords);
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * 题目数据类
     */
    public static class VocabularyQuestion {
        private String word;
        private String phonetic;
        private String meaning;
        private String[] options;
        private int correctIndex;
        
        public VocabularyQuestion(String word, String phonetic, String meaning, String[] options, int correctIndex) {
            this.word = word;
            this.phonetic = phonetic;
            this.meaning = meaning;
            this.options = options;
            this.correctIndex = correctIndex;
        }
        
        public String getWord() { return word; }
        public String getPhonetic() { return phonetic; }
        public String getMeaning() { return meaning; }
        public String[] getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
        
        public boolean isCorrect(int selectedIndex) {
            return selectedIndex == correctIndex;
        }
    }
}
