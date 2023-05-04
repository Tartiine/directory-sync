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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.directorysync.gui.ConfirmBox;

import java.io.IOException;

public class DirectorySync {
    //Demander si on veut une synchro automatique ou non au lancement de l'application
    public static Path targetDirectory = Paths.get("./trgDir");
    public static Path srcDirectory = Paths.get("./srcDir");
    public static WatchEvent.Kind<?> kind;
    public static Path targetPath;
    public static boolean isLocalExchange = false; // set to false if it is network exchange
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

    public static void deleteFile(Path file) {
        try {
            System.out.println("Deleting file: " + file.getFileName());
            if (isLocalExchange) {
                Files.delete(file);
            } else {
                ClientSide.events(kind, file);
            }

        } catch (IOException e) {
            System.err.println("Failed to delete " + file + ": " + e.getMessage());
        }
    }


    public static void createFile(Path file, Path srcPath) throws IOException {
        try {
            System.out.println("Creating file: " + file.getFileName());
            if (Files.isRegularFile(srcPath)) {
                if (isLocalExchange) {
                    Files.createFile(file);
                } else {
                    ClientSide.events(kind,file);
                }
            } else if (Files.isDirectory(srcPath)) {
                if (isLocalExchange) {
                    Files.createDirectory(file);
                } else {
                    ClientSide.events(kind,file);
                }
            }
        } catch (IOException e) {
            //fenetre popup pour demander le remplacement avec comparaison des deux files
            replaceFile(file, srcDirectory, targetDirectory, false);
        }
    }
    


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

    public static void replaceFile(Path srcFile, Path srcDirectory, Path targetDirectory, boolean override) throws IOException {
        Path relativePath = srcDirectory.relativize(srcFile);
        Path targetFile = targetDirectory.resolve(relativePath);
    
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
            copyAll(targetFile, srcFile);
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
    * Register the given directory, and all its sub-directories, with the WatchService.
    */
  private static void walkAndRegisterDirectories(final Path start, WatchService ws, Map<WatchKey, Path> keys) throws IOException {
    // register directory and sub-directories
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        WatchKey key = dir.register(ws, 
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY, 
        StandardWatchEventKinds.ENTRY_DELETE);
        keys.put(key, dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }


    public static void watchEvents() {
        try {

            // Create a WatchService
            Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
            WatchService watchService = FileSystems.getDefault().newWatchService();
            

            System.out.println("Watching directory: " + srcDirectory);


            // Register a directory for monitoring
            walkAndRegisterDirectories(srcDirectory, watchService, keys);


            // Handle events on files and directories within the source directory
            WatchKey key;
            while ((key = watchService.take()) != null) {
                Path dir = keys.get(key);
                if (dir == null) {
                  System.err.println("WatchKey not recognized!!");
                  continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    kind = event.kind();
                    System.out.println(
                        "Event kind:" + kind 
                          + ". File affected: " + event.context() + ".");
                    Path filename = (Path) event.context();

                    if (filename.toString().contains("~") || filename.toString().endsWith(".tmp")){
                        continue; // Ignore the file
                    }
                    //System.out.format("File : '%s'%n", filename);
                    Path srcPath = dir.resolve(filename);
                    //System.out.format("srcPath : '%s'%n", srcPath);
                    Path targetPath = targetDirectory.resolve(filename);
                    //System.out.format("targetPath : '%s'%n", targetPath);
                    

                    // Process events in a loop (continuously check for changes in the watched directory)
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        // Handle overflow events for later
                        continue;
                    }

                    // Handle specific event types
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {   
                        createFile(targetPath, srcPath);
                        if (Files.isDirectory(srcPath)) {
                            walkAndRegisterDirectories(srcPath, watchService, keys);
                            System.out.format("Directory '%s' created.%n", filename);
                        } else if (Files.isRegularFile(srcPath)) {
                            System.out.format("File '%s' created.%n", filename);
                        } 

                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        // Amelioration : Ne pas remplacer le fichier mais juste ajouter les modifications ?
                        while(true) {
                            try {
                                if (isLocalExchange) {
                                    replaceFile(srcPath,srcDirectory,targetDirectory, true);
                                } else {
                                    ClientSide.events(kind, targetPath);
                                }
                                System.out.format("File '%s' modified.%n", filename);
                                break;
                            } catch (IOException e) {
                                // File is locked, wait for a 1s and try again
                                Thread.sleep(1000);
                            }
                        }
                        
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        if (Files.isDirectory(targetPath)) {
                            deleteAll(targetPath);
                            System.out.format("Directory '%s' deleted.%n", filename);
                        } else {
                            if (isLocalExchange) {
                                Files.delete(targetPath);
                            } else {
                                ClientSide.events(kind, targetPath);
                            }
                            System.out.format("File '%s' deleted.%n", filename);
                        }
                    }
                }

                // Reset the WatchKey
                boolean valid = key.reset();
                if (!valid) {
                    // Handle invalid WatchKey
                    keys.remove(key);
                    // all directories are inaccessible
                    if (keys.isEmpty()) {
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

