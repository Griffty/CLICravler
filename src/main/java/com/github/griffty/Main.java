package com.github.griffty;

import com.github.griffty.input.InputController;
import com.github.griffty.util.DebugTools;
import com.github.griffty.util.Vector2;
import com.github.griffty.world.Level;
import com.github.griffty.world.LevelGenerator;
import lombok.Getter;

import java.util.Arrays;

public class Main {
    private static final int TARGET_FRAMERATE = 50;
    private static boolean levelFinished = true;
    private static boolean active = false;
    @Getter
    private static Level activeLevel;
    private static Vector2 size = new Vector2(120, 60);

    static void main() throws InterruptedException {
        TerminalController tc = TerminalController.get();
        DebugTools.ensureWriter();
        InputController.startInputThread(tc.getReader());

        while (true) {
            if (!active) {
                welcome(tc);
                continue;
            }
            if (levelFinished) {
                activeLevel = LevelGenerator.generateLevel(size);
                levelFinished = false;
                size = size.scale(1.5f);
            }

            DebugTools.registerTime("Frame Start");
            InputController.processInput();
            
            tc.render(activeLevel.project());

            long frameTime = System.currentTimeMillis() - DebugTools.getTime("Frame Start");
            long sleepTime = (1000 / TARGET_FRAMERATE) - frameTime;
//            DebugTools.log("Frame time: " + frameTime + "ms, sleeping for: " + sleepTime + "ms");
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        }
    }

    private static boolean warn = false;
    private static void welcome(TerminalController tc) {
        var size = tc.getTerminalSize();
        int h = size.getY();
        int w = size.getX();

        if (w < 50 || h < 20) {
            if (!warn){
                warn = true;
                System.out.println("Terminal size too small! Please resize to at least 50x20.");
            }
            return;
        }


        String[] welcomeMessage = {
                "#############################################",
                "#                                           #",
                "#         WELCOME TO THE SCARY MAZE         #",
                "#                                           #",
                "#      Use W A S D to move your player      #",
                "#                                           #",
                "#    Reach the exit (⬢) to generate even    #",
                "#                 bigger maze!              #",
                "#                                           #",
                "#############################################",
                "",
                "Press any key to start..."
        };

        char[][] frame = new char[h][w];
        for (int y = 0; y < h; y++) Arrays.fill(frame[y], ' ');

        int startY = (h - welcomeMessage.length) / 2;
        for (int i = 0; i < welcomeMessage.length; i++) {
            String line = welcomeMessage[i];
            int startX = (w - line.length()) / 2;
            for (int j = 0; j < line.length() && startX + j < w; j++) {
                frame[startY + i][startX + j] = line.charAt(j);
            }
        }

        tc.render(frame);

        if (InputController.getInputNonBlocking() != -1) active = true;
    }

    public static void finishLevel() {
        levelFinished = true;
    }
}