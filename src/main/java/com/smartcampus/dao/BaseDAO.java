/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.dao;

import com.smartcampus.models.BaseModel;
import java.util.List;

/**
 *
 * @author Nuwanka Fernando
 */
public class BaseDAO<T extends BaseModel> {

    private final List<T> items;

    public BaseDAO(List<T> items) {
        this.items = items; // Inject the specific list from MockDatabase
    }

    public List<T> getAll() {
        return items;
    }

    public T getById(String id) {
        for (T item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void add(T item) {
        items.add(item);
    }

    public void update(T updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            if (item.getId().equals(updatedItem.getId())) {
                items.set(i, updatedItem);
                return;
            }
        }
    }

    public void delete(T item) {
        items.remove(items.get(items.indexOf(item)));
    }

}
