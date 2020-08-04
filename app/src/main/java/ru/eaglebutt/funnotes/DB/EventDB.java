package ru.eaglebutt.funnotes.DB;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Event.class}, version = 1)
public abstract class EventDB extends RoomDatabase {

    public abstract EventList eventList();
    private static final String DB_NAME = "events.db";
    private static volatile EventDB INSTANCE = null;

    public synchronized static EventDB get(Context context){
        if (INSTANCE == null){
            INSTANCE = create(context, false);
        }
        return(INSTANCE);
    }

    private static EventDB create(Context context, boolean memoryOnly){
        RoomDatabase.Builder<EventDB> builder;
        if (memoryOnly) {
            builder = Room.inMemoryDatabaseBuilder(context.getApplicationContext(),
                    EventDB.class);
        }
        else {
            builder = Room.databaseBuilder(context.getApplicationContext(), EventDB.class,
                    DB_NAME);
        }
        return(builder.build());
    }

}
