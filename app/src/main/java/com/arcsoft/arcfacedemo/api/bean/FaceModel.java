package com.arcsoft.arcfacedemo.api.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FaceModel {
    @Expose
    @SerializedName("equipmentId")
    private String equipmentId;

    @Expose
    @SerializedName("faceFeature")
    private String faceFeature;

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public FaceModel(String equipmentId, String faceFeatures) {
        this.equipmentId = equipmentId;
        this.faceFeature = faceFeatures;
    }
}
