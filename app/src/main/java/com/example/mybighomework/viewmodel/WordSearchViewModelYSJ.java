package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.entity.DictionaryWordEntity;
import com.example.mybighomework.database.entity.ExampleSentenceEntity;
import com.example.mybighomework.database.entity.SearchHistoryEntity;
import com.example.mybighomework.database.entity.UserWordCollectionEntity;
import com.example.mybighomework.database.repository.DictionaryWordRepository;
import com.example.mybighomework.database.repository.ExampleSentenceRepositoryYSJ;
import com.example.mybighomework.database.repository.SearchHistoryRepositoryYSJ;
import com.example.mybighomework.database.repository.UserWordCollectionRepositoryYSJ;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单词搜索功能ViewModel
 * 管理搜索界面的数据和业务逻辑
 */
public class WordSearchViewModelYSJ extends AndroidViewModel {
    
    private final DictionaryWordRepository wordRepository;
    private final ExampleSentenceRepositoryYSJ sentenceRepository;
    private final UserWordCollectionRepositoryYSJ collectionRepository;
    private final SearchHistoryRepositoryYSJ historyRepository;
    private final ExecutorService executor;
    
    // 搜索结果
    private final MutableLiveData<List<DictionaryWordEntity>> searchResults = new MutableLiveData<>();
    
    // 当前选中的单词详情
    private final MutableLiveData<DictionaryWordEntity> selectedWord = new MutableLiveData<>();
    
    // 当前单词的例句
    private final MutableLiveData<List<ExampleSentenceEntity>> currentExamples = new MutableLiveData<>();
    
    // 收藏状态
    private final MutableLiveData<Boolean> isCollected = new MutableLiveData<>(false);
    
    // 加载状态
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // 错误信息
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // 当前用户ID
    private String currentUserId = "default";
    
    public WordSearchViewModelYSJ(@NonNull Application application) {
        super(application);
        wordRepository = new DictionaryWordRepository(application);
        sentenceRepository = new ExampleSentenceRepositoryYSJ(application);
        collectionRepository = new UserWordCollectionRepositoryYSJ(application);
        historyRepository = new SearchHistoryRepositoryYSJ(application);
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 设置当前用户ID
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }
    
    /**
     * 搜索单词
     */
    public void searchWords(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }
        
        isLoading.setValue(true);
        
        // 在后台线程执行数据库操作
        executor.execute(() -> {
            // 添加搜索历史
            historyRepository.addSearchHistory(keyword.trim(), currentUserId);
            
            // 执行搜索
            List<DictionaryWordEntity> results = wordRepository.searchWordsSync(keyword.trim() + "%");
            searchResults.postValue(results != null ? results : new ArrayList<>());
            isLoading.postValue(false);
        });
    }
    
    /**
     * 获取搜索结果LiveData
     */
    public LiveData<List<DictionaryWordEntity>> getSearchResults() {
        return searchResults;
    }
    
    /**
     * 选择单词查看详情
     */
    public void selectWord(DictionaryWordEntity word) {
        selectedWord.setValue(word);
        
        if (word != null) {
            // 加载例句
            loadExamples(word.getId());
            
            // 检查收藏状态
            checkCollectionStatus(word.getId());
        }
    }
    
    /**
     * 根据单词文本获取单词详情
     */
    public void getWordDetail(String wordText) {
        isLoading.setValue(true);
        
        wordRepository.getWordByWord(wordText, new DictionaryWordRepository.WordCallback() {
            @Override
            public void onSuccess(DictionaryWordEntity word) {
                selectedWord.postValue(word);
                if (word != null) {
                    loadExamples(word.getId());
                    checkCollectionStatus(word.getId());
                }
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 获取当前选中单词
     */
    public LiveData<DictionaryWordEntity> getSelectedWord() {
        return selectedWord;
    }
    
    /**
     * 加载例句
     */
    private void loadExamples(String wordId) {
        sentenceRepository.getExamplesByWordIdSync(wordId, 10, new ExampleSentenceRepositoryYSJ.SentencesCallback() {
            @Override
            public void onSuccess(List<ExampleSentenceEntity> sentences) {
                currentExamples.postValue(sentences);
            }
            
            @Override
            public void onError(String error) {
                currentExamples.postValue(new ArrayList<>());
            }
        });
    }
    
    /**
     * 获取例句LiveData
     */
    public LiveData<List<ExampleSentenceEntity>> getCurrentExamples() {
        return currentExamples;
    }
    
    /**
     * 检查收藏状态
     */
    private void checkCollectionStatus(String wordId) {
        collectionRepository.isWordCollected(wordId, currentUserId, new UserWordCollectionRepositoryYSJ.CheckCallback() {
            @Override
            public void onResult(boolean collected) {
                isCollected.postValue(collected);
            }
        });
    }
    
    /**
     * 获取收藏状态LiveData
     */
    public LiveData<Boolean> getIsCollected() {
        return isCollected;
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleCollection() {
        DictionaryWordEntity word = selectedWord.getValue();
        if (word == null) return;
        
        collectionRepository.toggleCollection(word.getId(), currentUserId, null, 
            new UserWordCollectionRepositoryYSJ.ToggleCallback() {
                @Override
                public void onToggled(boolean isNowCollected) {
                    isCollected.postValue(isNowCollected);
                }
                
                @Override
                public void onError(String error) {
                    errorMessage.postValue("收藏操作失败: " + error);
                }
            });
    }
    
    /**
     * 收藏单词并添加笔记
     */
    public void collectWordWithNote(String note) {
        DictionaryWordEntity word = selectedWord.getValue();
        if (word == null) return;
        
        collectionRepository.collectWord(word.getId(), currentUserId, note,
            new UserWordCollectionRepositoryYSJ.CollectCallback() {
                @Override
                public void onSuccess(long id) {
                    isCollected.postValue(true);
                }
                
                @Override
                public void onError(String error) {
                    errorMessage.postValue("收藏失败: " + error);
                }
            });
    }
    
    /**
     * 获取搜索历史
     */
    public LiveData<List<SearchHistoryEntity>> getSearchHistory() {
        return historyRepository.getRecentSearches(currentUserId, 10);
    }
    
    /**
     * 获取搜索建议
     */
    public void getSearchSuggestions(String keyword, SearchHistoryRepositoryYSJ.SuggestionsCallback callback) {
        historyRepository.getSearchSuggestions(currentUserId, keyword, 5, callback);
    }
    
    /**
     * 清除搜索历史
     */
    public void clearSearchHistory() {
        historyRepository.clearHistory(currentUserId, new SearchHistoryRepositoryYSJ.SimpleCallback() {
            @Override
            public void onComplete() {
                // 历史已清除
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("清除历史失败: " + error);
            }
        });
    }
    
    /**
     * 获取用户收藏列表
     */
    public LiveData<List<UserWordCollectionEntity>> getCollections() {
        return collectionRepository.getAllCollections(currentUserId);
    }
    
    /**
     * 获取加载状态
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * 获取错误信息
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 清除错误信息
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
