package com.example.newdisc.repo;

import com.example.newdisc.historyResponse.HistoryNode;
import org.springframework.data.repository.CrudRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface HistoryNodeRepository extends CrudRepository<HistoryNode, Long> {
    List<HistoryNode> findAllByItemIdAndDateGreaterThanEqualAndDateLessThan(String id,
                                                                        ZonedDateTime start,
                                                                        ZonedDateTime end);

    List<HistoryNode> findAllByItemId(String id);
}
