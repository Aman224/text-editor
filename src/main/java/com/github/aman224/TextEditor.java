package com.github.aman224;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextEditor {

    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        ContentManager contentManager = new ContentManager(args);

        logger.info("Terminal in Raw Mode");

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