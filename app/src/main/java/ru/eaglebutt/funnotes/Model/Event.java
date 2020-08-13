package ru.eaglebutt.funnotes.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "events")
public class Event {

    @SerializedName("start_time")
    @Expose
    private long startTime;
    @SerializedName("end_time")
    @Expose
    private long endTime;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("id")
    @Expose
    @PrimaryKey(autoGenerate = true)
    private int id;
    @SerializedName("title")
    @Expose
    private String title;
    private boolean isSynchronized = false;
    private long lastUpdateTime;

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean aSynchronized) {
        isSynchronized = aSynchronized;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        this.isSynchronized = false;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.isSynchronized = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.isSynchronized = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.isSynchronized = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.isSynchronized = false;
    }

    @Override
    public String toString() {
        return "\nid " + id +
                "\ntitle " + title +
                "\ndescription " + description +
                "\nstart time " + startTime +
                "\nend time " + endTime +
                "\nisSynchronized " + isSynchronized;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void update(){
        lastUpdateTime = System.currentTimeMillis();
    }
}