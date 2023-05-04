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

    public Conflict(Path relativeFilePath, List<Path> targetDirectories) {
        setRelativeFilePath(relativeFilePath);
        setTargetFiles(targetFilesFromDirs(targetDirectories));
        concernedFiles = new LinkedList<Integer>();
    }
    public Conflict(Path relativeFilePath) {
        setRelativeFilePath(relativeFilePath);
        setTargetFiles(null);
        concernedFiles = new LinkedList<Integer>();
    }

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
    public int getConcernedFilesLength() {
        return concernedFiles.size();
    }
    public void setTargetFiles (List<Path> targetPaths) {
        targetFiles = targetPaths;
    }

    public List<Path> targetFilesFromDirs(List<Path> targetDirectories) {
        List<Path> targets = new LinkedList<Path>();
        for (Path path : targetDirectories)
            targets.add(path.resolve(relativeFilePath));
        return targets;
    }

    public Path getLatestFile() {
        Path latestFile = null;
        for (Integer i : concernedFiles) {
            if (latestFile == null || targetFiles.get(i).toFile().lastModified() > latestFile.toFile().lastModified())
                latestFile = targetFiles.get(i);
        }
        return latestFile;
    }

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
