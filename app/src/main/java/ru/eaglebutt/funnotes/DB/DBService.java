package ru.eaglebutt.funnotes.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;

@Dao
public interface DBService {

    @Query("DELETE FROM user")
    void deleteUser();

    @Query("SELECT * from user")
    List<User> getUser();

    @Query("SELECT * from events")
    List<Event> getEvents();

    @Query("SELECT * from events where id = :id")
    Event findEventByID(int id);

    @Query("DELETE FROM events")
    void deleteAll();

    @Insert
    void insert(Event event);

    @Insert
    void insert(List<Event> eventList);

    @Insert
    void insert(User user);

    @Delete
    void delete(Event event);
    @Delete
    void delete(User user);

    @Update
    void update(Event event);

    @Update
    void update(User user);
}
