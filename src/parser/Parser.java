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

    private Token consume(TokenType expected) {
        Token token = peek();
        if (token != null && token.type == expected) {
            position++;
            return token;
        } else {
            throw new RuntimeException("Expected " + expected + " but got " + token);
        }
    }

    private Token consume(TokenType expected, String value) {
        Token token = peek();
        if (token != null && token.type == expected && token.value.equals(value)) {
            position++;
            return token;
        } else {
            throw new RuntimeException("Expected " + expected + " with value '" + value + "', but got " + token);
        }
    }

    private boolean match(TokenType type, String value) {
        Token t = peek();
        return t != null && t.type == type && t.value.equals(value);
    }

    private boolean match(TokenType type) {
        Token t = peek();
        return t != null && t.type == type;
    }

    public void parseProgram() {
        while (peek() != null) {
            parseStatement();
        }
        System.out.println("✅ Program parsed successfully.");
    }

    public void parseStatement() {
        if (match(TokenType.KEYWORD, "if")) {
            parseIfStatement();
        } else if (match(TokenType.KEYWORD, "while")) {
            parseWhileStatement();
        } else if (match(TokenType.KEYWORD, "do")) {
            parseDoWhileStatement();
        } else if (match(TokenType.KEYWORD, "for")) {
            parseForStatement();
        } else if (match(TokenType.KEYWORD, "switch")) {
            parseSwitchStatement();
        } else if (match(TokenType.KEYWORD, "function")) {
            parseFunctionDeclaration();
        } else if (match(TokenType.KEYWORD, "return")) {
            consume(TokenType.KEYWORD, "return");
            parseExpression();
            consume(TokenType.SEPARATOR, ";");
        } else if (match(TokenType.KEYWORD, "break") || match(TokenType.KEYWORD, "continue")) {
            consume(TokenType.KEYWORD);
            consume(TokenType.SEPARATOR, ";");
        } else if (match(TokenType.KEYWORD, "print")) {
            consume(TokenType.KEYWORD, "print");
            consume(TokenType.SEPARATOR, "(");
            parseExpression();
            consume(TokenType.SEPARATOR, ")");
            consume(TokenType.SEPARATOR, ";");
        } else if (match(TokenType.SEPARATOR, "{")) {
            parseBlock();
        } else if (peek().type == TokenType.IDENTIFIER) {
            // Peek ahead to see if this is a function call
            Token next = tokens.size() > position + 1 ? tokens.get(position + 1) : null;
            if (next != null && next.type == TokenType.SEPARATOR && next.value.equals("(")) {
                parseExpression(); // function call is a valid expression
                consume(TokenType.SEPARATOR, ";");
            } else {
                parseAssignment();
                consume(TokenType.SEPARATOR, ";");
            }
        }
 else {
            throw new RuntimeException("Bilinmeyen ifade türü: " + peek());
        }
    }

    private void parseAssignment() {
        consume(TokenType.IDENTIFIER);
        Token eq = consume(TokenType.OPERATOR);
        if (!eq.value.equals("=")) throw new RuntimeException("Expected '='");
        parseExpression();
    }

    private void parseIfStatement() {
        consume(TokenType.KEYWORD, "if");
        consume(TokenType.SEPARATOR, "(");
        parseExpression();
        consume(TokenType.SEPARATOR, ")");
        parseBlock();
        if (match(TokenType.KEYWORD, "else")) {
            consume(TokenType.KEYWORD, "else");
            parseBlock();
        }
    }

    private void parseWhileStatement() {
        consume(TokenType.KEYWORD, "while");
        consume(TokenType.SEPARATOR, "(");
        parseExpression();
        consume(TokenType.SEPARATOR, ")");
        parseBlock();
    }

    private void parseDoWhileStatement() {
        consume(TokenType.KEYWORD, "do");
        parseBlock();
        consume(TokenType.KEYWORD, "while");
        consume(TokenType.SEPARATOR, "(");
        parseExpression();
        consume(TokenType.SEPARATOR, ")");
        consume(TokenType.SEPARATOR, ";");
    }

    private void parseForStatement() {
        consume(TokenType.KEYWORD, "for");
        consume(TokenType.SEPARATOR, "(");
        parseAssignment();
        consume(TokenType.SEPARATOR, ";");
        parseExpression();
        consume(TokenType.SEPARATOR, ";");
        parseAssignment();
        consume(TokenType.SEPARATOR, ")");
        parseBlock();
    }

    private void parseSwitchStatement() {
        consume(TokenType.KEYWORD, "switch");
        consume(TokenType.SEPARATOR, "(");
        parseExpression();
        consume(TokenType.SEPARATOR, ")");
        consume(TokenType.SEPARATOR, "{");
        while (match(TokenType.KEYWORD, "case") || match(TokenType.KEYWORD, "default")) {
            if (match(TokenType.KEYWORD, "case")) {
                consume(TokenType.KEYWORD, "case");
                parseExpression();
                consume(TokenType.SEPARATOR, ":");
                while (!match(TokenType.KEYWORD, "case") && !match(TokenType.KEYWORD, "default") && !match(TokenType.SEPARATOR, "}")) {
                    parseStatement();
                }
            } else {
                consume(TokenType.KEYWORD, "default");
                consume(TokenType.SEPARATOR, ":");
                while (!match(TokenType.SEPARATOR, "}")) {
                    parseStatement();
                }
            }
        }
        consume(TokenType.SEPARATOR, "}");
    }

    private void parseFunctionDeclaration() {
        consume(TokenType.KEYWORD, "function");
        consume(TokenType.IDENTIFIER);
        consume(TokenType.SEPARATOR, "(");
        if (peek().type == TokenType.IDENTIFIER) {
            parseParameterList();
        }
        consume(TokenType.SEPARATOR, ")");
        parseBlock();
    }

    private void parseParameterList() {
        consume(TokenType.IDENTIFIER);
        while (match(TokenType.SEPARATOR, ",")) {
            consume(TokenType.SEPARATOR, ",");
            consume(TokenType.IDENTIFIER);
        }
    }

    private void parseBlock() {
        consume(TokenType.SEPARATOR, "{");
        while (peek() != null && !match(TokenType.SEPARATOR, "}")) {
            parseStatement();
        }
        consume(TokenType.SEPARATOR, "}");
    }

    private void parseExpression() {
        parseOr();
    }

    private void parseOr() {
        parseAnd();
        while (match(TokenType.OPERATOR, "||")) {
            consume(TokenType.OPERATOR, "||");
            parseAnd();
        }
    }

    private void parseAnd() {
        parseEquality();
        while (match(TokenType.OPERATOR, "&&")) {
            consume(TokenType.OPERATOR, "&&");
            parseEquality();
        }
    }

    private void parseEquality() {
        parseComparison();
        while (match(TokenType.OPERATOR, "==") || match(TokenType.OPERATOR, "!=")) {
            if (match(TokenType.OPERATOR, "==")) {
                consume(TokenType.OPERATOR, "==");
            } else {
                consume(TokenType.OPERATOR, "!=");
            }
            parseComparison();
        }
    }

    private void parseComparison() {
        parseAdditive();
        while (match(TokenType.OPERATOR, "<") || match(TokenType.OPERATOR, ">") ||
               match(TokenType.OPERATOR, "<=") || match(TokenType.OPERATOR, ">=")) {
            consume(TokenType.OPERATOR);
            parseAdditive();
        }
    }

    private void parseAdditive() {
        parseMultiplicative();
        while (match(TokenType.OPERATOR, "+") || match(TokenType.OPERATOR, "-")) {
            consume(TokenType.OPERATOR);
            parseMultiplicative();
        }
    }

    private void parseMultiplicative() {
        parseUnary();
        while (match(TokenType.OPERATOR, "*") || match(TokenType.OPERATOR, "/")) {
            consume(TokenType.OPERATOR);
            parseUnary();
        }
    }

    private void parseUnary() {
        if (match(TokenType.OPERATOR, "-") || match(TokenType.OPERATOR, "!")) {
            consume(TokenType.OPERATOR);
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() {
        if (match(TokenType.IDENTIFIER)) {
            consume(TokenType.IDENTIFIER);
            if (match(TokenType.SEPARATOR, "(")) {
                consume(TokenType.SEPARATOR, "(");
                if (!match(TokenType.SEPARATOR, ")")) {
                    parseExpression();
                    while (match(TokenType.SEPARATOR, ",")) {
                        consume(TokenType.SEPARATOR, ",");
                        parseExpression();
                    }
                }
                consume(TokenType.SEPARATOR, ")");
            }
        } else if (match(TokenType.NUMBER) || match(TokenType.STRING)) {
            consume(peek().type);
        } else if (match(TokenType.KEYWORD, "true") || match(TokenType.KEYWORD, "false")) {
            consume(TokenType.KEYWORD, peek().value);
        } else if (match(TokenType.SEPARATOR, "(")) {
            consume(TokenType.SEPARATOR, "(");
            parseExpression();
            consume(TokenType.SEPARATOR, ")");
        } else {
            throw new RuntimeException("Unexpected token in expression: " + peek());
        }
    }
}
