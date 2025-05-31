package lexer;

import model.Token;
import model.TokenType;

import java.util.*;

public class Lexer {

    private static final Set<String> KEYWORDS = Set.of("if", "else", "while", "function", "return");
    private static final Set<String> SEPARATORS = Set.of("(", ")", "{", "}", ";", ",");
    private static final Set<String> OPERATORS = Set.of("=", "==", "+", "-", "*", "/", "<", ">", "<=", ">=", "!=");

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            // WHITESPACE
            if (Character.isWhitespace(c)) {
                int start = i;
                while (i < input.length() && Character.isWhitespace(input.charAt(i))) i++;
                tokens.add(new Token(TokenType.WHITESPACE, input.substring(start, i), start, i));
                continue;
            }

            // IDENTIFIER or KEYWORD
            if (Character.isLetter(c) || c == '_') {
                int start = i++;
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) i++;
                String word = input.substring(start, i);
                TokenType type = KEYWORDS.contains(word) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
                tokens.add(new Token(type, word, start, i));
                continue;
            }

            // NUMBER
            if (Character.isDigit(c)) {
                int start = i;
                boolean hasDot = false;
                while (i < input.length() &&
                        (Character.isDigit(input.charAt(i)) || (!hasDot && input.charAt(i) == '.'))) {
                    if (input.charAt(i) == '.') hasDot = true;
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, i), start, i));
                continue;
            }

            // OPERATOR
            boolean matched = false;
            for (int len = 2; len >= 1; len--) {
                if (i + len <= input.length()) {
                    String op = input.substring(i, i + len);
                    if (OPERATORS.contains(op)) {
                        tokens.add(new Token(TokenType.OPERATOR, op, i, i + len));
                        i += len;
                        matched = true;
                        break;
                    }
                }
            }
            if (matched) continue;

            // SEPARATOR
            String chStr = String.valueOf(c);
            if (SEPARATORS.contains(chStr)) {
                tokens.add(new Token(TokenType.SEPARATOR, chStr, i, i + 1));
                i++;
                continue;
            }

            // UNKNOWN
            tokens.add(new Token(TokenType.UNKNOWN, chStr, i, i + 1));
            i++;
        }

        return tokens;
    }
}
