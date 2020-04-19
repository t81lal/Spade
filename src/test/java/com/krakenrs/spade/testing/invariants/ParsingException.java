package com.krakenrs.spade.testing.invariants;

public class ParsingException extends Exception {
    private static final long serialVersionUID = 5818807714507128794L;

    private final char[] buf;
    private final int lineNum;
    private final int errorPos;

    public ParsingException(String msg, char[] buf, int line, int errorPos) {
        super(msg);
        this.buf = buf;
        this.lineNum = line;
        this.errorPos = errorPos;
    }

    public char[] getInput() {
        return buf;
    }

    public int getLineNumber() {
        return lineNum;
    }

    public String getLineContents() {
        int linesLeft = lineNum - 1;
        int chPos = 0;
        while (linesLeft-- > 0) {
            while (chPos < buf.length && buf[chPos++] != '\n') {
            }
        }
        StringBuilder sb = new StringBuilder();
        while (chPos < buf.length && buf[chPos++] != '\n') {
            sb.append(buf[chPos - 1]);
        }
        return sb.toString();
    }

    public int getColumn() {
        int linesLeft = lineNum - 1;
        int chPos = 0;
        while (linesLeft-- > 0) {
            while (chPos < buf.length && buf[chPos++] != '\n') {
            }
        }
        return errorPos - chPos;
    }

    public int getErrorPos() {
        return errorPos;
    }
}
