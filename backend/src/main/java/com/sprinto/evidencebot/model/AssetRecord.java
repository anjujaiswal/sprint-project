package com.sprinto.evidencebot.model;

public class AssetRecord {
    private String assetId;
    private String assetType;
    private String location;
    private String owner;
    private String status;

    public AssetRecord() {}

    public AssetRecord(String assetId, String assetType, String location, String owner, String status) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.location = location;
        this.owner = owner;
        this.status = status;
    }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}