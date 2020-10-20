package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class LineCounterProcessorRunnable implements Runnable{
    private final String[] buffer;
    private final int n;
    private final CyclicBarrier cyclicBarrier;
    private final String s;
    private final LineCounterProcessor lineCounterProcessor = new LineCounterProcessor();

    public LineCounterProcessorRunnable(CyclicBarrier b, String s, String[] buffer, int n) {
        this.buffer = buffer;
        this.n = n;
        this.cyclicBarrier = b;
        this.s = s;
    }

    @Override
    public void run() {
        try {
            if(s == null){
                buffer[n] = null;
            }else {
                Pair<String, Integer> pair = lineCounterProcessor.process(s);
                buffer[n] = pair.getLeft() + " " + pair.getRight() + System.lineSeparator();
            }
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
