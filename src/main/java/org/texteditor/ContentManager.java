package org.texteditor;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ContentManager {
    private final int ARROW_UP = 1000;
    private final int ARROW_DOWN = 1001;
    private final int ARROW_RIGHT = 1002;
    private final int ARROW_LEFT = 1003;
    private final int PAGE_UP = 1004;
    private final int PAGE_DOWN = 1005;
    private final int HOME = 1006;
    private final int END = 1007;
    private final int DEL = 1008;

    private final Set<Integer> positioningKeys =
            Set.of(ARROW_UP, ARROW_DOWN, ARROW_RIGHT, ARROW_LEFT, HOME, END);

    private int cursorX = 0, cursorY = 0;
    private int rows = 10, columns = 10;


    private String fileName;
    private List<String> content;

    public ContentManager(String[] args) {
        if (args.length == 1) {
            this.fileName = args[0];
            this.content = readFile();
        } else {
            this.content = new ArrayList<>();
        }
    }

    public void render() throws IOException {
        while (true) {
            refreshScreen();
            int key = readKey();

            if (exit(key)) {
                break;
            }
            handleKeyRead(key);
        }
    }

    public void configureWindow(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    private List<String> readFile() {
        Path path = Path.of(fileName);
        List<String> content = new ArrayList<>();

        if (Files.exists(path)) {
            try(Stream<String> stream = Files.lines(path)) {
                content = stream.toList();
            } catch (IOException ex) {
                System.err.print("Error reading file: " + ex.getMessage());
            }
        }

        return content;
    }

    private int readKey() throws IOException {
        int key = System.in.read();
        if (key != '\033') {
            return key;
        }

        int secondKey = System.in.read();
        if (secondKey != '[') {
            return secondKey;
        }

        int thirdKey = System.in.read();

        return switch (thirdKey) {
            case 'A' -> ARROW_UP;
            case 'B' -> ARROW_DOWN;
            case 'C' -> ARROW_RIGHT;
            case 'D' -> ARROW_LEFT;
            case 'H' -> HOME;
            case 'F' -> END;
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                int fourthKey = System.in.read();
                if (fourthKey != '~') {
                    yield fourthKey;
                }
                switch (thirdKey) {
                    case '1', '7' -> { yield HOME; }
                    case '3' -> { yield DEL; }
                    case '4', '8' -> { yield END; }
                    case '5' -> { yield PAGE_UP; }
                    case '6' -> { yield PAGE_DOWN; }
                    default -> { yield fourthKey; }
                }
            }
            default -> thirdKey;
        };
    }

    private void handleKeyRead(int key) {
        if (positioningKeys.contains(key)) {
            moveCursor(key);
        }
    }

    private void refreshScreen() {
        StringBuilder builder = new StringBuilder();

        builder.append("\033[2J");
        builder.append("\033[H");

        for (int i = 0; i < rows; i++) {
            if (i >= content.size()) {
                builder.append("~");
            } else {
                builder.append(content.get(i));
            }
            builder.append("\033[K\r\n");
        }

        String message = "Text Editor v0.1";
        builder.append("\033[7m")
                .append(message)
                .append(" ".repeat(Math.max(0, columns - message.length())))
                .append("\033[0m");

        builder.append(String.format("\033[%d;%dH", cursorY + 1, cursorX + 1));
        System.out.print(builder);
    }

    private void moveCursor(int key) {
        switch (key) {
            case ARROW_UP -> {
                if (cursorY > 0) {
                    cursorY--;
                }
            }
            case ARROW_DOWN -> {
                if (cursorY < rows - 1) {
                    cursorY++;
                }
            }
            case ARROW_LEFT -> {
                if (cursorX > 0) {
                    cursorX--;
                }
            }
            case ARROW_RIGHT -> {
                if (cursorX < columns - 1) {
                    cursorX++;
                }
            }
            case HOME -> cursorX = 0;
            case END -> cursorX = columns - 1;
        }
    }

    private boolean exit(int key) {
        if (key == 'q') {
            System.out.print("\033[2J");
            System.out.print("\033[H");

            return true;
        }
        return false;
    }
}