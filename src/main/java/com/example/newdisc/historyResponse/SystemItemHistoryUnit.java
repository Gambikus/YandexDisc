package com.example.newdisc.historyResponse;

import com.example.newdisc.components.DefaultItem;
import com.example.newdisc.components.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class SystemItemHistoryUnit extends DefaultItem {

    String date;

    public SystemItemHistoryUnit(Item item) {
        this.id = item.getId();
        this.date = item.getDate().format(DateTimeFormatter.ISO_INSTANT);
        this.url = item.getUrl();
        this.parentId = item.getParentId();
        this.type = item.getType();
        this.size = item.getSize();
    }

    public SystemItemHistoryUnit(HistoryNode item) {
        this.id = item.getItemId();
        this.date = item.getDate().format(DateTimeFormatter.ISO_INSTANT);
        this.url = item.getUrl();
        this.parentId = item.getParentId();
        this.type = item.getType();
        this.size = item.getSize();
    }
}
