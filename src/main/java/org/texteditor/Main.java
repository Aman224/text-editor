package org.texteditor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.io.IOException;
import java.util.Arrays;


public class Main {
    private static LibC.Termios defaultAttributes;

    private static final int ARROW_UP = 1000;
    private static final int ARROW_DOWN = 1001;
    private static final int ARROW_RIGHT = 1002;
    private static final int ARROW_LEFT = 1003;
    private static final int PAGE_UP = 1004;
    private static final int PAGE_DOWN = 1005;
    private static final int HOME = 1006;
    private static final int END = 1007;
    private static final int DEL = 1008;

    private static int rows = 10;
    private static int columns = 10;

    public static void main(String[] args) throws IOException {
        enableRawMode();
        init();

        while (true) {
            refreshScreen();
            int key = readKey();
            handleKeyRead(key);
        }
    }

    private static void init() {
        LibC.WinSize winSize = getWindowSize();

        columns = winSize.ws_col;
        rows = winSize.ws_row;
    }

    private static int readKey() throws IOException {
        int key = System.in.read();
        if (key != '\033') {
            return key;
        }

        int secondKey = System.in.read();
        if (secondKey != '[' && secondKey != 'O') {
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

    private static void handleKeyRead(int key) {
        if (key == 'q') {
            resetAttributes();
            cleanup();
            System.exit(0);
        } else {
            System.out.print((char) key + " -> " + key + "\r\n");
        }
    }

    private static void enableRawMode() {
        LibC.Termios termios = new LibC.Termios();

        int rc = LibC.INSTANCE.tcgetattr(LibC.SYSTEM_OUT_FD, termios);
        if (rc != 0) {
            System.out.println("Error calling tcgetattr");
            System.exit(rc);
        }
        defaultAttributes = LibC.Termios.of(termios);

        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);
        termios.c_iflag &= ~(LibC.IXON | LibC.ICRNL);
        termios.c_oflag &= ~(LibC.OPOST);

        termios.c_cc[LibC.VMIN] = 0;
        termios.c_cc[LibC.VTIME] = 1;

        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, termios);
    }

    private static void refreshScreen() {
        StringBuilder builder = new StringBuilder();

        builder.append("\033[2J");
        builder.append("\033[H");

        builder.append("~\r\n".repeat(Math.max(0, rows - 1)));

        String message = "Text Editor v0.1";
        builder.append("\033[7m")
                .append(message)
                .append(" ".repeat(Math.max(0, columns - message.length())))
                .append("\033[0m");

        builder.append("\033[H");
        System.out.print(builder);
    }

    private static LibC.WinSize getWindowSize() {
        final LibC.WinSize winSize = new LibC.WinSize();
        final int rc = LibC.INSTANCE.ioctl(LibC.SYSTEM_OUT_FD, LibC.TI0CGWINSZ, winSize);

        if (rc != 0) {
            System.err.println("Failed to get ioctl. Error code: " + rc);
            System.exit(1);
        }

        return winSize;
    }

    private static void cleanup() {
        System.out.print("\033[2J");
        System.out.print("\033[H");
    }

    private static void resetAttributes() {
        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, defaultAttributes);
    }
}

interface LibC extends Library {
    int SYSTEM_OUT_FD = 0;

    int ISIG = 1, ICANON = 1, ECHO = 10, TCSA_FLUSH = 2, IXON = 2000,
            ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TI0CGWINSZ = 0x5413;

    LibC INSTANCE = Native.load("c", LibC.class);

    @Structure.FieldOrder(value = {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"})
    class Termios extends Structure {
        public int c_iflag, c_oflag, c_cflag, c_lflag;
        public byte[] c_cc = new byte[19];  /* special characters */

        public static Termios of(Termios t) {
            Termios copy = new Termios();
            copy.c_iflag = t.c_iflag;
            copy.c_oflag = t.c_oflag;
            copy.c_cflag = t.c_cflag;
            copy.c_lflag = t.c_lflag;
            copy.c_cc = t.c_cc.clone();
            return copy;
        }

        @Override
        public String toString() {
            return "Termios {" +
                    ", c_iflag=" + c_lflag +
                    ", c_oflag=" + c_oflag +
                    ", c_cflag=" + c_cflag +
                    ", c_lflag=" + c_lflag +
                    ", c_cc=" + Arrays.toString(c_cc);
        }
    }

    @Structure.FieldOrder(value = {"ws_row", "ws_col", "ws_xpixel", "ws_ypixel"})
    class WinSize extends Structure {
        public short ws_row, ws_col, ws_xpixel, ws_ypixel;

        @Override
        public String toString() {
            return "WinSize {" +
                    ", ws_row=" + ws_row +
                    ", ws_col=" + ws_col +
                    ", ws_xpixel=" + ws_xpixel +
                    ", ws_ypixel=" + ws_ypixel;
        }
    }

    int tcgetattr(int fd, Termios termios);

    int tcsetattr(int fd, int optional_actions, Termios termios);

    int ioctl(int fd, int opt, WinSize winSize);
}