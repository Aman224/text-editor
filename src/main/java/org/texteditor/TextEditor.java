package org.texteditor;

public class TextEditor {

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        ContentManager contentManager = new ContentManager(args);

        terminal.enableRawMode();

        contentManager.configureWindow(terminal.getRows(), terminal.getColumns());

        try {
            contentManager.render();
        } catch (Exception ex) {
            System.err.println("Error: " + ex);
            ex.printStackTrace();
        }

        terminal.reset();
    }
}