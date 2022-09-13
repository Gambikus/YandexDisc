package com.example.newdisc.components;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@Validated
public class SystemItemImportRequest {
    @NotNull
    private @Valid SystemItemImport[] items;

    @NotNull
    private String updateDate;

}
