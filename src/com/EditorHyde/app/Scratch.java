package com.EditorHyde.app;

/**
 * Created by xrdawson on 8/19/13.
 */
public class Scratch {

    private long id;
    private String comment;
    private String scratch;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getScratch() {
        return scratch;
    }

    public void setScratch(String scratch) {
        this.scratch = scratch;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return comment;
    }
}

