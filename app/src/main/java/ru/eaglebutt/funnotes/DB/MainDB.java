package ru.eaglebutt.funnotes.DB;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;

@Database(entities = {Event.class, User.class}, version = 12)
public abstract class MainDB extends RoomDatabase {

    public abstract DBService service();
    private static final String DB_NAME = "events.db";
    private static volatile MainDB INSTANCE = null;

    public synchronized static MainDB get(Context context){
        if (INSTANCE == null){
            INSTANCE = create(context, false);
        }
        return(INSTANCE);
    }

    private static MainDB create(Context context, boolean memoryOnly){
        RoomDatabase.Builder<MainDB> builder;
        if (memoryOnly) {
            builder = Room.inMemoryDatabaseBuilder(context.getApplicationContext(),
                    MainDB.class);
        }
        else {
            builder = Room.databaseBuilder(context.getApplicationContext(), MainDB.class,
                    DB_NAME)
            .fallbackToDestructiveMigration();
        }
        return(builder.build());
    }



}
