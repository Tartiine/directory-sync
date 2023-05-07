package com.directorysync;

import java.nio.file.Path;

public class Directory {
    private String name;
    private Path path;
    private boolean local;
    private String ipAddress;

    public Directory (String name, Path path) {
        this.name = name;
        this.path = path;
        this.local = true;
    }

    public Directory (String name, Path path, String ipAddress) {
        this.name = name;
        this.path = path;
        this.local = false;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Path getPath() {
        return this.path;
    }
    public void setPath(Path path) {
        this.path = path;
    }

    public Boolean isLocal() {
        return this.local;
    }
    public void setLocal(Boolean b) {
        this.local = b;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
