package ru.eaglebutt.funnotes.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "user")
public class User {

    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("surname")
    @Expose
    private String surname;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("id")
    @Expose
    @PrimaryKey(autoGenerate = true)
    private long id;
    @SerializedName("email")
    @Expose
    private String email;
    private boolean isSynchronized = false;


    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean aSynchronized) {
        isSynchronized = aSynchronized;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.isSynchronized = false;

    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
        this.isSynchronized = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.isSynchronized = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        this.isSynchronized = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.isSynchronized = false;
    }

    @Override
    public String toString() {
        return "\npassword " + password +
                "\nsurname='" + surname +
                "\nname='" + name +
                "\nid=" + id +
                "\nemail " + email +
                "\nisSynchronized " + isSynchronized;
    }
}