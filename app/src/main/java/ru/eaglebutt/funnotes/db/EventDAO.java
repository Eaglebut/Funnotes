package ru.eaglebutt.funnotes.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.eaglebutt.funnotes.model.Event;

@Dao
public interface EventDAO {

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

    @Query("select * from events where startTime >= :startTime and startTime <= :endTime")
    List<Event> getEventsBetweenTime(long startTime, long endTime);

    @Insert
    void insert(Event event);

    @Insert
    void insert(List<Event> eventList);

    @Delete
    void delete(Event event);

    @Update
    void update(Event event);
}
