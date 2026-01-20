package com.github.griffty;

import com.github.griffty.util.Vector2;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TerminalController {
    private static TerminalController instance;
    public static TerminalController get() {
        if (instance == null) {
            instance = new TerminalController();
        }
        return instance;
    }

    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final ConcurrentLinkedQueue<Integer> keyQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread inputThread;

    private TerminalController() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            terminal.enterRawMode();
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.flush();

            this.reader = terminal.reader();
            this.inputThread = new Thread(this::inputLoop, "input-thread");
            this.inputThread.setDaemon(true); // won’t prevent JVM exit
            this.inputThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void render(char[][] frame) {
        terminal.puts(InfoCmp.Capability.cursor_home);

        StringBuilder sb = new StringBuilder(frame.length * (frame[0].length + 1));
        for (char[] row : frame) {
            sb.append(row);
            sb.append('\n');
        }

        terminal.writer().print(sb);
        terminal.writer().flush();
    }

    private void inputLoop() {
        try {
            while (running.get()) {
                int ch = reader.read();
                if (ch == -1) continue;
                keyQueue.add(ch);
            }
        } catch (IOException ignored) {}
    }


    public Vector2 getTerminalSize() {
        int width = terminal.getWidth();
        int height = terminal.getHeight();
        return new Vector2(width, height);
    }

    public int getInputNonBlocking() {
        Integer v = keyQueue.poll();
        return v == null ? -1 : v;
    }

    public void shutdown() {
        running.set(false);
        try { reader.close(); } catch (IOException ignored) {}
    }
}
