package com.example.aplicatie_cofetarie.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GalleryImage {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "path")
    public String path;

}
