package ui;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import lexer.Lexer;
import model.Token;
import model.TokenType;
import parser.Parser;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap; class SyntaxHighlighterGUI {

    private static final Map<TokenType, Color> TOKEN_COLORS = new HashMap<>();
    private static final int HIGHLIGHT_DELAY = 300;

    static {
        TOKEN_COLORS.put(TokenType.KEYWORD, new Color(0, 0, 200));
        TOKEN_COLORS.put(TokenType.IDENTIFIER, new Color(0, 128, 0));
        TOKEN_COLORS.put(TokenType.NUMBER, new Color(255, 140, 0));
        TOKEN_COLORS.put(TokenType.OPERATOR, Color.RED);
        TOKEN_COLORS.put(TokenType.SEPARATOR, Color.MAGENTA);
        TOKEN_COLORS.put(TokenType.UNKNOWN, Color.GRAY);
        TOKEN_COLORS.put(TokenType.STRING, new Color(200, 50, 120));
        TOKEN_COLORS.put(TokenType.COMMENT, Color.gray);
    }

    private Timer highlightTimer;
    private final Map<TokenType, Style> styleCache = new HashMap<>();
    private final Lexer lexer = new Lexer();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SyntaxHighlighterGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Real-Time Syntax Highlighter");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JTextPane textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        JLabel statusLabel = new JLabel(" ");
        JButton parseButton = new JButton("Parse Et");

        // Parse Et butonu işlevi
        parseButton.addActionListener(e -> {
            String code = textPane.getText();
            List<Token> tokens = lexer.tokenize(code);
            tokens.removeIf(t -> t.type == TokenType.WHITESPACE);

            try {
                Parser parser = new Parser(tokens);
                parser.parseProgram();
                statusLabel.setText("✅ Sözdizimi geçerli.");
                statusLabel.setForeground(new Color(0, 128, 0));
            } catch (RuntimeException ex) {
                statusLabel.setText("❌ Hata: " + ex.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        });

        // Stil cache'le
        StyledDocument doc = textPane.getStyledDocument();
        for (TokenType type : TOKEN_COLORS.keySet()) {
            Style style = doc.addStyle(type.name(), null);
            StyleConstants.setForeground(style, TOKEN_COLORS.get(type));
            styleCache.put(type, style);
        }

        // Highlight sistemi
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void removeUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void changedUpdate(DocumentEvent e) { scheduleHighlight(); }

            private void scheduleHighlight() {
                if (highlightTimer != null) {
                    highlightTimer.restart();
                } else {
                    highlightTimer = new Timer(HIGHLIGHT_DELAY, e -> {
                        highlight(textPane);
                        highlightTimer.stop();
                    });
                    highlightTimer.setRepeats(false);
                    highlightTimer.start();
                }
            }
        });

        // Layout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(parseButton, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void highlight(JTextPane textPane) {
        String code = textPane.getText();
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(0, code.length(), textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

        List<Token> tokens = lexer.tokenize(code);
        for (Token token : tokens) {
            if (token.type == TokenType.WHITESPACE) continue;
            Style style = styleCache.getOrDefault(token.type, textPane.getStyle(StyleContext.DEFAULT_STYLE));
            int start = Math.max(0, Math.min(token.start, doc.getLength()));
            int len = Math.max(0, Math.min(token.value.length(), doc.getLength() - start));
            if (len > 0) {
                doc.setCharacterAttributes(start, len, style, true);
            }
        }
    }
}
