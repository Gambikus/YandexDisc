package com.example.newdisc.tools;

import com.example.newdisc.components.DefaultItem;
import com.example.newdisc.tools.SystemItemType;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class CustomSequenceProvider implements DefaultGroupSequenceProvider<DefaultItem> {
    @Override
    public List<Class<?>> getValidationGroups(DefaultItem item) {
        List<Class<?>> defaultGroupSequence = new ArrayList<>();

        defaultGroupSequence.add(DefaultItem.class);

        if (item != null && item.getType() == SystemItemType.FILE) {
            defaultGroupSequence.add(DefaultItem.WhenTypeIsFile.class);
        } else if (item != null) {
            defaultGroupSequence.add(DefaultItem.WhenTypeIsFolder.class);
        }

        return defaultGroupSequence;
    }
}
