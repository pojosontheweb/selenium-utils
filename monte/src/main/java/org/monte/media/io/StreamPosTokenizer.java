
package org.monte.media.io;

import java.io.*;
import java.util.Vector;




public class StreamPosTokenizer
         {
    private Reader reader = null;


    private int readpos = 0;


    private int startpos = -1, endpos = -1;
    private Vector<Integer> unread = new Vector<Integer>();

    private char buf[] = new char[20];


    private int peekc = NEED_CHAR;

    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;

    private boolean pushedBack;
    private boolean forceLower;

    private int lineno = 1;

    private boolean eolIsSignificantP = false;
    private boolean slashSlashCommentsP = false;
    private boolean slashStarCommentsP = false;


    private char[] slashSlash = new char[] {'/','/'};
    private char[] slashStar = new char[] {'/','*'};
    private char[] starSlash = new char[] {'*','/'};

    private byte ctype[] = new byte[256];
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;

    private boolean isParseHexNumbers = false;
    private boolean isParseExponents = false;


    public int ttype = TT_NOTHING;


    public static final int TT_EOF = -1;


    public static final int TT_EOL = '\n';


    public static final int TT_NUMBER = -2;


    public static final int TT_WORD = -3;


    private static final int TT_NOTHING = -4;


    public String sval;


    public double nval;


    public StreamPosTokenizer() {
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars(128 + 32, 255);
        whitespaceChars(0, ' ');
        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        parseNumbers();
    }



    public StreamPosTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        reader = r;
    }


    public void setReader(Reader r) {
        this.reader = r;
        readpos = 0;
        unread.clear();
        peekc = NEED_CHAR;
        pushedBack = false;
        forceLower = false;
        lineno = 0;
        startpos = endpos = -1;
        ttype = TT_NOTHING;
    }


    public void resetSyntax() {
        for (int i = ctype.length; --i >= 0;)
            ctype[i] = 0;
    }


    public void wordChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] |= CT_ALPHA;
    }


    public void whitespaceChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = CT_WHITESPACE;
    }


    public void ordinaryChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = 0;
    }


    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = 0;
    }


    public void commentChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_COMMENT;
    }


    public void quoteChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_QUOTE;
    }


    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++)
            ctype[i] |= CT_DIGIT;
        ctype['.'] |= CT_DIGIT;
        ctype['-'] |= CT_DIGIT;

    }

    public void parsePlusAsNumber() {
        ctype['+'] |= CT_DIGIT;
    }


    public void parseHexNumbers() {
        parseNumbers();
        isParseHexNumbers = true;
    }

    public void parseExponents() {
        parseNumbers();
        isParseExponents = true;
    }


    public void eolIsSignificant(boolean flag) {
        eolIsSignificantP = flag;
    }


    public void slashStarComments(boolean flag) {
        slashStarCommentsP = flag;
    }


    public void slashSlashComments(boolean flag) {
        slashSlashCommentsP = flag;
    }


    public void lowerCaseMode(boolean fl) {
        forceLower = fl;
    }


    private int read() throws IOException {

        int data;
        if (unread.size() > 0) {
            data = ((Integer) unread.lastElement()).intValue();
            unread.removeElementAt(unread.size() - 1);
        } else {
            data = reader.read();
        }
        if (data != -1) { readpos++; }
        return data;
    }

    private void unread(int c) {
        unread.addElement(c);
        readpos--;
    }


    @SuppressWarnings("empty-statement")
    public int nextToken() throws IOException {
        if (pushedBack) {
            pushedBack = false;
            return ttype;
        }
        byte ct[] = ctype;
        sval = null;

        int c = peekc;
        if (c < 0)
            c = NEED_CHAR;
        if (c == SKIP_LF) {
            c = read();
            if (c < 0) {

                startpos = endpos = readpos - 1;
                return ttype = TT_EOF;
            }
            if (c == '\n')
                c = NEED_CHAR;
        }
        if (c == NEED_CHAR) {
            c = read();
            if (c < 0) {

                startpos = endpos = readpos - 1;
                return ttype = TT_EOF;
            }
        }
        ttype = c;


        peekc = NEED_CHAR;

        int ctype = c < 256 ? ct[c] : CT_ALPHA;
        while ((ctype & CT_WHITESPACE) != 0) {
            if (c == '\r') {
                lineno++;
                if (eolIsSignificantP) {
                    peekc = SKIP_LF;

                    startpos = endpos = readpos - 1;
                    return ttype = TT_EOL;
                }
                c = read();
                if (c == '\n')
                    c = read();
            } else {
                if (c == '\n') {
                    lineno++;
                    if (eolIsSignificantP) {

                        startpos = endpos = readpos - 1;
                        return ttype = TT_EOL;
                    }
                }
                c = read();
            }
            if (c < 0) {

                startpos = endpos = readpos;
                return ttype = TT_EOF;
            }
            ctype = c < 256 ? ct[c] : CT_ALPHA;
        }


        startpos = readpos - 1;


        hex: if (((ctype & CT_DIGIT) != 0) &&
                c == '0' && isParseHexNumbers) {
            c = read();
            if (c == 'x') {
                int digits = 0;
                long hval = 0;
                while (digits < 16) {
                    c = read();
                    if (c >= '0' && c <= '9') {
                        hval = (hval << 4) | (c - '0');
                    } else if (c >= 'A' && c <= 'F') {
                        hval = (hval << 4) | (c - 'A' + 10);
                    } else if (c >= 'a' && c <= 'f') {
                        hval = (hval << 4) | (c - 'a' + 10);
                    } else {
                        unread(c);
                        if (digits == 0) {
                            sval = "0x";
                            endpos = readpos - 1;
                            return ttype = TT_WORD;
                        } else {
                            nval = (double) hval;
                            endpos = readpos - 1;
                            return ttype = TT_NUMBER;
                        }
                    }
                    digits++;
                }
                nval = (double) hval;
                endpos = readpos - 1;
                return ttype = TT_NUMBER;
            } else {
                unread(c);
                c = '0';
            }
        }

        digit: if ((ctype & CT_DIGIT) != 0) {
            int digits = 0;
            boolean neg = false;
            if (c == '-') {
                c = read();
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;

                    if (('-' & CT_ALPHA) != 0) {
                        unread(c);
                        c = '-';
                        break digit;
                    } else {
                        endpos = readpos - 1;
                        return ttype = '-';
                    }
                }
                neg = true;
            } else if (c == '+') {
                c = read();
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;

                    if (('+' & CT_ALPHA) != 0) {
                        unread(c);
                        c = '+';
                        break digit;
                    } else {
                        endpos = readpos - 1;
                        return ttype = '-';
                    }
                }
                neg = false;
            }

            double v = 0;
            int decexp = 0;
            int seendot = 0;
            while (true) {
                if (c == '.' && seendot == 0)
                    seendot = 1;
                else if ('0' <= c && c <= '9') {
                    digits++;
                    v = v * 10 + (c - '0');
                    decexp += seendot;
                } else
                    break;
                c = read();
            }
            peekc = c;
            if (decexp != 0) {
                double denom = 10;
                decexp--;
                while (decexp > 0) {
                    denom *= 10;
                    decexp--;
                }

                v = v / denom;
            }
            nval = neg ? -v : v;

            endpos = (c == -1) ? readpos - 1 : readpos - 2;
            if (digits == 0) {
                if (('.' & CT_ALPHA) != 0) {
                    unread(c);
                    if (neg) {
                        unread('.');
                        c = '-';
                    } else {
                        read();
                        c = '.';
                    }
                    break digit;
                } else {
                    return ttype = '.';
                }
            } else {
                if (isParseExponents) {
                    if (c == 'E' || c == 'e') {
                        c = read();

                        digits = 0;
                        neg = false;
                        if (c == '-') {
                            c = read();
                            if (c < '0' || c > '9') {
                                unread(c);
                                unread('E');
                                return ttype = TT_NUMBER;
                            }
                            neg = true;
                        }
                        v = 0;
                        decexp = 0;
                        while (true) {
                            if ('0' <= c && c <= '9') {
                                digits++;
                                v = v * 10 + (c - '0');
                            } else {
                                break;
                            }
                            c = read();
                        }
                        peekc = c;
                        nval *= Math.pow(10, (neg) ? -v : v);
                    }
                }
                return ttype = TT_NUMBER;
            }
        }

        if ((ctype & CT_ALPHA) != 0) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    char nb[] = new char[buf.length * 2];
                    System.arraycopy(buf, 0, nb, 0, buf.length);
                    buf = nb;
                }
                buf[i++] = (char) c;
                c = read();
                ctype = c < 0 ? CT_WHITESPACE : c < 256 ? ct[c] : CT_ALPHA;
            } while ((ctype & (CT_ALPHA | CT_DIGIT)) != 0);
            peekc = c;
            sval = String.copyValueOf(buf, 0, i);
            if (forceLower)
                sval = sval.toLowerCase();

            endpos = (c == -1) ? readpos - 1 : readpos - 2;
            return ttype = TT_WORD;
        }

        if ((ctype & CT_QUOTE) != 0) {
            ttype = c;
            int i = 0;

            int d = read();
            while (d >= 0 && d != ttype && d != '\n' && d != '\r') {
                if (d == '\\') {
                    c = read();
                    int first = c;
                    if (c >= '0' && c <= '7') {
                        c = c - '0';
                        int c2 = read();
                        if ('0' <= c2 && c2 <= '7') {
                            c = (c << 3) + (c2 - '0');
                            c2 = read();
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = read();
                            } else
                                d = c2;
                        } else
                            d = c2;
                    } else {
                        switch (c) {
                            case 'a':
                                c = 0x7;
                                break;
                            case 'b':
                                c = '\b';
                                break;
                            case 'f':
                                c = 0xC;
                                break;
                            case 'n':
                                c = '\n';
                                break;
                            case 'r':
                                c = '\r';
                                break;
                            case 't':
                                c = '\t';
                                break;
                            case 'v':
                                c = 0xB;
                                break;
                        }
                        d = read();
                    }
                } else {
                    c = d;
                    d = read();
                }
                if (i >= buf.length) {
                    char nb[] = new char[buf.length * 2];
                    System.arraycopy(buf, 0, nb, 0, buf.length);
                    buf = nb;
                }
                buf[i++] = (char)c;
            }


            peekc = (d == ttype) ? NEED_CHAR : d;

            sval = String.copyValueOf(buf, 0, i);

            endpos = readpos - 2;
            return ttype;
        }



        if (slashSlashCommentsP && c == slashSlash[0]
                || slashStarCommentsP && c == slashStar[0]) {
            if (c == slashStar[0] && slashStar.length == 1) {


                while ((c = read()) != starSlash[0]) {
                    if (c == '\r') {
                        lineno++;
                        c = read();
                        if (c == '\n') {
                            c = read();
                        }
                    } else {
                        if (c == '\n') {
                            lineno++;
                            c = read();
                        }
                    }
                    if (c < 0) {
                        endpos = readpos;
                        return ttype = TT_EOF;
                    }
                }
                return nextToken();
            } else if (c == slashSlash[0] && slashSlash.length == 1) {


                while ((c = read()) != '\n' && c != '\r' && c >= 0);
                peekc = c;
                return nextToken();
            } else {


                c = read();
                if (c == slashStar[1] && slashStarCommentsP) {
                    int prevc = 0;
                    while ((c = read()) != starSlash[1] || prevc != starSlash[0]) {
                        if (c == '\r') {
                            lineno++;
                            c = read();
                            if (c == '\n') {
                                c = read();
                            }
                        } else {
                            if (c == '\n') {
                                lineno++;
                                c = read();
                            }
                        }
                        if (c < 0) {
                            endpos = readpos;
                            return ttype = TT_EOF;
                        }
                        prevc = c;
                    }
                    return nextToken();
                } else if (c == slashSlash[1] && slashSlashCommentsP) {
                    while ((c = read()) != '\n' && c != '\r' && c >= 0);
                    peekc = c;
                    return nextToken();
                } else {

                    if ((ct[slashSlash[0]] & CT_COMMENT) != 0) {
                        while ((c = read()) != '\n' && c != '\r' && c >= 0);
                        peekc = c;
                        return nextToken();
                    } else {
                        peekc = c;

                        endpos = readpos - 2;
                        return ttype = slashSlash[0];
                    }
                }
            }
        }

        if ((ctype & CT_COMMENT) != 0) {
            while ((c = read()) != '\n' && c != '\r' && c >= 0);
            peekc = c;

            return nextToken();
        }


        endpos = readpos - 1;
        return ttype = c;
    }

    public int nextChar() throws IOException {
        if (pushedBack) {
            throw new IllegalStateException("can't read char when a token has been pushed back");
        }
        if (peekc == NEED_CHAR) {
            return read();
        } else {
            int ch = peekc;
            peekc = NEED_CHAR;
            return ch;
        }
    }

    public void pushCharBack(int ch) throws IOException {
        if (pushedBack) {
            throw new IllegalStateException("can't push back char when a token has been pushed back");
        }
        if (peekc == NEED_CHAR) {
            unread(ch);
        } else {
            unread(peekc);
            peekc = NEED_CHAR;
            unread(ch);
        }
    }

    public void setSlashStarTokens(String slashStar, String starSlash) {
        if (slashStar.length() != starSlash.length()) {
            throw new IllegalArgumentException("SlashStar and StarSlash tokens must be of same length: '"+slashStar+"' '"+starSlash+"'");
        }
        if (slashStar.length() < 1 || slashStar.length() > 2) {
            throw new IllegalArgumentException("SlashStar and StarSlash tokens must be of length 1 or 2: '"+slashStar+"' '"+starSlash+"'");
        }
        this.slashStar = slashStar.toCharArray();
        this.starSlash = starSlash.toCharArray();
        commentChar(this.slashStar[0]);
    }

    public void setSlashSlashToken(String slashSlash) {
        if (slashSlash.length() < 1 || slashSlash.length() > 2) {
            throw new IllegalArgumentException("SlashSlash token must be of length 1 or 2: '"+slashSlash+"'");
        }
        this.slashSlash = slashSlash.toCharArray();
        commentChar(this.slashSlash[0]);
    }


    public void pushBack() {
        if (ttype != TT_NOTHING)
            pushedBack = true;
    }


    public int lineno() {
        return lineno;
    }


    public int getStartPosition() {
        return startpos;
    }

    public void setStartPosition(int p) {
        startpos = p;
    }

    public int getEndPosition() {
        return endpos;
    }


    public void consumeGreedy(String greedyToken) {
        if (greedyToken.length() < sval.length()) {
            pushBack();
            setStartPosition(getStartPosition() + greedyToken.length());
            sval = sval.substring(greedyToken.length());
        }
    }

    @Override
    public String toString() {
        String ret;
        switch (ttype) {
            case TT_EOF:
                ret = "EOF";
                break;
            case TT_EOL:
                ret = "EOL";
                break;
            case TT_WORD:
                ret = sval;
                break;
            case TT_NUMBER:
                ret = "n=" + nval;
                break;
            case TT_NOTHING:
                ret = "NOTHING";
                break;
            default:{
                char s[] = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char) ttype;
                ret = new String(s);
                break;
            }
        }
        return "Token[" + ret + "], line " + lineno;
    }
}
