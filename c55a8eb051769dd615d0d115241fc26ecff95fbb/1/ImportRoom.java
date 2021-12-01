package com.java110.entity.assetImport;

/**
 * 房屋导入对象
 */
public class ImportRoom {

    private ImportFloor floor;

    private String roomNum;

    private int layer;

    private String section;

    private double builtUpArea;

    private String roomFeeId;

    private String feeEndDate;

    private ImportOwner importOwner;

    public ImportFloor getFloor() {
        return floor;
    }

    public void setFloor(ImportFloor floor) {
        this.floor = floor;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public double getBuiltUpArea() {
        return builtUpArea;
    }

    public void setBuiltUpArea(double builtUpArea) {
        this.builtUpArea = builtUpArea;
    }

    public ImportOwner getImportOwner() {
        return importOwner;
    }

    public void setImportOwner(ImportOwner importOwner) {
        this.importOwner = importOwner;
    }

    public String getRoomFeeId() {
        return roomFeeId;
    }

    public void setRoomFeeId(String roomFeeId) {
        this.roomFeeId = roomFeeId;
    }

    public String getFeeEndDate() {
        return feeEndDate;
    }

    public void setFeeEndDate(String feeEndDate) {
        this.feeEndDate = feeEndDate;
    }
}
