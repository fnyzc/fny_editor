package parser;

import model.Token;
import model.TokenType;

import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return position < tokens.size() ? tokens.get(position) : null;
    }

    private Token consume(TokenType expectedType) {
        Token token = peek();
        if (token != null && token.type == expectedType) {
            position++;
            return token;
        } else {
            throw new RuntimeException("Expected " + expectedType + " but got " + token);
        }
    }

    public void parseStatement() {
        parseAssignment();
        consume(TokenType.SEPARATOR); // ;
        System.out.println("✅ Statement parsed successfully.");
    }

    private void parseAssignment() {
        consume(TokenType.IDENTIFIER);
        Token eq = consume(TokenType.OPERATOR);
        if (!eq.value.equals("=")) {
            throw new RuntimeException("Expected '=' in assignment.");
        }
        parseExpression();
    }

    private void parseExpression() {
        parseTerm();
        Token next = peek();
        if (next != null && next.type == TokenType.OPERATOR && next.value.equals("+")) {
            consume(TokenType.OPERATOR); // +
            parseTerm();
        }
    }

    private void parseTerm() {
        Token t = peek();
        if (t == null) throw new RuntimeException("Unexpected end of input.");
        if (t.type == TokenType.IDENTIFIER || t.type == TokenType.NUMBER) {
            consume(t.type);
        } else {
            throw new RuntimeException("Expected identifier or number but got " + t);
        }
    }
}
