package lexer;

import model.Token;
import model.TokenType;

import java.util.*;

public class Lexer {

    private static final Set<String> keywords = Set.of(
        "if", "else", "while", "function", "return", "break", "continue", "print",
        "for", "do", "switch", "case", "default", "true", "false", "null",
        "int", "bool", "string", "void"
    );

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            char current = input.charAt(position);

            // Whitespace
            if (Character.isWhitespace(current)) {
                int start = position;
                while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
                    position++;
                }
                tokens.add(new Token(TokenType.WHITESPACE, input.substring(start, position), start, position));
                continue;
            }

            // Comments
            if (current == '/' && position + 1 < input.length()) {
                char next = input.charAt(position + 1);

                // Single-line comment
                if (next == '/') {
                    int start = position;
                    position += 2;
                    StringBuilder comment = new StringBuilder("//");

                    while (position < input.length() && input.charAt(position) != '\n') {
                        comment.append(input.charAt(position));
                        position++;
                    }

                    tokens.add(new Token(TokenType.COMMENT, comment.toString(), start, position));
                    continue;
                }

                // Multi-line comment
                if (next == '*') {
                    int start = position;
                    position += 2;
                    StringBuilder comment = new StringBuilder("/*");

                    while (position + 1 < input.length() && !(input.charAt(position) == '*' && input.charAt(position + 1) == '/')) {
                        comment.append(input.charAt(position));
                        position++;
                    }

                    if (position + 1 < input.length()) {
                        comment.append("*/");
                        position += 2;
                    } else {
                        // Unclosed comment
                        tokens.add(new Token(TokenType.UNKNOWN, comment.toString(), start, position));
                        continue;
                    }

                    tokens.add(new Token(TokenType.COMMENT, comment.toString(), start, position));
                    continue;
                }
            }

            // Identifiers / Keywords
            if (Character.isLetter(current)) {
                int start = position;
                while (position < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
                    position++;
                }
                String word = input.substring(start, position);
                TokenType type = keywords.contains(word) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
                tokens.add(new Token(type, word, start, position));
                continue;
            }

            // Numbers
            if (Character.isDigit(current)) {
                int start = position;
                while (position < input.length() && Character.isDigit(input.charAt(position))) {
                    position++;
                }
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, position), start, position));
                continue;
            }

            // Strings
            if (current == '"') {
                int start = position;
                position++; // skip opening quote
                StringBuilder str = new StringBuilder();

                while (position < input.length() && input.charAt(position) != '"') {
                    str.append(input.charAt(position));
                    position++;
                }

                if (position < input.length() && input.charAt(position) == '"') {
                    position++; // skip closing quote
                    tokens.add(new Token(TokenType.STRING, "\"" + str + "\"", start, position));
                } else {
                    tokens.add(new Token(TokenType.UNKNOWN, "\"" + str, start, position));
                }
                continue;
            }

            // Operators
            if ("=+-*/<>!&|".indexOf(current) != -1) {
                int start = position;
                String op = String.valueOf(current);
                position++;

                if (position < input.length()) {
                    char next = input.charAt(position);
                    if ((current == '=' && next == '=') || (current == '!' && next == '=') ||
                        (current == '<' && next == '=') || (current == '>' && next == '=') ||
                        (current == '&' && next == '&') || (current == '|' && next == '|') ||
                        (current == '+' && next == '+') || (current == '-' && next == '-')) {
                        op += next;
                        position++;
                    } 
                }

                tokens.add(new Token(TokenType.OPERATOR, op, start, position));
                continue;
            }

            // Separators
            if ("[]();{}:,.".indexOf(current) != -1) {
                tokens.add(new Token(TokenType.SEPARATOR, String.valueOf(current), position, position + 1));
                position++;
                continue;
            }

            // Unknown
            tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(current), position, position + 1));
            position++;
        }

        return tokens;
    }
}
