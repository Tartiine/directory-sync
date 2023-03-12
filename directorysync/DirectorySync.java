/** 
* Class name  : DirectorySync
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date          : 
*/

package directorysync;

import java.nio.file.*;
import java.util.Comparator;
import java.io.IOException;

public class DirectorySync {

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

    public static void deleteFile(Path filename) {
        try {
            System.out.println("Deleting file: " + filename.getFileName());
            Files.delete(filename);
        } catch (IOException e) {
            System.err.println("Failed to delete " + filename + ": " + e.getMessage());
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
    

    public static void watchEvents() {
        try {
            // Create a WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // Register a directory for monitoring
            
            srcDirectory.register(
                watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, 
                StandardWatchEventKinds.ENTRY_DELETE);

            // Process events in a loop (continuously check for changes in the watched directory)
            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    System.out.println(
                        "Event kind:" + event.kind() 
                          + ". File affected: " + event.context() + ".");
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        // Handle overflow events for later
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path filePath = targetDirectory.resolve(filename);

                    // Handle specific event types
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {   
                        try {
                            Files.createFile(filePath);
                        } catch (IOException e) {
                            //fenetre popup pour demander le remplacement avec comparaison des deux files
                            replaceFile(filePath, srcDirectory, targetDirectory, false);
                        }

                        System.out.format("File '%s' created.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.format("File '%s' modified.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        deleteFile(filePath);
                        System.out.format("File '%s' deleted.%n", filename);
                    }
                }

                // Reset the WatchKey
                boolean valid = key.reset();
                if (!valid) {
                    // Handle invalid WatchKey
                    break;
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