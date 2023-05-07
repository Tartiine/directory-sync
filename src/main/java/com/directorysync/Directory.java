/** 
* Class name  : Directory
* Description   : Represents a directory to be synchronized
* @author Isaac Dalberto, Sofia Saadi
*/
package com.directorysync;

import java.nio.file.Path;

public class Directory {
    private String name;
    private Path path;
    private boolean local;
    private String ipAddress;

    /**
     * @brief Constructor for a local directory
     * @param name Name of the directory
     * @param path Path to the directory
     */
    public Directory (String name, Path path) {
        this.name = name;
        this.path = path;
        this.local = true;
    }

    
    /**
     * @brief Constructor for a remote directory
     * @param name Name of the directory
     * @param path Path to the directory
     * @param ipAddress IP address of the remote directory
     */
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
