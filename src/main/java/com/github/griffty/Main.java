package com.github.griffty;

import com.github.griffty.util.DebugTools;
import com.github.griffty.util.Vector2;
import com.github.griffty.world.Level;

public class Main {
    private static final int TARGET_FRAMERATE = 50;
    private static Level level;

    public static void main(String[] args) throws InterruptedException {
        TerminalController tc = TerminalController.get();
        DebugTools.ensureWriter();

        long lastTime;

        level = generateLevel();

        while (true) {
            lastTime = System.currentTimeMillis();

            processInput(tc);
            
            tc.render(level.project());

            long frameTime = System.currentTimeMillis() - lastTime;
            long sleepTime = (1000 / TARGET_FRAMERATE) - frameTime;
            DebugTools.log("Frame time: " + frameTime + "ms, sleeping for: " + sleepTime + "ms");
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        }
    }

    private static Level generateLevel() {
        Vector2 size = new Vector2(60, 40);

        byte[][] tiles = new byte[size.getY()][size.getX()];
        for (int i = 0; i < size.getX(); i++) {
            for (int j = 0; j < size.getY(); j++) {
                if (i == 0 || i == size.getX()-1 || j == 0 || j == size.getY()-1){
                    tiles[j][i] = 1;
                    continue;
                }
                tiles[j][i] = 2;
            }
        }

        return new Level(size, tiles);
    }

    private static void processInput(TerminalController tc) {
        int ch = tc.getInputNonBlocking();
        if (ch != -1) {
            switch (ch) {
                case 'w' -> level.move(new Vector2(0, 1));
                case 's' -> level.move(new Vector2(0, -1));
                case 'a' -> level.move(new Vector2(1, 0));
                case 'd' -> level.move(new Vector2(-1, 0));
                case 'q' -> shutdown(tc);
            }
        }
    }

    private static void shutdown(TerminalController tc) {
        tc.shutdown();
        System.exit(0);
    }
}