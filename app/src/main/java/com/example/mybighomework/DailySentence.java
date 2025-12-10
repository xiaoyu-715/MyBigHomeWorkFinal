package com.example.mybighomework;

import java.util.ArrayList;
import java.util.List;

public class DailySentence {
    private String englishText;
    private String chineseText;
    private String author;
    private String date;
    private List<Vocabulary> vocabularyList;
    private boolean isFavorited;

    public DailySentence(String englishText, String chineseText, String author, String date) {
        this.englishText = englishText;
        this.chineseText = chineseText;
        this.author = author;
        this.date = date;
        this.vocabularyList = new ArrayList<>();
        this.isFavorited = false;
    }

    // Getters and Setters
    public String getEnglishText() {
        return englishText;
    }

    public void setEnglishText(String englishText) {
        this.englishText = englishText;
    }

    public String getChineseText() {
        return chineseText;
    }

    public void setChineseText(String chineseText) {
        this.chineseText = chineseText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Vocabulary> getVocabularyList() {
        return vocabularyList;
    }

    public void setVocabularyList(List<Vocabulary> vocabularyList) {
        this.vocabularyList = vocabularyList;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    // 添加词汇
    public void addVocabulary(String word, String meaning) {
        vocabularyList.add(new Vocabulary(word, meaning));
    }

    // 词汇内部类
    public static class Vocabulary {
        private String word;
        private String meaning;

        public Vocabulary(String word, String meaning) {
            this.word = word;
            this.meaning = meaning;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getMeaning() {
            return meaning;
        }

        public void setMeaning(String meaning) {
            this.meaning = meaning;
        }
    }
}