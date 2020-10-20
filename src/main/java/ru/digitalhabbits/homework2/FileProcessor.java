package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.nio.charset.Charset.defaultCharset;
import static org.slf4j.LoggerFactory.getLogger;

public class FileProcessor {
    private static final Logger logger = getLogger(FileProcessor.class);
    public static final int CHUNK_SIZE = 2 * getRuntime().availableProcessors();
    private final String[] buffer = new String[CHUNK_SIZE];

    public void process(@Nonnull String processingFileName, @Nonnull String resultFileName) {
        checkFileExists(processingFileName);

        final File file = new File(processingFileName);

        ExecutorService fileWriter = Executors.newSingleThreadExecutor();
        Exchanger<String[]> exchanger = new Exchanger<>();
        fileWriter.execute(new FileWriter(resultFileName, exchanger));

        ExecutorService executorService = Executors.newFixedThreadPool(CHUNK_SIZE);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(CHUNK_SIZE, () -> {
            try {
                exchanger.exchange(buffer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        int n = 0;
        try (final Scanner scanner = new Scanner(file, defaultCharset())) {
            while (scanner.hasNext()) {
                executorService.execute(new LineCounterProcessorRunnable(cyclicBarrier, scanner.nextLine(), buffer, n));
                n++;
                if(n == CHUNK_SIZE){
                    n = 0;
                }
                if(!scanner.hasNext()){
                    for(int i = n; i < CHUNK_SIZE; i++){
                        executorService.execute(new LineCounterProcessorRunnable(cyclicBarrier, null, buffer, i));
                    }
                }
            }
        } catch (IOException exception) {
            logger.error("", exception);
        }

        executorService.shutdown();
        fileWriter.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.SECONDS);
            fileWriter.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("Finish main thread {}", Thread.currentThread().getName());
    }

    private void checkFileExists(@Nonnull String fileName) {
        final File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File '" + fileName + "' not exists");
        }
    }

}