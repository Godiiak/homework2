package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriter implements Runnable {
    private static final Logger logger = getLogger(FileWriter.class);
    private final String resultFilename;
    private final Exchanger<String[]> exchanger;

    public FileWriter(String resultFilename, Exchanger<String[]> exchanger) {
        this.resultFilename = resultFilename;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(resultFilename, false))) {
            while(!Thread.currentThread().isInterrupted()) {
                String[] buffer = exchanger.exchange(new String[FileProcessor.CHUNK_SIZE]);

                for (String s : buffer
                ) {
                    if(s != null){
                        writer.write(s);
                    }else {
                        Thread.currentThread().interrupt();
                    }
                }
                writer.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Finish writer thread {}", currentThread().getName());
    }
}
