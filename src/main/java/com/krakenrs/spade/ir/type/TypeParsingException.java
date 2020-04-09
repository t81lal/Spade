package com.krakenrs.spade.ir.type;

public class TypeParsingException extends RuntimeException {
    private final char[] cs;
    private final int offset;
    private final int errorPos;

    public TypeParsingException(String msg) {
        super(msg);
        cs = null;
        offset = errorPos = 0;
    }

    public TypeParsingException(char[] cs, int offset, int errorPos) {
        this("Illegal char", cs, offset, errorPos, true);
    }

    public TypeParsingException(String msg, char[] cs, int offset, int errorPos, boolean printChar) {
        super(makeErrorMessage(msg, cs, offset, errorPos, printChar));
        this.cs = cs;
        this.offset = offset;
        this.errorPos = errorPos;
    }

    private static String makeErrorMessage(String msg, char[] cs, int offset, int errorPos, boolean printChar) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (printChar) {
            sb.append(" '").append(cs[errorPos]).append("'");
        }
        sb.append(" at pos=").append(errorPos).append(" in \"");
        int wordStart = 0;
        for (int i = errorPos - 1; i >= 0 && Character.isAlphabetic(cs[i]); i--)
            ;
        for (int i = wordStart; i <= errorPos; i++) {
            sb.append(cs[i]);
        }
        return sb.append("\"").toString();
    }

    public char[] getChars() {
        return cs;
    }

    public int getOffset() {
        return offset;
    }

    public int getErrorPos() {
        return errorPos;
    }
}
