package com.random.data.adapter.outbound.serialiazer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "items")
@XmlAccessorType(XmlAccessType.FIELD)
public class Wrapper<T> {

    @XmlElement(name = "item")
    private List<T> items;

    public Wrapper() {}

    public Wrapper(List<T> items) {
        this.items = Objects.requireNonNull(items, "items must not be null");
    }

    public List<T> getItems() {
        return items;
    }
}
