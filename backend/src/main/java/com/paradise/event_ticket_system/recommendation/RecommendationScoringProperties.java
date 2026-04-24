package com.paradise.event_ticket_system.recommendation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.recommendation.scoring")
public class RecommendationScoringProperties {

    private int defaultLimit = 6;
    private int categoryMatchPoints = 3;
    private int tagMatchPoints = 2;
    private int homeCityMatchPoints = 2;
    private int soonWindowBonusPoints = 1;
    private int soonWindowDays = 14;

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public int getCategoryMatchPoints() {
        return categoryMatchPoints;
    }

    public void setCategoryMatchPoints(int categoryMatchPoints) {
        this.categoryMatchPoints = categoryMatchPoints;
    }

    public int getTagMatchPoints() {
        return tagMatchPoints;
    }

    public void setTagMatchPoints(int tagMatchPoints) {
        this.tagMatchPoints = tagMatchPoints;
    }

    public int getHomeCityMatchPoints() {
        return homeCityMatchPoints;
    }

    public void setHomeCityMatchPoints(int homeCityMatchPoints) {
        this.homeCityMatchPoints = homeCityMatchPoints;
    }

    public int getSoonWindowBonusPoints() {
        return soonWindowBonusPoints;
    }

    public void setSoonWindowBonusPoints(int soonWindowBonusPoints) {
        this.soonWindowBonusPoints = soonWindowBonusPoints;
    }

    public int getSoonWindowDays() {
        return soonWindowDays;
    }

    public void setSoonWindowDays(int soonWindowDays) {
        this.soonWindowDays = soonWindowDays;
    }
}
