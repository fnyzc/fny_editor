package ui;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import lexer.Lexer;
import model.Token;
import model.TokenType;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SyntaxHighlighterGUI {

    private static final Map<TokenType, Color> TOKEN_COLORS = new HashMap<>();
    private static final int HIGHLIGHT_DELAY = 300; // ms

    static {
        TOKEN_COLORS.put(TokenType.KEYWORD, new Color(0, 0, 200));
        TOKEN_COLORS.put(TokenType.IDENTIFIER, new Color(0, 128, 0));
        TOKEN_COLORS.put(TokenType.NUMBER, new Color(255, 140, 0));
        TOKEN_COLORS.put(TokenType.OPERATOR, Color.RED);
        TOKEN_COLORS.put(TokenType.SEPARATOR, Color.MAGENTA);
        TOKEN_COLORS.put(TokenType.UNKNOWN, Color.GRAY);
    }

    private Timer highlightTimer;

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> new SyntaxHighlighterGUI().createAndShowGUI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Real-Time Syntax Highlighter");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("❌ Pencere kapatılıyor, çıkış yapılıyor.");
                System.exit(0);
            }
        });

        JTextPane textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        Lexer lexer = new Lexer();

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void removeUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void changedUpdate(DocumentEvent e) { scheduleHighlight(); }

            private void scheduleHighlight() {
                if (highlightTimer != null) {
                    highlightTimer.restart();
                } else {
                    highlightTimer = new Timer(HIGHLIGHT_DELAY, e -> {
                        highlight();
                        highlightTimer.stop();
                    });
                    highlightTimer.setRepeats(false);
                    highlightTimer.start();
                }
            }

            private void highlight() {
                SwingUtilities.invokeLater(() -> {
                    String code = textPane.getText();
                    StyledDocument doc = textPane.getStyledDocument();
                    doc.setCharacterAttributes(0, code.length(), textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

                    List<Token> tokens = lexer.tokenize(code);
                    for (Token token : tokens) {
                        if (token.type == TokenType.WHITESPACE) continue;
                        Style style = textPane.addStyle(token.type.name(), null);
                        StyleConstants.setForeground(style, TOKEN_COLORS.getOrDefault(token.type, Color.BLACK));
                        doc.setCharacterAttributes(token.start, token.value.length(), style, true);
                    }
                });
            }
        });

        frame.getContentPane().add(scrollPane);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
