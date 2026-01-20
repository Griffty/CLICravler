package com.github.griffty.input;

import com.github.griffty.Main;
import com.github.griffty.util.Vector2;
import org.jline.utils.NonBlockingReader;

import java.util.concurrent.ConcurrentLinkedQueue;

public class InputController {
    private static final ConcurrentLinkedQueue<Integer> keyQueue = new ConcurrentLinkedQueue<>();
    private static InputThread inputThread;
    public static void startInputThread(NonBlockingReader reader){
        inputThread = new InputThread(keyQueue, reader);
    }

    public static int getInputNonBlocking() {
        if (inputThread == null) throw new IllegalStateException("InputController has not been initialized");
        Integer v = keyQueue.poll();
        return v == null ? -1 : v;
    }

    private static void clearInputBuffer() {
        keyQueue.clear();
    }

    public static void processInput() { //replace with general events
        int ch = getInputNonBlocking();
        if (ch != -1) {
            switch (ch) {
                case 'w' -> Main.getActiveLevel().move(new Vector2(0, -1));
                case 's' -> Main.getActiveLevel().move(new Vector2(0, 1));
                case 'a' -> Main.getActiveLevel().move(new Vector2(-1, 0));
                case 'd' -> Main.getActiveLevel().move(new Vector2(1, 0));
                case 'q' -> shutdown();
            }
        }
        clearInputBuffer();
    }

    public static void shutdown() {
        inputThread.shutDown();
        System.exit(0);
    }
}
