package com.example.mybighomework.utils;

import android.util.Log;

import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.entity.StudyPhaseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 阶段日期管理器
 * 负责计算和调整阶段的日期范围
 * 
 * Requirements: Task 4
 */
public class PhaseDateManager {
    
    private static final String TAG = "PhaseDateManager";
    private final StudyPhaseDao studyPhaseDao;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public PhaseDateManager(StudyPhaseDao studyPhaseDao) {
        this.studyPhaseDao = studyPhaseDao;
    }
    
    /**
     * 当阶段启动时，重新计算该阶段及其后续阶段的日期范围
     * 
     * @param planId 计划ID
     * @param currentPhase 当前启动的阶段
     * @param startDate 开始日期 (yyyy-MM-dd)
     */
    public void recalculatePhasesFrom(int planId, StudyPhaseEntity currentPhase, String startDate) {
        if (currentPhase == null) return;
        
        try {
            Log.d(TAG, "开始重新计算阶段日期: Plan=" + planId + ", Phase=" + currentPhase.getPhaseOrder() + ", Start=" + startDate);
            
            // 1. 更新当前阶段
            Date start = dateFormat.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            
            // 设置当前阶段开始日期
            currentPhase.setStartDate(startDate);
            
            // 计算当前阶段结束日期
            calendar.add(Calendar.DAY_OF_MONTH, Math.max(0, currentPhase.getDurationDays() - 1));
            String endDate = dateFormat.format(calendar.getTime());
            currentPhase.setEndDate(endDate);
            
            studyPhaseDao.update(currentPhase);
            Log.d(TAG, "更新当前阶段: " + currentPhase.getPhaseName() + " [" + startDate + " ~ " + endDate + "]");
            
            // 2. 更新后续阶段
            List<StudyPhaseEntity> allPhases = studyPhaseDao.getPhasesByPlanId(planId);
            Date lastEndDate = calendar.getTime();
            
            for (StudyPhaseEntity phase : allPhases) {
                if (phase.getPhaseOrder() > currentPhase.getPhaseOrder()) {
                    // 下一阶段开始日期 = 上一阶段结束日期 + 1天
                    calendar.setTime(lastEndDate);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String nextStart = dateFormat.format(calendar.getTime());
                    
                    phase.setStartDate(nextStart);
                    
                    // 计算结束日期
                    calendar.add(Calendar.DAY_OF_MONTH, Math.max(0, phase.getDurationDays() - 1));
                    String nextEnd = dateFormat.format(calendar.getTime());
                    phase.setEndDate(nextEnd);
                    
                    // 重置状态（如果之前被标记过）
                    if (StudyPhaseEntity.STATUS_COMPLETED.equals(phase.getStatus()) || 
                        StudyPhaseEntity.STATUS_IN_PROGRESS.equals(phase.getStatus())) {
                        // 保持原状态，或者是根据逻辑重置？通常后续阶段应该是未开始
                        if (StudyPhaseEntity.STATUS_COMPLETED.equals(phase.getStatus())) {
                            // 如果后续阶段已经是完成状态，这可能是不一致的数据，但在重新规划时，通常只处理未开始的
                            // 这里假设重新规划只影响时间，不回退状态，除非显式要求
                        } else {
                            phase.setStatus(StudyPhaseEntity.STATUS_NOT_STARTED);
                        }
                    } else {
                        phase.setStatus(StudyPhaseEntity.STATUS_NOT_STARTED);
                    }
                    
                    studyPhaseDao.update(phase);
                    Log.d(TAG, "更新后续阶段: " + phase.getPhaseName() + " [" + nextStart + " ~ " + nextEnd + "]");
                    
                    lastEndDate = calendar.getTime();
                }
            }
            
        } catch (ParseException e) {
            Log.e(TAG, "日期解析失败", e);
        }
    }
    
    /**
     * 动态调整阶段时长
     * @param phaseId 阶段ID
     * @param newDurationDays 新的持续天数
     */
    public void adjustPhaseDuration(int phaseId, int newDurationDays) {
        StudyPhaseEntity phase = studyPhaseDao.getPhaseById(phaseId);
        if (phase == null) return;
        
        phase.setDurationDays(newDurationDays);
        studyPhaseDao.update(phase);
        
        // 如果该阶段已有开始日期，需要重新计算结束日期和后续阶段
        if (phase.getStartDate() != null && !phase.getStartDate().isEmpty()) {
            recalculatePhasesFrom(phase.getPlanId(), phase, phase.getStartDate());
        }
    }
}
