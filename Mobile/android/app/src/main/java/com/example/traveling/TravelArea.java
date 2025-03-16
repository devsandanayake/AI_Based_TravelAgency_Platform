package com.example.traveling;

import java.util.List;

public class TravelArea {
    private List<String> list;

    public TravelArea() {
        // Default constructor required for calls to DataSnapshot.getValue(TravelArea.class)
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
