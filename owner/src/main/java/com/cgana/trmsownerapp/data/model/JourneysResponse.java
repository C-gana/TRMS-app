package com.cgana.trmsownerapp.data.model;

import java.util.List;

public class JourneysResponse {
    private List<Journey> journeys;
    private int total;
    private int page;
    private int pages;

    // Getters and Setters
    public List<Journey> getJourneys() {
        return journeys;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}

