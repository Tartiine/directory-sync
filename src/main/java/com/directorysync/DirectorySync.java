/** 
* Class name  : DirectorySync
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date  : May 2023
*/

package com.directorysync;

import com.directorysync.network.ClientSide;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.directorysync.gui.ConfirmBox;
import com.directorysync.gui.Main;


/**
 * @class DirectorySync
 * @brief A class to synchronize directories
 * @details This class contains methods to compare directories and synchronize them by copying, deleting, or modifying files as required.
 * @note This class assumes that the directories are on the same system unless specified otherwise.
 */
public class DirectorySync {
    //public static Path targetDirectory = Paths.get("./trgDir");
    //public static Path srcDirectory = Paths.get("./srcDir");
    public static WatchEvent.Kind<?> kind;
    public static Path targetPath;
    static boolean isDirProcessing = false;
    public static boolean isLocalExchange = true; // set to false if it is network exchange
    public static WatchService watchService;
    public static Map<WatchKey, Path> keys;

    /**
     * @brief Deletes all files and directories within a target directory
     * @details This function recursively walks through all files and directories within the specified target directory, and deletes each one. 
     * @param targetDirectory The target directory to be emptied
     * @throws IOException If an I/O error occurs
     */
    public static List<Directory> directories;

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
    public static void hardSynchronization(List<Path> targetPaths, Path recoveryPath, Boolean warning) throws IOException {
        switch (getFolderValidity(recoveryPath.toString())) {
            case 0: throw new IOException("Please enter a source directory.");
            case 2: throw new IOException("Source directory (" + recoveryPath + ") does not exist.");
            case 3: throw new IOException("Unable to write or read in source directory (" + recoveryPath + ").");
        }
        for (Path path : targetPaths) {
            switch (getFolderValidity(path.toString())) {
                case 0: throw new IOException("Please enter a target directory.");
                case 2: throw new IOException("Target directory (" + path + ") does not exist.");
                case 3: throw new IOException("Unable to write or read in target directory (" + path + ").");
            }
            if (checkFolderInclusion(path, recoveryPath, true)) throw new IOException("Source and target directories should not contain each other.");
        }
        if (warning) {
            Boolean answer = ConfirmBox.display("Warning", "Warning: the content of all target directories will be erased. Continue?");
            if (!answer) {
                System.out.println("Synchronization cancelled");
                return;
            }
        }
        for (Path path : targetPaths) {
            System.out.println("deleteAll(" + path + ")");
            System.out.println("copyAll(" + path + ", " + recoveryPath + ")");
        }
    }

    /**
     * Tests a path that supposedly point towards a directory
     * @param path The path to check
     * @return 0 if{@code path}is empty
     * <li>1 if{@code path}is valid</li>
     * <li>2 if{@code path}is invalid or doesn't point to a directory</li>
     * <li>3 if{@code path}is valid but doesn't allow for reading or writing</li>
     */
    public static int getFolderValidity(String path) {
        if (path.trim().isEmpty())
            return 0;
        Path dirPath = Path.of(path);
        if (!Files.isDirectory(dirPath))
            return 2;
        else if (!Files.isReadable(dirPath) || !Files.isWritable(dirPath))
            return 3;
        else
            return 1;
    }

    /**
     * Returns true if the first folder contains the second folder and vice versa
    */
    public static boolean checkFolderInclusion(Path firstPath, Path secondPath, boolean twoWayCheck) {
        try {
            Path relativePath = firstPath.relativize(secondPath);
            System.out.println(relativePath);
            if (relativePath.toString().contains("..")) {
                if (twoWayCheck) return checkFolderInclusion(secondPath, firstPath, false);
                else return false;
            }
            else return true;
        } catch (Exception e1) {
            if (twoWayCheck) return checkFolderInclusion(secondPath, firstPath, false);
            else return false;
        }
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
     * Initialize the synchronization between folders by copying missing files.
     * In case of a conflict, the user is asked which file to keep.
     * Otherwise, and contrary to the hard synchronization, no files are deleted.
    */
    public static void softSynchronization (List<Path> directoryPaths) throws IOException {
        List<Conflict> conflicts = new LinkedList<Conflict>();
        try {
            for (Path dirPath : directoryPaths) {
                Files.walk(dirPath).forEach(path -> {
                    if (!Files.isRegularFile(path)) return;
                    Path relPath = dirPath.relativize(path);
                    
                    if (conflicts.contains(new Conflict(relPath))) return;
                    
                    Conflict conflict = new Conflict(relPath, directoryPaths);
                    for (int i = 0 ; i < directoryPaths.size() ; i++) {
                        if (Files.exists(directoryPaths.get(i).resolve(relPath)))
                            conflict.addConflictFile(i);
                    }
                    conflicts.add(conflict);
                    System.out.println("Test conflict:" + relPath.toString());
                });
            }

            for (Conflict conflict : conflicts) {
                if (conflict.getConcernedFilesLength() == 1) {
                    try { conflict.resolve(conflict.getLatestFile()); }
                    catch (Exception e) { System.err.println(e.getStackTrace()); }
                } else if (conflict.getConcernedFilesLength() > 1) {
                    // If 2 or more directories have a file with the same name, ask which one to keep
                    if (ConfirmBox.display("Conflict", "Do you want to keep the first file?")) {
                        try { conflict.resolve(conflict.getTargetFileIndex(0)); }
                        catch (Exception e) { System.err.println(e.getStackTrace()); }
                    } else {
                        try { conflict.resolve(conflict.getTargetFileIndex(1)); }
                        catch (Exception e) { System.err.println(e.getStackTrace()); }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
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
        for (Directory dir : Main.directoryList) {
            if (srcPath.startsWith((Path) dir)) {
                srcDir = (Path) dir;
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
            for (Directory dir : Main.directoryList) {
                walkAndRegisterDirectories((Path) dir, watchService, keys);
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

                    for (Directory target : Main.directoryList) {
                        //System.out.println("target: " + target);
                        Path targetPath = constructTargetPath(srcPath,(Path) target);
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
                                if(target.isLocal()){
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
                                        if (target.isLocal()) {
                                            replaceFile(srcPath,(Path) target, true);
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
                                if (target.isLocal()) {
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



    public static void main(String[] args) {
        watchEvents();
    }
}

