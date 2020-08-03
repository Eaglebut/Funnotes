package ru.eaglebutt.funnotes.DB;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Timestamp start_time;
    private Timestamp end_time;
    @NonNull
    private String title;
    private String description;
    private Timestamp last_update;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getLast_update() {
        return last_update;
    }

    public void setLast_update(){
        last_update = new Timestamp(System.currentTimeMillis());
    }


    @Ignore
    public Event(int id, Timestamp start_time, Timestamp end_time, @NonNull String title, String description) {
        this.id = id;
        this.start_time = start_time;
        this.end_time = end_time;
        this.title = title;
        this.description = description;
        this.last_update = null;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", last_update=" + last_update +
                '}';
    }
}
