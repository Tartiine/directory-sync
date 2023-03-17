/** 
* Class name  : DirectorySync
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date          : 
*/

package directorysync;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class DirectorySync {
    //Demander si on veut une synchro automatique ou non au lancement de l'application
    public static Path targetDirectory = Paths.get("C:/Users/sosob/OneDrive/Bureau/trgDir");
    public static Path srcDirectory = Paths.get("C:/Users/sosob/OneDrive/Bureau/srcDir");


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
            Files.delete(file);
        } catch (IOException e) {
            System.err.println("Failed to delete " + file + ": " + e.getMessage());
        }
    }

    public static void createFile(Path file, Path srcPath) throws IOException {
        try {
            System.out.println("Creating file: " + file.getFileName());
            if (Files.isRegularFile(srcPath)) {
                Files.createFile(file); 
            } else if (Files.isDirectory(srcPath)) {
                Files.createDirectory(file); 
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
                    WatchEvent.Kind<?> kind = event.kind();
                    System.out.println(
                        "Event kind:" + event.kind() 
                          + ". File affected: " + event.context() + ".");
                    Path filename = (Path) event.context();
                    System.out.format("File : '%s'%n", filename);
                    Path srcPath = dir.resolve(filename);
                    System.out.format("srcPath : '%s'%n", srcPath);
                    Path targetPath = targetDirectory.resolve(srcDirectory.relativize(srcPath));
                    System.out.format("targetPath : '%s'%n", targetPath);
            
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
                        if (Files.isRegularFile(srcPath)) {
                            replaceFile(srcPath,srcDirectory,targetDirectory, true);
                            System.out.format("File '%s' modified.%n", filename);
                        }
                        
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        if (Files.isDirectory(targetPath)) {
                            deleteAll(targetPath);
                            System.out.format("Directory '%s' deleted.%n", filename);
                        } else if (Files.isRegularFile(targetPath)) {
                            Files.delete(targetPath);
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

        } catch (IOException e) {
            // Handle IO exceptions
            e.printStackTrace();
        } catch (InterruptedException e) {
            // Handle interrupted exceptions
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        watchEvents();
    }
}

