/** 
* Class name  : DirectorySync
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date          : 
*/

package directorysync;

import java.io.IOException;
import java.nio.file.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;

public class DirectorySync {

    public static void deleteAll (File targetDirectory) {
        File[] oldFiles = targetDirectory.listFiles();

        for (File file : oldFiles) {
            if (file.isFile()) {
                System.out.println("Deleting file: " + file.getName());
            }
            else if (file.isDirectory()) {
                deleteAll(file);
                System.out.println("Deleting directory: " + file.getName());
            }
            file.delete();
        }
    }

    public static void copyAll (File targetDirectory, File srcDirectory) throws FileNotFoundException, IOException {
        File[] newFiles = srcDirectory.listFiles();
        
        for (File file : newFiles) {
            if (file.isFile()) {
                System.out.println("Copying file: " + file.getName());
                BufferedInputStream bInStream = new BufferedInputStream(new FileInputStream(file));
                byte[] data = new byte[(int) file.length()];
                bInStream.read(data);
                bInStream.close();
    
                File newFile = new File(targetDirectory, file.getName());
                newFile.createNewFile();
                BufferedOutputStream bOutStream = new BufferedOutputStream(new FileOutputStream(newFile));
                bOutStream.write(data);
                bOutStream.close();
            }
            else if (file.isDirectory()) {
                System.out.println("Copying directory: " + file.getName());
                File newDir = new File(targetDirectory, file.getName());
                newDir.mkdirs();
                copyAll(newDir, file);
            }
        }
    }

    public static void hardSynchronization (String targetPath, String recoveryPath) throws Exception {
        File recoverySrc = new File(recoveryPath);
        if (!recoverySrc.exists())
            throw new Exception("Source directory (" + recoveryPath + ") does not exist.");
        File targetDirectory = new File(targetPath);
        if (!targetDirectory.exists())
            throw new Exception("Target directory (" + targetPath + ") does not exist.");
        deleteAll(targetDirectory);
        copyAll(targetDirectory, recoverySrc);
    }

    
    public static void replaceFile (File srcFile, File srcDirectory, File targetDirectory, boolean override) throws FileNotFoundException, IOException {
        URI srcFilePath = srcFile.toURI();
        URI srcDirPath = srcDirectory.toURI();
        String relativePath = srcDirPath.relativize(srcFilePath).getPath();

        File targetFile = new File(targetDirectory.getPath() + "/" + relativePath);
        if (targetFile.exists()) {    
            //If the file already exists, we check if it is the same and if we are allowed to override it.
            if (!override) {
                if (!((srcFile.isFile() && targetFile.isFile()) || (srcFile.isDirectory() && targetFile.isDirectory()))) {
                    System.out.println("allowed to override!");
                    return;
                }
                if (srcFile.length() != targetFile.length()) {
                    System.out.println("Not allowed to override!");
                    return;
                }
                if (Files.mismatch(Path.of(srcFile.toURI()), Path.of(targetFile.toURI())) != -1) {
                    System.out.println("Not allowed to override!");
                    return;
                }
            }
            
            //Start the replacement by erasing the old file
            if (targetFile.isFile()) {
                System.out.println("Replacing file: " + targetFile.getName());
            }
            else if (targetFile.isDirectory()) {
                deleteAll(targetFile);
                System.out.println("Replacing directory: " + targetFile.getName());
            }
            targetFile.delete();
        }
        //If the file doesn't exist or if we ovveride it, create a copy.
        if (srcFile.isFile()) {
            System.out.println("Copying file: " + srcFile.getName());
            BufferedInputStream bInStream = new BufferedInputStream(new FileInputStream(srcFile));
            byte[] data = new byte[(int) srcFile.length()];
            bInStream.read(data);
            bInStream.close();

            targetFile.createNewFile();
            BufferedOutputStream bOutStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            bOutStream.write(data);
            bOutStream.close();
        }
        else if (srcFile.isDirectory()) {
            System.out.println("Copying directory: " + srcFile.getName());
            targetFile.mkdirs();
            copyAll(targetFile, srcFile);
        }
    }

    public static void watchEvents() {
        try {
            // Create a WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // Register a directory for monitoring
            Path directory = Paths.get("C:/Users");
            directory.register(
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

                    // Handle specific event types
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        //CODE POUR CREER UN FICHIER
                        System.out.format("File '%s' created.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        //CODE POUR MODIFIER UN FICHIER
                        System.out.format("File '%s' modified.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        //CODE POUR SUPPRIMER UN FICHIER
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