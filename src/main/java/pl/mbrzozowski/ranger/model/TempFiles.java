package pl.mbrzozowski.ranger.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TempFiles {

    protected File file;

    public TempFiles(String fileName) {
        new File(System.getProperty("java.io.tmpdir") + "Rangerbot").mkdirs();
        String tempLogFilePath = System.getProperty("java.io.tmpdir") + "Rangerbot\\" + fileName;
        file = new File(tempLogFilePath);
    }

    public File getFile() {
        return file;
    }

    public void writeToFile(String message, boolean append) {
        if (file == null) {
            throw new NullPointerException("Log file is null");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.append(message).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToFile(String message) {
        writeToFile(message, true);
    }

    public void writeSeparatorToLogFile() {
        writeToFile("------------");
        writeToFile("");
    }

    public void clear() {
        file.delete();
    }
}
