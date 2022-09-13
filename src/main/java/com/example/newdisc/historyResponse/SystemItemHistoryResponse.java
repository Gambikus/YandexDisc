package com.example.newdisc.historyResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemItemHistoryResponse {
    private SystemItemHistoryUnit[] items;
}
