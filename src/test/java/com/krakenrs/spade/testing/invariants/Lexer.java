package com.krakenrs.spade.testing.invariants;

public class Lexer {
    static enum TokenType {
        COMMA, COLON, WORD, STRING_LIT, INT_LIT, FLOAT_LIT, LBRACKET, RBRACKET, LBRACE, RBRACE, NL, EOF
    }

    private final char[] chars;
    private final int buflen;
    private int bp, sp;
    private int tpos;
    private int linesRead;
    private char[] sbuf;
    private char ch;
    private TokenType token;
    private boolean tokenNl;

    public Lexer(char[] chars) {
        this.chars = new char[chars.length + 1];
        // Add an EOF token
        System.arraycopy(chars, 0, this.chars, 0, chars.length);
        this.chars[chars.length] = '\0';
        this.sbuf = new char[128];
        this.buflen = chars.length;
        this.bp = -1;
        this.scanChar();
    }

    public int tokenPos() {
        return tpos;
    }

    public TokenType token() {
        return token;
    }

    public String lexeme() {
        return String.valueOf(sbuf, 0, sp);
    }

    public void setTokenNL(boolean tokenNl) {
        this.tokenNl = tokenNl;
    }

    private void putChar(char c) {
        if (sp == sbuf.length) {
            char[] newbuf = new char[sp * 2];
            System.arraycopy(sbuf, 0, newbuf, 0, sp);
            sbuf = newbuf;
        }
        sbuf[sp++] = c;
    }

    private void scanChar() {
        ch = chars[++bp];
    }

    private void scanLitChar() throws ParsingException {
        if (ch == '\\') {
            scanChar();
            switch (ch) {
                case 'b':
                    putChar('\b');
                    scanChar();
                    break;
                case 't':
                    putChar('\t');
                    scanChar();
                    break;
                case 'n':
                    putChar('\n');
                    scanChar();
                    break;
                case 'f':
                    putChar('\f');
                    scanChar();
                    break;
                case 'r':
                    putChar('\r');
                    scanChar();
                    break;
                case '\\':
                    putChar('\\');
                    scanChar();
                    break;
                case '\'':
                    putChar('\'');
                    scanChar();
                    break;
                case '"':
                    putChar('"');
                    scanChar();
                    break;
                default:
                    error("illegal escape character");
            }
        } else if (bp != buflen) {
            putChar(ch);
            scanChar();
        }
    }

    private void scanDigits() {
        while (isDigit(ch)) {
            putChar(ch);
            scanChar();
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void scanInt() {
        /* int := digit
         *      | onenine digits
         *      | '-' digit
         *      | '-' onenine digits 
         * digits := digit | digit digits
         * digit := '0' | onenine
         * onenine := '1' ... '9'
         */
        if (ch == '-') {
            putChar(ch);
            scanChar();
        }
        if (ch == '0') {
            putChar(ch);
            scanChar();
        } else {
            scanDigits();
        }
    }

    private void scanFrac() {
        /* frac := epsilon
         *       | '.' digits
         */
        if (ch == '.') {
            token = TokenType.FLOAT_LIT;
            putChar(ch);
            scanChar();
            scanDigits();
        } else {
            token = TokenType.INT_LIT;
        }
    }

    private void scanExp() {
        /* exp := epsilon
         *      | 'E' sign digits
         *      | 'e' sign digits
         * sign := epsilon
         *       | '-'
         *       | '+'
         */
        if (ch == 'E' || ch == 'e') {
            putChar(ch);
            scanChar();
            if (ch == '-' || ch == '+') {
                putChar(ch);
                scanChar();
            }
            scanDigits();
        }
    }

    private void scanNumber() {
        /* number := int frac exp */
        scanInt();
        scanFrac();
        scanExp();
    }

    private void scanWord() {
        do {
            putChar(ch);
            scanChar();
            
            switch(ch) {
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                    break;
                default:
                    token = TokenType.WORD;
                    return;
            }
        } while (true);
    }

    public void next() throws ParsingException {
        _next();
    }

    public void _next() throws ParsingException {
        sp = 0;
        tpos = bp;

        while (true) {
            switch (ch) {
                case ' ':
                case '\t': {
                    do {
                        scanChar();
                    } while (ch == ' ' || ch == '\t');
                    tpos = bp;
                    break;
                }
                case '\n': {
                    scanChar();
                    token = TokenType.NL;
                    linesRead++;
                    if (tokenNl) {
                        return;
                    } else {
                        tpos = bp;
                        break;
                    }
                }
                // CR
                case 13: {
                    scanChar();
                    if (ch == '\n') {
                        scanChar();
                    }
                    linesRead++;
                    tpos = bp;
                    break;
                }
                case ',':
                    scanChar();
                    token = TokenType.COMMA;
                    return;
                case ':':
                    scanChar();
                    token = TokenType.COLON;
                    return;
                case '[':
                    scanChar();
                    token = TokenType.LBRACKET;
                    return;
                case ']':
                    scanChar();
                    token = TokenType.RBRACKET;
                    return;
                case '{':
                    scanChar();
                    token = TokenType.LBRACE;
                    return;
                case '}':
                    scanChar();
                    token = TokenType.RBRACE;
                    return;
                case '"': {
                    scanChar();
                    while (ch != '"' && ch != '\n' && bp < buflen) {
                        scanLitChar();
                    }
                    if (ch == '"') {
                        token = TokenType.STRING_LIT;
                        scanChar();
                    } else {
                        error("unterminated string literal");
                    }
                    return;
                }
                case '/': {
                    scanChar();
                    if (ch == '/') {
                        scanChar();
                        // Line comment -> skip
                        while (ch != '\n' && bp < buflen) {
                            scanChar();
                        }
                    } else {
                        error("expected '/' for line comment");
                    }
                    break; // find next real token
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-': {
                    scanNumber();
                    return;
                }
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                    scanWord();
                    return;
                default: {
                    if (bp == buflen) {
                        token = TokenType.EOF;
                        return;
                    } else {
                        error("illegal char");
                    }
                }
            }
        }
    }

    public void error(String msg) throws ParsingException {
        throw new ParsingException(msg, chars, linesRead + 1, tpos);
    }
}
