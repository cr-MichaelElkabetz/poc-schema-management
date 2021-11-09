package com.cybereason.schema.model;

public class Release {
    String tag;
    Long id;

    public Release(Object getTagName, Object getId) {
        this.tag = String.valueOf(getTagName);
        this.id = Long.valueOf(getId.toString());
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
