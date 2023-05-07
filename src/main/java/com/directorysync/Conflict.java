/** 
* Class name  : Conflict
* Description   : Represents a conflict between two or more files with the same relative file path.
* @author Isaac Dalberto, Sofia Saadi
*/
package com.directorysync;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

public class Conflict {
    private Path relativeFilePath;
    private List<Path> targetFiles;
    private List<Integer> concernedFiles;

    /**
     *@brief Constructs a Conflict object with a relative file path and target directories.
     *It sets the relative file path and sets the target files by resolving the relative
     *file path against each target directory. It also initializes the list of concerned files.
     *@param relativeFilePath the relative file path to create the Conflict for
     *@param targetDirectories the directories containing the target files to compare against
     */
    public Conflict(Path relativeFilePath, List<Path> targetDirectories) {
        setRelativeFilePath(relativeFilePath);
        setTargetFiles(targetFilesFromDirs(targetDirectories));
        concernedFiles = new LinkedList<Integer>();
    }

    /**
     * @brief Constructs a Conflict object with a relative file path.
     * It sets the relative file path to the given value and sets the target files to null.
     * It also initializes the list of concerned files.
     * @param relativeFilePath the relative file path to create the Conflict for
     */
    public Conflict(Path relativeFilePath) {
        setRelativeFilePath(relativeFilePath);
        setTargetFiles(null);
        concernedFiles = new LinkedList<Integer>();
    }

    /**
     * @brief Checks if this Conflict is equal to another Object.
     * Two Conflict objects are equal if they have the same relative file path.
     * @param o the Object to compare against
     * @return true if the given Object is a Conflict and has the same relative file path, false otherwise
    */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Conflict)) return false;
        Conflict c = (Conflict) o;
        return c.relativeFilePath.equals(this.relativeFilePath);
    }

    public void setRelativeFilePath(Path filePath) {
        relativeFilePath = filePath;
    }
    public Path getRelativeFilePath() {
        return relativeFilePath;
    }
    public void addConflictFile(Integer fileIndex) {
        concernedFiles.add(fileIndex);
    }
    public Integer getTargetFileIndex(int index) {
        return concernedFiles.get(index);
    }
    public Path getConcernedPath(int index) {
        return targetFiles.get(getTargetFileIndex(index));
    }
    public int getConcernedFilesLength() {
        return concernedFiles.size();
    }
    public void setTargetFiles(List<Path> targetPaths) {
        targetFiles = targetPaths;
    }

    /**
     * @brief Generates a list of target files by resolving relative file path against a list of target directories.
     * @param targetDirectories A list of directories in which to look for target files.
     * @return A list of absolute paths to target files.
     */
    public List<Path> targetFilesFromDirs(List<Path> targetDirectories) {
        List<Path> targets = new LinkedList<Path>();
        for (Path path : targetDirectories)
            targets.add(path.resolve(relativeFilePath));
        return targets;
    }

    /**
     * @brief Returns the latest modified file in the list of target files.
     * @return The latest modified file in the list of target files.
    */
    public Path getLatestFile() {
        Path latestFile = null;
        for (Integer i : concernedFiles) {
            if (latestFile == null || targetFiles.get(i).toFile().lastModified() > latestFile.toFile().lastModified())
                latestFile = targetFiles.get(i);
        }
        return latestFile;
    }

    /**
     * @brief Resolves conflicts by copying the specified file to each target file in the target files list.
     * @param fileToKeep The file to be copied to each target file.
     * @throws Exception if the operation fails.
    */
    public void resolve (Path fileToKeep) throws Exception {
        for (Path file : targetFiles) {
            if (Files.isDirectory(file)) {
                System.out.println("Copying directory: " + file.getFileName());
                try {
                    Files.createDirectory(file);
                } catch (FileAlreadyExistsException e) {
                    System.out.println("Directory " + file.getFileName() + " already exists.");
                }
            } else {
                System.out.println("Copying to file: " + file.getFileName());
                Files.copy(fileToKeep, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void resolve (Integer fileIndex) throws Exception {
        resolve(targetFiles.get(fileIndex));
    }
}
