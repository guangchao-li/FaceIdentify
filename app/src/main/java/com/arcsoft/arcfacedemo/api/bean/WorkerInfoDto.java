package com.arcsoft.arcfacedemo.api.bean;

public class WorkerInfoDto {
    /**
     * 工人姓名
     */
    private String workerName;

    /**
     * 工人年龄
     */
    private String age;

    /**
     * 入场时间
     */
    private String joinTime;

    /**
     * 工种
     */
    private String workerType;

    /**
     * 工龄
     */
    private String workYears;

    /**
     * 所在项目部
     */
    private String projectName;

    /**
     * 所在架子队
     */
    private String selfName;

    /**
     * 班组名称
     */
    private String teamName;

    /**
     * 有无重大病史
     */
    private String majorMedicalHistory;

    /**
     * 是否体检
     */
    private String  isPhysicalExamed;

    /**
     * 安全教育得分
     */
    private String safetyEducationScore;


    /**
     * 行为安全之星   是否
     *
     */
    private String behaviorSafetystars;


    /**
     * 不良记录次数
     */
    private String badRecordCount;

    /**
     * 良好记录次数
     */
    private String goodRecordCount;


    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(String joinTime) {
        this.joinTime = joinTime;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public String getWorkYears() {
        return workYears;
    }

    public void setWorkYears(String workYears) {
        this.workYears = workYears;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSelfName() {
        return selfName;
    }

    public void setSelfName(String selfName) {
        this.selfName = selfName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getMajorMedicalHistory() {
        return majorMedicalHistory;
    }

    public void setMajorMedicalHistory(String majorMedicalHistory) {
        this.majorMedicalHistory = majorMedicalHistory;
    }

    public String getIsPhysicalExamed() {
        return isPhysicalExamed;
    }

    public void setIsPhysicalExamed(String isPhysicalExamed) {
        this.isPhysicalExamed = isPhysicalExamed;
    }

    public String getSafetyEducationScore() {
        return safetyEducationScore;
    }

    public void setSafetyEducationScore(String safetyEducationScore) {
        this.safetyEducationScore = safetyEducationScore;
    }

    public String getBehaviorSafetystars() {
        return behaviorSafetystars;
    }

    public void setBehaviorSafetystars(String behaviorSafetystars) {
        this.behaviorSafetystars = behaviorSafetystars;
    }

    public String getBadRecordCount() {
        return badRecordCount;
    }

    public void setBadRecordCount(String badRecordCount) {
        this.badRecordCount = badRecordCount;
    }

    public String getGoodRecordCount() {
        return goodRecordCount;
    }

    public void setGoodRecordCount(String goodRecordCount) {
        this.goodRecordCount = goodRecordCount;
    }
}
