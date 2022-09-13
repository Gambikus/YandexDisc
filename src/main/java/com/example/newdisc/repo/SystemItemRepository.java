package com.example.newdisc.repo;

import com.example.newdisc.components.Item;
import org.springframework.data.repository.CrudRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface SystemItemRepository extends CrudRepository<Item, String> {
    List<Item> findAllByParentId(String parentId);
    List<Item> findAllByDateGreaterThanEqualAndDateLessThanEqual(ZonedDateTime start, ZonedDateTime end);
}
