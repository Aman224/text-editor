package org.texteditor;

import java.io.IOException;

public class TextEditor {

    public static void main(String[] args) throws IOException {
        Terminal terminal = new Terminal();
        ContentManager contentManager = new ContentManager(args);

        terminal.enableRawMode();

        contentManager.configureWindow(terminal.getRows(), terminal.getColumns());
        contentManager.render();

        terminal.reset();
    }
}