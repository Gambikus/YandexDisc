package com.example.newdisc.controllers;

import com.example.newdisc.components.*;
import com.example.newdisc.historyResponse.HistoryNode;
import com.example.newdisc.historyResponse.SystemItemHistoryResponse;
import com.example.newdisc.historyResponse.SystemItemHistoryUnit;
import com.example.newdisc.repo.HistoryNodeRepository;
import com.example.newdisc.tools.Error;
import com.example.newdisc.repo.SystemItemRepository;
import com.example.newdisc.tools.SystemItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
public class DiscOpenController {
    @Autowired
    private SystemItemRepository systemItemRepository;

    @Autowired
    private HistoryNodeRepository historyNodeRepository;

    @PostMapping("/imports")
    public ResponseEntity<?> imports(@Validated @RequestBody SystemItemImportRequest request) {
        ZonedDateTime date = ZonedDateTime.parse(request.getUpdateDate());
        Set<String> folders = new HashSet<>(); // Множество id папок для их дальнейщего обновления
        Map<String, Boolean> isUsed = new HashMap<>();
        for (SystemItemImport item : request.getItems()) {
            /*
                Формируем множество папок, для заполнения\обновления и
                сохраняем в таблицу информацию о файлах и папках.
             */
            if (systemItemRepository.existsById(item.getId())) {
                Item newItem = systemItemRepository.findById(item.getId()).get();
                if (newItem.getParentId() != null) {
                    folders.add(newItem.getParentId());
                    /*
                        Меня дату обновления родителя, чтобы в случае изменения родителя
                        у айтема не забыть изменить дату родителя в методе updateFolder
                     */
                    Item parentItem = systemItemRepository.findById(newItem.getParentId()).get();
                    parentItem.setDate(date);
                    systemItemRepository.deleteById(parentItem.getId());
                    systemItemRepository.save(parentItem);
                    isUsed.put(newItem.getParentId(), false);
                }
                if (item.getParentId() != null) {
                    folders.add(item.getParentId());
                    isUsed.put(item.getParentId(), false);
                }
                systemItemRepository.deleteById(item.getId());
            } else if (item.getParentId() != null) {
                folders.add(item.getParentId());
                isUsed.put(item.getParentId(), false);
            }
            systemItemRepository.save(new Item(item, date));
        }
        for (String folderId: folders) {
            updateFolder(folderId, isUsed, date);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    /*
        Метод для заполнения и обновления папок, путем перебора содержимого
     */
    private void updateFolder(String folderId, Map<String, Boolean> isUsed, ZonedDateTime date) {
        isUsed.put(folderId, true);
        Item item = systemItemRepository.findById(folderId).get();
        List<Item> items = systemItemRepository.findAllByParentId(item.getId());
        Long size = 0L;
        for (Item value : items) {
            if (!(value.getType() == SystemItemType.FILE ||
                    (isUsed.get(value.getId()) != null && isUsed.get(value.getId())))) {
                updateFolder(value.getId(), isUsed, date);
            }
            Item child = systemItemRepository.findById(value.getId()).get();
            size += child.getSize();
            if (item.getDate().isBefore(child.getDate())) { // Обновление времени последнего обновления для папки
                item.setDate(child.getDate());
            }
        }
        item.setSize(size);
        systemItemRepository.deleteById(item.getId());
        systemItemRepository.save(item);

        /*
            Если дата последнего обновления совпадает с датой запроса,
            то добавляем новую запись
         */
        if (item.getDate().equals(date)) {
            historyNodeRepository.save(new HistoryNode(item));
        }
        /*
            Если у обрабатываемой папки есть родитель, и его id или нет в IsUsed,
            или есть с ложным значением, то обновляем родительскую папку
         */
        if (item.getParentId() != null &&
                (isUsed.get(item.getParentId()) == null || !isUsed.get(item.getParentId()))) {
            updateFolder(item.getParentId(), isUsed, date);
        }
    }

    @DeleteMapping  ("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String date) {
        ZonedDateTime updateDate = ZonedDateTime.parse(date);
        if (!systemItemRepository.existsById(id)) {
            return new ResponseEntity<>(new Error("Item not found", 404), HttpStatus.NOT_FOUND);
        }
        Item item = systemItemRepository.findById(id).get();
        deleteItem(item);
        /*
            Если есть родительская папка у файла или каталога, то обновляем дату
            последнего обновления и сохраняем запись об изменении.
         */
        if (item.getParentId() != null) {
            Item parentItem = systemItemRepository.findById(item.getParentId()).get();
            parentItem.setDate(updateDate);
            parentItem.setSize(parentItem.getSize() - item.getSize());
            systemItemRepository.deleteById(parentItem.getId());
            systemItemRepository.save(parentItem);
            historyNodeRepository.save(new HistoryNode(parentItem));
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    /*
        Метод для удаления папки и всех её детей
     */
    private void deleteItem(Item item) {
        if (item.getType() == SystemItemType.FOLDER) {
            List<Item> items = systemItemRepository.findAllByParentId(item.getId());
            for (Item el : items) {
                deleteItem(el);
            }
        }
        systemItemRepository.deleteById(item.getId());
        List<HistoryNode> nodes = historyNodeRepository.findAllByItemId(item.getId());
        for (HistoryNode node: nodes) {
            historyNodeRepository.deleteById(node.getId());
        }
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<?> getSystemItem (@PathVariable String id) {
        if (!systemItemRepository.existsById(id)) {
            return new ResponseEntity<>(new Error("Item not found", 404), HttpStatus.NOT_FOUND);
        }
        Item item = systemItemRepository.findById(id).get();
        SystemItem systemItem = new SystemItem(item);
        findChildren(systemItem);
        return new ResponseEntity<>(systemItem, HttpStatus.OK);
    }

    /*
        Метод для формирования массивов детей для папки и её детей
     */
    private void findChildren(SystemItem item) {
        if (item.getType() == SystemItemType.FOLDER) {
            List<Item> items = systemItemRepository.findAllByParentId(item.getId());
            SystemItem[] children = new SystemItem[items.size()];
            for (int i = 0; i < items.size(); i++) {
                children[i] = new SystemItem(items.get(i));
                findChildren(children[i]);
            }
            item.setChildren(children);
        }
    }

    @GetMapping("/updates")
    public ResponseEntity<?> updates(@RequestParam String date) {
        ZonedDateTime updateDate = ZonedDateTime.parse(date);
        List<Item> items = systemItemRepository.findAllByDateGreaterThanEqualAndDateLessThanEqual(
                updateDate.minusDays(1L),
                updateDate);
        SystemItemHistoryUnit[] units = new SystemItemHistoryUnit[items.size()];
        for (int i = 0; i < items.size(); i++) {
            units[i] = new SystemItemHistoryUnit(items.get(i));
        }
        return new ResponseEntity<>(new SystemItemHistoryResponse(units), HttpStatus.OK);
    }

    @GetMapping("/node/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable String id,
                                        @RequestParam(name = "dateStart", required = false) String start,
                                        @RequestParam(name = "dateEnd", required = false) String end) {
        ZonedDateTime startDate = ZonedDateTime.now().minusYears(100);
        ZonedDateTime endDate = ZonedDateTime.now().plusYears(100);
        if (start != null) {
            startDate = ZonedDateTime.parse(start);
        }
        if (end != null) {
            endDate = ZonedDateTime.parse(end);
        }
        if (!systemItemRepository.existsById(id)) {
            return new ResponseEntity<>(new Error("Item not found", 404), HttpStatus.NOT_FOUND);
        }

        List<HistoryNode> nodes = historyNodeRepository.findAllByItemIdAndDateGreaterThanEqualAndDateLessThan(id,
                startDate,
                endDate);
        SystemItemHistoryUnit[] units = new SystemItemHistoryUnit[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            units[i] = new SystemItemHistoryUnit(nodes.get(i));
        }
        return new ResponseEntity<>(new SystemItemHistoryResponse(units), HttpStatus.OK);
    }

    @ExceptionHandler({DateTimeParseException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<?> handleException(Exception e) {
        return new ResponseEntity<>(new Error("Validation Failed", 400), HttpStatus.BAD_REQUEST);
    }
}
