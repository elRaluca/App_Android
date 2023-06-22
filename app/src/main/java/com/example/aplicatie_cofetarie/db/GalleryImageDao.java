package com.example.aplicatie_cofetarie.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GalleryImageDao {

    @Query("SELECT * FROM galleryimage")
    List<GalleryImage> getAll();

    @Insert
    void insertAll(GalleryImage... users);

    @Delete
    void delete(GalleryImage user);

}
