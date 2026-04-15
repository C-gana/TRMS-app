package com.cgana.trmsownerapp.data.local.converters;

import androidx.room.TypeConverter;

import com.cgana.trmsownerapp.data.model.Location;
import com.cgana.trmsownerapp.data.model.Seat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {

    private static final Gson gson = new Gson();

    // List<Seat> converters
    @TypeConverter
    public static String fromSeatList(List<Seat> seats) {
        return gson.toJson(seats);
    }

    @TypeConverter
    public static List<Seat> toSeatList(String seatsJson) {
        Type listType = new TypeToken<List<Seat>>() {}.getType();
        return gson.fromJson(seatsJson, listType);
    }

    // List<String> converters
    @TypeConverter
    public static String fromStringList(List<String> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String json) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    // Location converters
    @TypeConverter
    public static String fromLocation(Location location) {
        return gson.toJson(location);
    }

    @TypeConverter
    public static Location toLocation(String locationJson) {
        return gson.fromJson(locationJson, Location.class);
    }
}

