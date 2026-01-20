package com.github.griffty.input;

import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InputThread extends Thread{
    private final ConcurrentLinkedQueue<Integer> keyQueue;
    private final NonBlockingReader reader;

    private boolean running;
    protected InputThread(ConcurrentLinkedQueue<Integer> keyQueue, NonBlockingReader reader) {
        super("input-thread");
        this.reader = reader;
        this.keyQueue = keyQueue;
        this.setDaemon(true);
        running = true;
        start();
    }

    @Override
    public void run() {
        try {
            while (running) {
                int ch = reader.read();
                if (ch == -1) continue;
                keyQueue.add(ch);
            }
            reader.close();
        } catch (IOException ignored) {}
    }

    public void shutDown() {
        running = false;
    }
}
