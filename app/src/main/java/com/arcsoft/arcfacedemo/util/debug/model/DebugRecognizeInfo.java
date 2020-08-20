package com.arcsoft.arcfacedemo.util.debug.model;

import com.arcsoft.arcfacedemo.util.face.model.RecognizeInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class DebugRecognizeInfo extends RecognizeInfo {
    private long enterTime = System.currentTimeMillis();

    public long getEnterTime() {
        return enterTime;
    }


    private int trackId;
    private long qualityCost = -1;
    private long extractCost;
    private long livenessCost = -1;
    private long compareCost;
    private long totalCost;

    public long getQualityCost() {
        return qualityCost;
    }

    public void setQualityCost(long qualityCost) {
        this.qualityCost = qualityCost;
    }

    public long getExtractCost() {
        return extractCost;
    }

    public void setExtractCost(long extractCost) {
        this.extractCost = extractCost;
    }

    public long getLivenessCost() {
        return livenessCost;
    }

    public void setLivenessCost(long livenessCost) {
        this.livenessCost = livenessCost;
    }

    public long getCompareCost() {
        return compareCost;
    }

    public void setCompareCost(long compareCost) {
        this.compareCost = compareCost;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }


    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String performanceDaraToJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("trackId", trackId);
            jsonObject.put("qualityCost", qualityCost);
            jsonObject.put("extractCost", extractCost);
            jsonObject.put("livenessCost", livenessCost);
            jsonObject.put("compareCost", compareCost);
            jsonObject.put("totalCost", totalCost);
        } catch (JSONException e) {
            return "error";
        }
        return jsonObject.toString();
    }
}
