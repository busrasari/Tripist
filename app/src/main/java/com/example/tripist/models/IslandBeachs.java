package com.example.tripist.models;

import java.io.Serializable;

public class IslandBeachs extends Categories implements Serializable {
    public IslandBeachs(String name, Double latitude, Double longitude, String image) {
        super(name, latitude, longitude, image);
    }
}
