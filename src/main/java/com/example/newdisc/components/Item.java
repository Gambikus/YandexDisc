package com.example.newdisc.components;

import com.example.newdisc.tools.SystemItemType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    private String id;

    private Long size;

    private SystemItemType type;

    private String parentId;

    private ZonedDateTime date;

    private String url;

    public Item(SystemItemImport item, ZonedDateTime updateDate) {
        this.id = item.getId();
        this.type = item.getType();
        this.parentId = item.getParentId();
        this.date = updateDate;
        this.size = item.getSize();
        this.url = item.getUrl();
    }

}
