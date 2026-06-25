package com.example.new_toy_store.review.application.dto.response;

import java.util.Map;

public class ReviewSummaryResponse {
    private double averageRating;
    private int totalReviews;
    private Map<Integer, Integer> starCounts;

    public ReviewSummaryResponse(double averageRating, int totalReviews, Map<Integer, Integer> starCounts) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.starCounts = starCounts;
    }

    public double getAverageRating() { return averageRating; }
    public int getTotalReviews() { return totalReviews; }
    public Map<Integer, Integer> getStarCounts() { return starCounts; }
}