package com.github.aman224;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Terminal {
    private static final Logger logger = LogManager.getLogger();

    private final LibC.Termios defaultAttributes;
    private final LibC.Termios termios;

    private int rows = 10, columns = 10;

    public Terminal() {
        this.termios = getTermios();
        this.defaultAttributes = LibC.Termios.of(this.termios);

        LibC.Winsize winSize = getWinsize();
        this.rows = winSize.ws_row - 1;
        this.columns = winSize.ws_col;
    }

    public void enableRawMode() {
        termios.c_iflag &= ~(LibC.BRKINT | LibC.ICRNL | LibC.INPCK | LibC.ISTRIP | LibC.IXON);
        termios.c_oflag &= ~(LibC.OPOST);
        termios.c_cflag |= (LibC.CS8);
        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);

        termios.c_cc[LibC.VMIN] = 0;
        termios.c_cc[LibC.VTIME] = 1;

        int rc = LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSAFLUSH, termios);
        if (rc != 0) {
            exit(rc, "termios.tcsetattr");
        }
        logger.info("Terminal set to raw mode");
    }

    private LibC.Termios getTermios() {
        LibC.Termios termios = new LibC.Termios();
        int rc = LibC.INSTANCE.tcgetattr(LibC.STDIN_FILENO, termios);

        if (rc != 0) {
            exit(rc, "termios.tcgetattr");
        }
        return termios;
    }

    private LibC.Winsize getWinsize() {
        LibC.Winsize winsize = new LibC.Winsize();
        int rc = LibC.INSTANCE.ioctl(LibC.STDIN_FILENO, LibC.TIOCGWINSZ, winsize);
        if (rc != 0) {
            exit(rc, "ioctl");
        }
        return winsize;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public void reset() {
        int rc = LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSAFLUSH, defaultAttributes);
        if (rc != 0) {
            exit(rc, "termios.tcsetattr");
        }
        logger.info("Terminal Reset");
    }

    private void exit(int err, String method) {
        if (err != 0) {
            logger.error("Error calling {}", method);
        }
        System.exit(err);
    }
}

interface LibC extends Library {
    LibC INSTANCE = Native.load(Platform.isWindows() ? "msvcrt" : "c", LibC.class);

    @Structure.FieldOrder(value = {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"})
    class Termios extends Structure {
        public int c_iflag;      /* input modes */
        public int c_oflag;      /* output modes */
        public int c_cflag;      /* control modes */
        public int c_lflag;      /* local modes */
        public byte[] c_cc = new byte[32];

        public static Termios of(Termios original) {
            Termios copy = new Termios();
            copy.c_iflag = original.c_iflag;
            copy.c_oflag = original.c_oflag;
            copy.c_cflag = original.c_cflag;
            copy.c_lflag = original.c_lflag;
            copy.c_cc = original.c_cc.clone();
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
    class Winsize extends Structure {
        public short ws_row;
        public short ws_col;
        public short ws_xpixel;
        public short ws_ypixel;
    }

    int tcgetattr(int fd, Termios termios);
    int tcsetattr(int fd, int optional_actions, Termios termios);
    int ioctl(int fd, int cmd, Winsize winsize);


    int STDIN_FILENO = 0;

    int ECHO = 0x0000008;
    int TCSAFLUSH = 0x2;
    int ICANON = 0x0000002;
    int ISIG = 0x0000001;
    int IXON = 0x0000400;
    int IEXTEN = 0x0008000;
    int ICRNL = 0x0000100;
    int OPOST = 0x0000001;
    int BRKINT = 0x0000002;
    int INPCK = 0x0000010;
    int ISTRIP = 0x0000020;
    int CS8 = 0x0000030;
    int VTIME = 5;
    int VMIN = 6;

    int TIOCGWINSZ = 0x00005413;
}


