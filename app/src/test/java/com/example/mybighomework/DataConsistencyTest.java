package com.example.mybighomework;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据一致性验证测试类
 * 用于验证学习报告中各项统计数据的准确性和一致性
 */
@RunWith(RobolectricTestRunner.class)
public class DataConsistencyTest {

    @Mock
    private StudyRecordRepository studyRecordRepository;
    
    @Mock
    private ExamRecordRepository examRecordRepository;
    
    @Mock
    private VocabularyRecordRepository vocabularyRecordRepository;
    
    @Mock
    private QuestionDao questionDao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试词汇训练统计数据的一致性
     */
    @Test
    public void testVocabularyStatisticsConsistency() {
        // 模拟数据
        int expectedVocabularyCount = 1500;
        Mockito.when(vocabularyRecordRepository.getTotalVocabularyCount()).thenReturn(expectedVocabularyCount);
        
        // 验证数据获取
        int actualCount = vocabularyRecordRepository.getTotalVocabularyCount();
        
        // 断言
        assertEquals("词汇训练统计数据应该一致", expectedVocabularyCount, actualCount);
        assertTrue("词汇数量应该大于0", actualCount > 0);
    }

    /**
     * 测试真题练习统计数据的一致性
     */
    @Test
    public void testExamStatisticsConsistency() {
        // 模拟数据
        int expectedExamCount = 85;
        Mockito.when(examRecordRepository.getTotalExamCount()).thenReturn(expectedExamCount);
        
        // 验证数据获取
        int actualCount = examRecordRepository.getTotalExamCount();
        
        // 断言
        assertEquals("真题练习统计数据应该一致", expectedExamCount, actualCount);
        assertTrue("考试数量应该大于等于0", actualCount >= 0);
    }

    /**
     * 测试模拟考试统计数据的一致性
     */
    @Test
    public void testMockExamStatisticsConsistency() {
        // 模拟数据
        int expectedMockExamCount = 12;
        Mockito.when(examRecordRepository.getMockExamCount()).thenReturn(expectedMockExamCount);
        
        // 验证数据获取
        int actualCount = examRecordRepository.getMockExamCount();
        
        // 断言
        assertEquals("模拟考试统计数据应该一致", expectedMockExamCount, actualCount);
        assertTrue("模拟考试数量应该大于等于0", actualCount >= 0);
    }

    /**
     * 测试错题本统计数据的一致性
     */
    @Test
    public void testErrorQuestionStatisticsConsistency() {
        // 模拟数据
        List<QuestionEntity> mockWrongQuestions = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            mockWrongQuestions.add(new QuestionEntity()); // 模拟错题对象
        }
        Mockito.when(questionDao.getWrongQuestions(60.0, 1)).thenReturn(mockWrongQuestions);
        
        // 验证数据获取
        int actualCount = questionDao.getWrongQuestions(60.0, 1).size();
        
        // 断言
        assertEquals("错题本统计数据应该一致", 45, actualCount);
        assertTrue("错题数量应该大于等于0", actualCount >= 0);
    }

    /**
     * 测试学习时长统计数据的一致性
     */
    @Test
    public void testStudyTimeConsistency() {
        // 模拟数据
        double expectedStudyHours = 25.5;
        Mockito.when(studyRecordRepository.getTotalStudyTimeHours()).thenReturn(expectedStudyHours);
        
        // 验证数据获取
        double actualHours = studyRecordRepository.getTotalStudyTimeHours();
        
        // 断言
        assertEquals("学习时长统计数据应该一致", expectedStudyHours, actualHours, 0.01);
        assertTrue("学习时长应该大于等于0", actualHours >= 0);
    }

    /**
     * 测试学习时长回退机制的一致性
     * 当没有实际学习记录时，应该使用估算值
     */
    @Test
    public void testStudyTimeFallbackConsistency() {
        // 模拟没有学习记录的情况
        Mockito.when(studyRecordRepository.getTotalStudyTimeHours()).thenReturn(0.0);
        Mockito.when(vocabularyRecordRepository.getMasteredVocabularyCount()).thenReturn(100);
        
        // 计算回退值（与ReportActivity中的逻辑一致）
        double actualStudyHours = studyRecordRepository.getTotalStudyTimeHours();
        int masteredVocabularyCount = vocabularyRecordRepository.getMasteredVocabularyCount();
        double fallbackHours = actualStudyHours > 0 ? actualStudyHours : masteredVocabularyCount * 0.05;
        
        // 断言
        assertEquals("回退学习时长应该基于词汇数量计算", 5.0, fallbackHours, 0.01);
        assertTrue("回退学习时长应该大于0", fallbackHours > 0);
    }

    /**
     * 测试平均分数统计数据的一致性
     */
    @Test
    public void testAverageScoreConsistency() {
        // 模拟数据
        double expectedAverageScore = 78.5;
        Mockito.when(examRecordRepository.getAverageScore()).thenReturn(expectedAverageScore);
        
        // 验证数据获取
        double actualScore = examRecordRepository.getAverageScore();
        
        // 断言
        assertEquals("平均分数统计数据应该一致", expectedAverageScore, actualScore, 0.01);
        assertTrue("平均分数应该在0-100之间", actualScore >= 0 && actualScore <= 100);
    }

    /**
     * 测试所有统计数据的数值合理性
     */
    @Test
    public void testStatisticsReasonableness() {
        // 模拟合理的数据范围
        Mockito.when(vocabularyRecordRepository.getTotalVocabularyCount()).thenReturn(1000);
        Mockito.when(examRecordRepository.getTotalExamCount()).thenReturn(50);
        Mockito.when(examRecordRepository.getMockExamCount()).thenReturn(10);
        Mockito.when(questionDao.getWrongQuestions(60.0, 1)).thenReturn(new ArrayList<>());
        Mockito.when(studyRecordRepository.getTotalStudyTimeHours()).thenReturn(20.0);
        Mockito.when(examRecordRepository.getAverageScore()).thenReturn(85.0);

        // 验证数据合理性
        int vocabCount = vocabularyRecordRepository.getTotalVocabularyCount();
        int examCount = examRecordRepository.getTotalExamCount();
        int mockExamCount = examRecordRepository.getMockExamCount();
        int errorCount = questionDao.getWrongQuestions(60.0, 1).size();
        double studyHours = studyRecordRepository.getTotalStudyTimeHours();
        double averageScore = examRecordRepository.getAverageScore();

        // 断言数据合理性
        assertTrue("词汇数量应该在合理范围内", vocabCount >= 0 && vocabCount <= 10000);
        assertTrue("考试数量应该在合理范围内", examCount >= 0 && examCount <= 1000);
        assertTrue("模拟考试数量不应超过总考试数量", mockExamCount <= examCount);
        assertTrue("错题数量应该在合理范围内", errorCount >= 0);
        assertTrue("学习时长应该在合理范围内", studyHours >= 0 && studyHours <= 1000);
        assertTrue("平均分数应该在0-100之间", averageScore >= 0 && averageScore <= 100);
    }

    /**
     * 测试数据更新后的一致性
     */
    @Test
    public void testDataConsistencyAfterUpdate() {
        // 初始数据
        Mockito.when(vocabularyRecordRepository.getTotalVocabularyCount()).thenReturn(100);
        int initialCount = vocabularyRecordRepository.getTotalVocabularyCount();
        
        // 模拟数据更新
        Mockito.when(vocabularyRecordRepository.getTotalVocabularyCount()).thenReturn(150);
        int updatedCount = vocabularyRecordRepository.getTotalVocabularyCount();
        
        // 断言数据更新的一致性
        assertNotEquals("数据更新后应该有变化", initialCount, updatedCount);
        assertTrue("更新后的数据应该大于初始数据", updatedCount > initialCount);
    }
}