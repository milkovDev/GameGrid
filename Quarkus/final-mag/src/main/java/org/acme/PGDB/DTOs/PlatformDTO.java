package org.acme.PGDB.DTOs;

import java.util.List;

public class PlatformDTO {
    private Long id;
    private String name;

    public PlatformDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
