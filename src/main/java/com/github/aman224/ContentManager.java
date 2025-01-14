package com.github.aman224;

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
            Set.of(ARROW_UP, ARROW_DOWN, ARROW_RIGHT, ARROW_LEFT, HOME, END, PAGE_UP, PAGE_DOWN);

    private int cursorX = 0, offsetX = 0, cursorY = 0, offsetY = 0;
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
            scroll();
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
        if (secondKey != '[' && secondKey != 'O') {
            return secondKey;
        }

        int thirdKey = System.in.read();

        if (secondKey == '[') {
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
                        case '1', '7' -> {
                            yield HOME;
                        }
                        case '3' -> {
                            yield DEL;
                        }
                        case '4', '8' -> {
                            yield END;
                        }
                        case '5' -> {
                            yield PAGE_UP;
                        }
                        case '6' -> {
                            yield PAGE_DOWN;
                        }
                        default -> {
                            yield fourthKey;
                        }
                    }
                }
                default -> thirdKey;
            };
        } else {
            return switch (thirdKey) {
                case 'H' -> HOME;
                case 'F' -> END;
                default -> thirdKey;
            };
        }
    }

    private void handleKeyRead(int key) {
        if (positioningKeys.contains(key)) {
            moveCursor(key);
        }
    }

    private void refreshScreen() {
        StringBuilder builder = new StringBuilder();

        resetCursorAndClear(builder);
        renderContent(builder);
        renderStatusBar(builder);
        positionCursor(builder);

        System.out.print(builder);
    }

    private void scroll() {
        if (cursorY >= rows + offsetY) {
            offsetY = cursorY - rows + 1;
        } else if (cursorY < offsetY) {
            offsetY = cursorY;
        }

        if (cursorX >= columns + offsetX) {
            offsetX = cursorX - columns + 1;
        } else if (cursorX < offsetX) {
            offsetX = cursorX;
        }
    }

    private void resetCursorAndClear(StringBuilder builder) {
        builder.append("\033[2J");
        builder.append("\033[H");
    }

    private void renderContent(StringBuilder builder) {
        for (int i = 0; i < rows; i++) {
            int contentI = offsetY + i;

            if (contentI >= content.size()) {
                builder.append("~");
            } else {
                String line = content.get(contentI);
                int lengthToDraw = line.length() - offsetX;

                if (lengthToDraw < 0) { lengthToDraw = 0; }
                if (lengthToDraw > columns) { lengthToDraw = columns; }

                if (lengthToDraw > 0) {
                    builder.append(line, offsetX, offsetX + lengthToDraw);
                }
            }
            builder.append("\033[K\r\n");
        }
    }

    private void renderStatusBar(StringBuilder builder) {
        String message = "Text Editor v0.1 [Rows: " + rows + ", Columns: " + columns + ", X: " + cursorX + ", offsetX: " + offsetX + ", Y: " + cursorY + ", offsetY: " + offsetY + "]";
        builder.append("\033[7m")
                .append(message)
                .append(" ".repeat(Math.max(0, columns - message.length())))
                .append("\033[0m");
    }

    private void positionCursor(StringBuilder builder) {
        builder.append(String.format("\033[%d;%dH", cursorY - offsetY + 1, cursorX - offsetX + 1));
    }

    private void moveCursor(int key) {
        String currentLine = currentLIne();

        switch (key) {
            case ARROW_UP -> {
                if (cursorY > 0) {
                    cursorY--;
                }
            }
            case ARROW_DOWN -> {
                if (cursorY < content.size()) {
                    cursorY++;
                }
            }
            case ARROW_LEFT -> {
                if (cursorX > 0) {
                    cursorX--;
                }
            }
            case ARROW_RIGHT -> {
                if (currentLine != null && cursorX < currentLine.length()) {
                    cursorX++;
                }
            }
            case PAGE_UP, PAGE_DOWN -> {
                if (key == PAGE_UP) {
                    moveCursorToTop();
                } else {
                    moveCursorToBottom();
                }

                for (int i = 0; i < rows; i++) {
                    moveCursor(key == PAGE_UP ? ARROW_UP : ARROW_DOWN);
                }
            }
            case HOME -> cursorX = 0;
            case END -> {
                if (currentLine != null) {
                    cursorX = currentLine.length();
                }
            }
        }

        String newLine = currentLIne();
        if (newLine != null && cursorX > newLine.length()) {
            cursorX = newLine.length();
        }
    }

    private String currentLIne() {
        if (cursorY < content.size()) {
            return content.get(cursorY);
        } else {
            return null;
        }
    }

    private void moveCursorToTop() {
        cursorY = offsetY;
    }

    private void moveCursorToBottom() {
        cursorY = offsetY + rows - 1;

        if (cursorY > content.size()) {
            cursorY = content.size();
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