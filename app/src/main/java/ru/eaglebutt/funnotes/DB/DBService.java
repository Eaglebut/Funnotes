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

    @Query("SELECT * from events where localId = :id")
    Event findEventByLocalId(int id);

    @Query("DELETE FROM events")
    void deleteAllEvents();

    @Query("select * from events where lastUpdateTime = :lastUpdated")
    Event findEventByLastUpdated(long lastUpdated);

    @Query("select * from events where serverId = :serverId")
    Event findEventByServerId(int serverId);

    @Query("select * from events where not status = 0")
    List<Event> getNotUpdatedEvents();

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
