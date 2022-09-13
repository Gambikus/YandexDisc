package com.example.newdisc.components;

import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemItem extends DefaultItem {

    private String date;

    private SystemItem[] children;
    public SystemItem(Item item) {
        this.id = item.getId();
        this.date = item.getDate().format(DateTimeFormatter.ISO_INSTANT);
        this.url = item.getUrl();
        this.parentId = item.getParentId();
        this.type = item.getType();
        this.size = item.getSize();
        this.children = null;
    }
}
