package com.github.griffty.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DebugTools {
    private static PrintWriter writer;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (DebugTools.class) {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            }
        }));
    }

    public static void ensureWriter() {
        if (writer != null) return;
        synchronized (DebugTools.class) {
            if (writer != null) return;
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)), true);
                System.out.println("Writer created");
            } catch (IOException e) {
                System.out.println("[DEBUG LOG] Failed to open log.txt: " + e.getMessage());
                throw new Error("Failed to open log.txt", e);
            }
        }
    }

    public static void log(String message) {
        writer.println(message);
        writer.flush();
    }
}