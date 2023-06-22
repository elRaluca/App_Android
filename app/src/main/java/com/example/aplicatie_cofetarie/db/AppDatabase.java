package com.example.aplicatie_cofetarie.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {GalleryImage.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GalleryImageDao galleryImageDao();

}
