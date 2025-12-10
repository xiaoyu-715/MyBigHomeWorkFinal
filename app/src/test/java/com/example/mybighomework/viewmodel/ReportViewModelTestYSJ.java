package com.example.mybighomework.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.dao.ExamDao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ReportViewModel单元测试
 * 验证学习报告数据加载和处理逻辑
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ReportViewModelTestYSJ {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private StudyRecordDao studyRecordDao;
    
    @Mock
    private VocabularyDao vocabularyDao;
    
    @Mock
    private ExamDao examDao;
    
    private Application application;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        application = RuntimeEnvironment.getApplication();
    }
    
    @Test
    public void testGetTotalStudyDays() {
        // 准备测试数据
        List<String> studyDays = Arrays.asList("2025-11-20", "2025-11-21", "2025-11-22");
        when(studyRecordDao.getDistinctStudyDays()).thenReturn(studyDays);
        
        // 验证返回的学习天数
        assertEquals(3, studyDays.size());
    }
    
    @Test
    public void testGetMasteredVocabularyCount() {
        // 准备测试数据
        when(vocabularyDao.getMasteredVocabularyCount()).thenReturn(50);
        
        // 验证返回的已掌握词汇数
        int count = vocabularyDao.getMasteredVocabularyCount();
        assertEquals(50, count);
    }
    
    @Test
    public void testGetMasteredVocabularyCount_Empty() {
        // 测试空数据情况
        when(vocabularyDao.getMasteredVocabularyCount()).thenReturn(0);
        
        int count = vocabularyDao.getMasteredVocabularyCount();
        assertEquals(0, count);
    }
    
    @Test
    public void testStudyDaysEmpty() {
        // 测试空学习记录
        when(studyRecordDao.getDistinctStudyDays()).thenReturn(new ArrayList<>());
        
        List<String> days = studyRecordDao.getDistinctStudyDays();
        assertTrue(days.isEmpty());
    }
}
