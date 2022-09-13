package com.example.newdisc.historyResponse;

import com.example.newdisc.components.Item;
import com.example.newdisc.tools.SystemItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoryNode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String itemId;

    private Long size;

    private SystemItemType type;

    private String parentId;

    private ZonedDateTime date;

    private String url;

    public HistoryNode(Item item) {
        this.itemId = item.getId();
        this.type = item.getType();
        this.parentId = item.getParentId();
        this.date = item.getDate();
        this.size = item.getSize();
        this.url = item.getUrl();
    }

}
