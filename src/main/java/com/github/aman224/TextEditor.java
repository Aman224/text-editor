package com.github.aman224;

import java.io.IOException;

public class TextEditor {
    private final Terminal terminal;
    private final ContentManager contentManager;


    public TextEditor() {
        this.terminal = new Terminal();
        this.contentManager = new ContentManager(terminal.getRows(), terminal.getColumns());
    }

    public void start() throws IOException {
        while (true) {
            contentManager.scroll();
            contentManager.render();
            int key = contentManager.readKey();
            if (key == -1) {
                break;
            }
            contentManager.handleInput(key);
        }
    }

    public void init(String[] args) {
        terminal.enableRawMode();

        if (args.length == 1) {
            contentManager.setFile(args[0]);
        }
    }

    public void reset() {
        contentManager.cleanup();
        terminal.reset();
    }
}