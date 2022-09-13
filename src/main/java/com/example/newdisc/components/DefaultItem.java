package com.example.newdisc.components;

import com.example.newdisc.tools.CustomSequenceProvider;
import com.example.newdisc.tools.SystemItemType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.group.GroupSequenceProvider;

import javax.validation.constraints.*;


@Data
@NoArgsConstructor
@GroupSequenceProvider(value = CustomSequenceProvider.class)
public abstract class DefaultItem {
    @NotNull
    protected String id;

    protected String parentId;
    @NotNull
    protected SystemItemType type;

    @Size(max=255, groups=WhenTypeIsFile.class)
    @Null(groups=WhenTypeIsFolder.class)
    protected String url;

    @Positive(groups=WhenTypeIsFile.class)
    @Null(groups=WhenTypeIsFolder.class)
    protected Long size;

    public interface WhenTypeIsFolder {

    }
    public interface WhenTypeIsFile {

    }
}
