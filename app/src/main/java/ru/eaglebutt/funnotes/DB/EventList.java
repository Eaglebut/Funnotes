package ru.eaglebutt.funnotes.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface EventList {

    @Query("SELECT * from events")
    List<Event> getEvents();

    @Query("SELECT * from events where id = :id")
    Event findEventByID(int id);

    @Insert
    void insert(Event event);

    @Delete
    void delete(Event event);

    @Update
    void update(Event event);
}
