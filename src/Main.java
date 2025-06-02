import lexer.Lexer;
import model.Token;
import model.TokenType;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String code = """
            function greet(name) {
                if (name == "admin" || name == "root") {
                    print("Access denied");
                } else {
                    print("Welcome " + name);
                }
            }

            flag = true;
            count = 0;

            while (flag && count < 10) {
                if (!(count == 5 || count == 7)) {
                    print("Allowed");
                } else {
                    print("Skipped");
                }

                count = count + 1;

                if (count >= 9) {
                    flag = false;
                }
            }

            greet("root");
            """;

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(code);

        for (Token token : tokens) {
            if (token.type != TokenType.WHITESPACE) { // boşlukları atla
                System.out.println(token);
            }
        }
    }
}
