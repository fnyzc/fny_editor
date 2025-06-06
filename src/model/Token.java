package model;

public class Token {
    public TokenType type;
    public String value;
    public int start;
    public int end;

    public Token(TokenType type, String value, int start, int end) {
        this.type = type;
        this.value = value;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "[" + type + " : '" + value + "' (" + start + "-" + end + ")]";
    }
}