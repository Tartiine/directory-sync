/** 
* Class name  : 
* Description   : Directory synchronization project
* @author Isaac Dalberto, Sofia Saadi
* Date          : 
*/

package directorysync;

import java.io.IOException;
import java.nio.file.*;

public class DirectorySync {

    public static void main(String[] args) {
        watchEvents();
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
                        System.out.format("File '%s' created.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.format("File '%s' modified.%n", filename);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
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

}

