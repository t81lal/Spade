package com.krakenrs.spade.ir.irify;

public class IndentedStringWriter {
    private static final String INDENT = "\t";

    private boolean printIndentation;
    private int indentLevel;
    private final StringBuilder stringBuilder;

    public IndentedStringWriter() {
        stringBuilder = new StringBuilder();
    }

    public void print(char c) {
        if (printIndentation) {
            stringBuilder.append(INDENT.repeat(Math.max(0, indentLevel)));
            printIndentation = false;
        }

        stringBuilder.append(c);
        if (c == '\n') {
            printIndentation = true;
        }
    }

    public void print(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            print(s.charAt(i));
        }
    }

    public void println(CharSequence s) {
        print(s);
        print('\n');
    }

    public void newline() {
        print('\n');
    }

    public void indent() {
        indentLevel++;
    }

    public void deindent() {
        if (indentLevel <= 0) {
            throw new IllegalStateException();
        }
        indentLevel--;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
