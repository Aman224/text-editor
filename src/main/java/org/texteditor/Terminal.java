package org.texteditor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.util.Arrays;

public class Terminal {
    private final LibC.Termios defaultAttributes;
    private final LibC.Termios termios;

    private int rows = 10, columns = 10;

    public Terminal() {
        this.termios = getTermios();
        this.defaultAttributes = LibC.Termios.of(this.termios);

        LibC.WinSize winSize = getWinSize();
        this.rows = winSize.ws_row - 1;
        this.columns = winSize.ws_col;

    }

    public void enableRawMode() {
        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);
        termios.c_iflag &= ~(LibC.IXON | LibC.ICRNL);
        termios.c_oflag &= ~(LibC.OPOST);

        termios.c_cc[LibC.VMIN] = 0;
        termios.c_cc[LibC.VTIME] = 1;

        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, termios);
    }

    public void reset() {
        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, defaultAttributes);
    }

    private LibC.Termios getTermios() {
        LibC.Termios termios = new LibC.Termios();
        int rc = LibC.INSTANCE.tcgetattr(LibC.SYSTEM_OUT_FD, termios);
        if (rc != 0) {
            System.out.println("Error calling tcgetattr");
            System.exit(rc);
        }
        return termios;
    }

    private LibC.WinSize getWinSize() {
        final LibC.WinSize winSize = new LibC.WinSize();
        final int rc = LibC.INSTANCE.ioctl(LibC.SYSTEM_OUT_FD, LibC.TI0CGWINSZ, winSize);
        if (rc != 0) {
            System.err.println("Failed to get ioctl. Error code: " + rc);
            System.exit(1);
        }
        return winSize;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
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


