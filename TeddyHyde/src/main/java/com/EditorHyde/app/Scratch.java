package com.EditorHyde.app;

import java.util.Date;
import java.util.List;

import static com.roscopeco.ormdroid.Query.eql;
import com.roscopeco.ormdroid.Entity;

/**
 * Created by xrdawson on 8/19/13.
 */
public class Scratch extends Entity {

    public int id;
    public String contents;
    public Date createdAt;
    public Date updatedAt;

    public Scratch() {
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;

    }

    @Override
    public int save() {
        this.updatedAt = new Date();
        return super.save();
    }

    public static List<Scratch> scratches() {
        List<Scratch> results = query(Scratch.class).executeMulti();
        return results;
    }

    public String toString() {
        String rv = "";
        if( null != contents && "" != contents ) {
            String yamlStripped = MarkupUtilities.stripYFM( contents );
            String stripped = yamlStripped.replace( '\n', ' ');

            rv = updatedAt.toString() + ": ";
            if( stripped.length() < 20 ) {
                rv += stripped;
            }
            else {
                rv += stripped.substring(0, 20) + "...";
            }
        }
        else {
            rv = "Unknown contents";
        }
        return rv;
    }
}

