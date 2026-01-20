package com.github.griffty;

import com.github.griffty.util.Vector2;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;

public class TerminalController {
    private static TerminalController instance;
    public static TerminalController get() {
        if (instance == null) {
            instance = new TerminalController();
        }
        return instance;
    }

    private final Terminal terminal;

    private TerminalController() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            terminal.enterRawMode();
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void render(char[][] frame) {
        terminal.puts(InfoCmp.Capability.cursor_home);

        StringBuilder sb = new StringBuilder(frame.length * frame[0].length + frame.length * 8);
        for (int y = 0; y < frame.length; y++) {
            sb.append(frame[y]);
            if (y != frame.length - 1) {
                sb.append('\r');
                sb.append("\u001B[1B");
            }
        }

        terminal.writer().print(sb);
        terminal.writer().flush();
    }


    public Vector2 getTerminalSize() {
        int width = terminal.getWidth();
        int height = terminal.getHeight();
        return new Vector2(width, height);
    }

    public NonBlockingReader getReader() {
        return terminal.reader();
    }
}
