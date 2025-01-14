package com.github.aman224;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        TextEditor editor = new TextEditor();

        try {
            editor.init(args);
            editor.start();
        } catch (Exception ex) {
            logger.error("Error starting text editor: [{}]", ex.getMessage(), ex);
        }

        editor.reset();
    }
}
