/** 
* Class name  : DirectorySync
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date   : 
*/

package com.directorysync;

import com.directorysync.network.ClientSide;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class DirectorySync
 * @brief A class to synchronize directories
 * @details This class contains methods to compare directories and synchronize them by copying, deleting, or modifying files as required.
 * @note This class assumes that the directories are on the same system unless specified otherwise.
 */
public class DirectorySync {
    //public static Path targetDirectory = Paths.get("./trgDir");
    //public static Path srcDirectory = Paths.get("./srcDir");
    public static List<Path> syncDirectories = new ArrayList<>();;
    public static WatchEvent.Kind<?> kind;
    public static Path targetPath;
    static boolean isDirProcessing = false;
    public static boolean isLocalExchange = false; // set to false if it is network exchange
    public static WatchService watchService;
    public static Map<WatchKey, Path> keys;
    /**
     * @brief Deletes all files and directories within a target directory
     * @details This function recursively walks through all files and directories within the specified target directory, and deletes each one. 
     * @param targetDirectory The target directory to be emptied
     * @throws IOException If an I/O error occurs
     */

    public static void deleteAll(Path targetDirectory) throws IOException {
        Files.walk(targetDirectory)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    System.out.println("Deleting file: " + path);
                    Files.delete(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete " + path + ": " + e.getMessage());
                }
            });
            
    }
    /**
    * @brief Deletes a file
    * @details This function deletes a file at the specified path. If the file is on a remote system, it sends the deletion event to the remote system using the ClientSide class.
    * @param file The path of the file to be deleted
    */

    public static void deleteFile(Path file) {
        try {
            System.out.println("Deleting file: " + file.getFileName());
            if (isLocalExchange) {
                Files.delete(file);
            } else {
                ClientSide.events(kind.toString(), file);
            }

        } catch (IOException e) {
            System.err.println("Failed to delete " + file + ": " + e.getMessage());
        }
    }

    /**
     * @brief Creates a file or directory
     * @details This function creates a file or directory at the specified path. If the source path is a regular file, it creates a file. If it is a directory, it creates a directory.
     * @param file The path of the file or directory to be created
     * @param srcPath The source path of the file or directory to be created
     * @throws IOException If an I/O error occurs
     */
    public static void createFile(Path file, Path srcPath) throws IOException {
        try {
            System.out.println("Creating file: " + file.getFileName());
            if (Files.isRegularFile(srcPath)) {
                if (isLocalExchange) {
                    Files.createFile(file);
                } else {
                    ClientSide.events(kind.toString(),file);
                }
            } else if (Files.isDirectory(srcPath)) {
                if (isLocalExchange) {
                    Files.createDirectory(file);
                } else {
                    ClientSide.events(kind.toString()+"_DIR",file);
                }
            }
    
        } catch (IOException e) {
            //fenetre popup pour demander le remplacement avec comparaison des deux files
            //replaceFile(file, srcDirectory, targetDirectory, false);
        }
    }
    

    /**
     * @brief Copies all files and directories from a source directory to a target directory.
     * @details This function recursively copies all files and directories from a source directory to a target directory. If the target directory doesn't exist, it will create one.
     * @param targetDirectory The path of the target directory
     * @param srcDirectory The path of the source directory
     * @throws IOException If an I/O error occurs
     */
    public static void copyAll(Path targetDirectory, Path srcDirectory) throws IOException {
        Files.walk(srcDirectory)
            .forEach(path -> {
                try {
                    Path targetPath = targetDirectory.resolve(srcDirectory.relativize(path));
                    if (Files.isDirectory(path)) {
                        System.out.println("Copying directory: " + path.getFileName());
                        Files.createDirectory(targetPath);
                    } else {
                        System.out.println("Copying file: " + path.getFileName());
                        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to copy " + path.getFileName() + ": " + e.getMessage());
                }
            });
    }
    /**
    * @brief Synchronizes a target directory with a recovery directory
    * @details This method deletes all files and directories in the target directory, and then copies all files and directories from the recovery directory to the target directory.
    * @param targetPath The path of the target directory to be synchronized
    * @param recoveryPath The path of the recovery directory to be used for synchronization
    * @throws IOException If an I/O error occurs, or if either the target or recovery directory does not exist
    */
    public static void hardSynchronization(String targetPath, String recoveryPath) throws IOException {
        Path recoverySrc = Paths.get(recoveryPath);
        if (!Files.exists(recoverySrc)) {
            throw new IOException("Source directory (" + recoveryPath + ") does not exist.");
        }
        Path targetDirectory = Paths.get(targetPath);
        if (!Files.exists(targetDirectory)) {
            throw new IOException("Target directory (" + targetPath + ") does not exist.");
        }
        deleteAll(targetDirectory);
        copyAll(targetDirectory, recoverySrc);
    }

    /**
     * @brief Replaces a file or directory at the target path with the source file or directory
     * @details This function replaces a file or directory at the target path with the source file or directory. If the target file or directory doesn't exist, it will create one.
     * @param srcFile The path of the source file or directory
     * @param srcDirectory The path of the source directory
     * @param targetDirectory The path of the target directory
     * @param override If true, allows the function to override the existing target file or directory. If false, the function will not override the target file or directory and returns an error.
     * @throws IOException If an I/O error occurs
     */
    public static void replaceFile(Path srcFile, Path targetDirectory, boolean override) throws IOException {
        Path targetFile = constructTargetPath(srcFile, targetDirectory);
        if (Files.exists(targetFile)) {
            // If the file already exists, we check if it is the same and if we are allowed to override it.
            if (!override) {
                try {
                    if (!Files.isSameFile(srcFile, targetFile)) {
                        System.out.println("Not allowed to override!");
                        return;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to compare files: " + e.getMessage());
                    return;
                }
            }
    
            // Start the replacement by erasing the old file
            if (Files.isRegularFile(targetFile)) {
                System.out.println("Replacing file: " + targetFile.getFileName());
            } else if (Files.isDirectory(targetFile)) {
                deleteAll(targetFile);
                System.out.println("Replacing directory: " + targetFile.getFileName());
            }
            Files.delete(targetFile);
        }
        // If the file doesn't exist or if we override it, create a copy.
        if (Files.isRegularFile(srcFile)) {
            System.out.println("Copying file: " + srcFile.getFileName());
            Files.copy(srcFile, targetFile);
        } else if (Files.isDirectory(srcFile)) {
            System.out.println("Copying directory: " + srcFile.getFileName());
            Files.createDirectories(targetFile);
            Files.walkFileTree(srcFile, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path targetDir = targetFile.resolve(srcFile.relativize(dir));
                            try {
                                Files.copy(dir, targetDir);
                            } catch (FileAlreadyExistsException e) {
                                if (!Files.isDirectory(targetDir)) {
                                    throw e;
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
    
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path target = targetFile.resolve(srcFile.relativize(file));
                            Files.copy(file, target);
                            return FileVisitResult.CONTINUE;
                        }
                    });
        }
    }
    




    /**
     * @brief Registers the given directory, and all its sub-directories, with the WatchService
     * @details This function recursively walks through the given directory, and all its sub-directories, and registers them with the given WatchService for monitoring file system changes.
     * @param start The starting directory or list of directories to be monitored
     * @param ws The WatchService to register directories with
     * @param keys The map of registered WatchKeys and corresponding directories
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the start parameter is not a Path or List<Path>
     */
    public static void walkAndRegisterDirectories(Path start, WatchService watchService, Map<WatchKey, Path> keys) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void walkAndUnregisterDirectories(Path root, WatchService watchService, Map<WatchKey, Path> keys) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                WatchKey key = keys.keySet().stream().filter(k -> keys.get(k).equals(dir)).findFirst().orElse(null);
                if (key != null) {
                    key.cancel();
                    keys.remove(key);
                }
                return FileVisitResult.CONTINUE;
            }
    
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    

    private static Path constructTargetPath(Path srcPath, Path targetDir) {
        Path srcDir = null;
        for (Path dir : syncDirectories) {
            if (srcPath.startsWith(dir)) {
                srcDir = dir;
                break;
            }
        }
        if (srcDir == null) {
            throw new IllegalArgumentException("srcPath does not belong to any of the syncDirectories.");
        }
        Path relativePath = srcDir.relativize(srcPath);
        Path targetPath = targetDir.resolve(relativePath);
        return targetPath;
    }
    
    

    public static void watchEvents() {
        try {
            // Create a WatchService
            keys = new HashMap<>();
            watchService = FileSystems.getDefault().newWatchService();
            
            // Register directories for monitoring
            for (Path dir : syncDirectories) {
                walkAndRegisterDirectories(dir, watchService, keys);
            }

            // Handle events on files and directories within the registered directories
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    return;
                }
                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    kind = event.kind();
                    System.out.println("Event kind:" + kind + ". File affected: " + event.context() + ".");
                    Path filename = (Path) event.context();
                    if (filename.toString().contains("~") || filename.toString().endsWith(".tmp")){
                        continue; // Ignore the file if it's a temporary file
                    }
                    boolean isSrc = false;
    
                    Path srcPath = dir.resolve(filename);
                    //System.out.println("DIR: " + dir);
                    //System.out.println("srcPath: " + srcPath);

                    for (Path target : syncDirectories) {
                        //System.out.println("target: " + target);
                        Path targetPath = constructTargetPath(srcPath, target);
                        System.out.println("targetPath: " + targetPath);
                        WatchKey keyBlock = null;
                        for (Map.Entry<WatchKey, Path> entry : keys.entrySet()) {
                            if (entry.getValue().equals(targetPath)) {
                                keyBlock = entry.getKey();
                                break;
                            }
                        }

                        if (targetPath.equals(srcPath)) {
                            continue; // Skip the directory where the event was triggered
                        }

                        if (keyBlock != null) {
                            keyBlock.cancel();
                            keys.remove(keyBlock);
                        }

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            // Handle overflow events for later
                            continue;
                        }
                
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            createFile(targetPath, srcPath);
                            if (Files.isDirectory(srcPath)) {
                                if (!isSrc) {
                                    walkAndRegisterDirectories(srcPath, watchService, keys);
                                    isSrc = true; 
                                }
                                if(isLocalExchange){
                                    walkAndRegisterDirectories(targetPath, watchService, keys);
                                    System.out.format("Directory '%s' created.%n", filename);
                                }
 
                            } else if (Files.isRegularFile(srcPath)) {
                                System.out.format("File '%s' created.%n", filename);
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            if (!Files.isDirectory(srcPath)) {
                                while(true) {
                                    try {
                                        if (isLocalExchange) {
                                            replaceFile(srcPath, target, true);
                                        } else {
                                            ClientSide.events(kind.toString(), srcPath);
                                        }
                                        System.out.format("File '%s' modified.%n", filename);
                                        break;
                                    } catch (IOException e) {
                                        // File is locked, wait for a 1s and try again
                                        Thread.sleep(1000);
                                    }
                                }
                            }
                            
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            if (Files.isDirectory(targetPath)) {
                                if (!isSrc) {
                                    walkAndUnregisterDirectories(srcPath, watchService, keys);
                                    isSrc = true; // mark as unregistered
                                }
                                walkAndUnregisterDirectories(targetPath, watchService, keys);
                                deleteAll(targetPath);
                                System.out.format("Directory '%s' deleted.%n", filename);
                            } else {
                                if (isLocalExchange) {
                                    try {
                                        Files.delete(targetPath);
                                    } catch (NoSuchFileException e) {
                                        System.out.format("File '%s' doesn't exist in this directory.%n", filename);
                                    }
                                } else {
                                    ClientSide.events(kind.toString(), targetPath);
                                }
                                System.out.format("File '%s' deleted.%n", filename);
                            }
                        }
                        try {
                            WatchKey newKey = targetPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                            keys.put(newKey, targetPath);
                        } catch (NoSuchFileException e) {
                            System.err.println("Failed to register " + targetPath + " for watching: " + e.getMessage());
                        } catch (NotDirectoryException e) {
                        }
                    }
                    isSrc = false;
                }
    
                // Reset the WatchKey
                boolean valid = key.reset();
                if (!valid) {
                    // Handle invalid WatchKey
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        // All directories are inaccessible
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    

    /**
    @brief Adds a directory to the list of synchronized directories.
    @param dirName The name of the directory to be added.
    */
    public static void addSyncDir(String dirName){
        Path dir = Paths.get(dirName);
        syncDirectories.add(dir);

    }


    public static void main(String[] args) {
        addSyncDir("./trgDir");
        addSyncDir("./srcDir");
        watchEvents();
    }
}

