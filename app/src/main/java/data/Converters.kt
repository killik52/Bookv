package com.example.myapplication.data

import androidx.room.TypeConverter
import com.example.myapplication.ArtigoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    // Converter para List<ArtigoItem>
    @TypeConverter
    fun fromArtigoItemList(value: List<ArtigoItem>?): String? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<List<ArtigoItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toArtigoItemList(value: String?): List<ArtigoItem>? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<List<ArtigoItem>>() {}.type
        return gson.fromJson(value, type)
    }

    // Converter para List<String>
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}