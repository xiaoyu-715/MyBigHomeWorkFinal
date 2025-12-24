package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书ViewModel
 * 管理词书列表状态和搜索逻辑
 */
public class BookViewModel extends AndroidViewModel {

    private BookRepository repository;
    private ExecutorService executor;

    private MutableLiveData<List<BookEntity>> books = new MutableLiveData<>();
    private MutableLiveData<List<BookEntity>> searchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BookViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        repository = new BookRepository(database);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<BookEntity>> getBooks() {
        return books;
    }

    public LiveData<List<BookEntity>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 加载指定父级下的词书
     */
    public void loadBooksByParentId(String parentId) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<BookEntity> result = repository.getBooksByParentIdSync(parentId);
                books.postValue(result);
            } catch (Exception e) {
                errorMessage.postValue("加载词书失败: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 加载顶级分类
     */
    public void loadTopLevelBooks() {
        loadBooksByParentId("0");
    }

    /**
     * 搜索词书
     */
    public void searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<BookEntity> result = repository.searchBooksSync(keyword.trim());
                searchResults.postValue(result);
            } catch (Exception e) {
                errorMessage.postValue("搜索失败: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 获取词书详情
     */
    public LiveData<BookEntity> getBookById(String bookId) {
        MutableLiveData<BookEntity> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                BookEntity book = repository.getBookByIdSync(bookId);
                result.postValue(book);
            } catch (Exception e) {
                errorMessage.postValue("获取词书详情失败: " + e.getMessage());
            }
        });
        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
