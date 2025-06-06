package ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lexer.Lexer;
import model.Token;
import model.TokenType;
import parser.Parser;

public class SyntaxHighlighterGUI {

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
        TOKEN_COLORS.put(TokenType.COMMENT, Color.GRAY);
    }

    private Timer highlightTimer;
    private final Map<TokenType, Style> styleCache = new HashMap<>();
    private final Lexer lexer = new Lexer();
    private File currentFile = null;

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

        // === Menü Çubuğu ve Dosya Desteği ===
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Dosya");

        JMenuItem openItem = new JMenuItem("Aç…");
        JMenuItem saveItem = new JMenuItem("Kaydet…");
        JMenuItem exitItem = new JMenuItem("Çıkış");

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // TextPane ve stiller
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        StyledDocument doc = textPane.getStyledDocument();

        for (TokenType type : TOKEN_COLORS.keySet()) {
            Style style = doc.addStyle(type.name(), null);
            StyleConstants.setForeground(style, TOKEN_COLORS.get(type));
            styleCache.put(type, style);
        }

        // Satır numarası paneli
        LineNumberPanel lineNumbers = new LineNumberPanel(textPane);

        // ScrollPane içine textPane ve satır numarası paneli
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumbers);

        // Parse butonu ve durum etiketi
        JButton parseButton = new JButton("Parse Et");
        JLabel statusLabel = new JLabel(" ");

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
                // Hata token bilgisine göre vurgulama gerekirse burada eklenebilir
            }
        });

        // Belge değiştiğinde vurgulamayı erteleyerek çalıştır
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void removeUpdate(DocumentEvent e) { scheduleHighlight(); }
            public void changedUpdate(DocumentEvent e) { scheduleHighlight(); }

            private void scheduleHighlight() {
                if (highlightTimer != null) {
                    highlightTimer.restart();
                } else {
                    highlightTimer = new Timer(HIGHLIGHT_DELAY, ev -> {
                        highlight(textPane);
                        highlightTimer.stop();
                    });
                    highlightTimer.setRepeats(false);
                    highlightTimer.start();
                }
            }
        });

        // Alt panel: önce parse/status, sonra renk legend
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JPanel buttonStatusPanel = new JPanel(new BorderLayout(5, 5));
        buttonStatusPanel.add(parseButton, BorderLayout.WEST);
        buttonStatusPanel.add(statusLabel, BorderLayout.CENTER);

        // Renk legend özelliklerini burada oluştur
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        legendPanel.setBackground(new Color(245, 245, 245));

        for (Map.Entry<TokenType, Color> entry : TOKEN_COLORS.entrySet()) {
            TokenType type = entry.getKey();
            Color color = entry.getValue();

            // Küçük renk kutucuğu
            JLabel colorBox = new JLabel("   ");
            colorBox.setOpaque(true);
            colorBox.setBackground(color);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            String displayName = switch (type) {
                case KEYWORD    -> "Keyword";
                case IDENTIFIER -> "Identifier";
                case NUMBER     -> "Number";
                case OPERATOR   -> "Operator";
                case SEPARATOR  -> "Separator";
                case STRING     -> "String";
                case COMMENT    -> "Comment";
                case UNKNOWN    -> "Unknown";
                default         -> type.name();
            };
            JLabel nameLabel = new JLabel(displayName);

            legendPanel.add(colorBox);
            legendPanel.add(nameLabel);
        }

        bottomPanel.add(buttonStatusPanel, BorderLayout.NORTH);
        bottomPanel.add(legendPanel, BorderLayout.SOUTH);

        // Ana pencere düzeni
        frame.getContentPane().setLayout(new BorderLayout(5, 5));
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // === Menü Öğeleri İçin ActionListener’lar ===
        openItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java ve Metin Dosyaları", "java", "txt"));
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String content = new String(bytes, StandardCharsets.UTF_8);
                    textPane.setText(content);
                    currentFile = file;
                    statusLabel.setText("📂 Açıldı: " + file.getName());
                    statusLabel.setForeground(Color.BLACK);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Dosya okunurken bir hata oluştu:\n" + ex.getMessage(),
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (currentFile != null) {
                fileChooser.setCurrentDirectory(currentFile.getParentFile());
                fileChooser.setSelectedFile(currentFile);
            }
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java ve Metin Dosyaları", "java", "txt"));
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                    writer.write(textPane.getText());
                    currentFile = file;
                    statusLabel.setText("💾 Kaydedildi: " + file.getName());
                    statusLabel.setForeground(Color.BLACK);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Dosya kaydedilirken bir hata oluştu:\n" + ex.getMessage(),
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitItem.addActionListener(e -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void highlight(JTextPane textPane) {
        String code = textPane.getText();
        StyledDocument doc = textPane.getStyledDocument();

        // Önce tüm metni varsayılan stile döndür
        doc.setCharacterAttributes(0, code.length(), textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

        // Tokenize ederek renkleri uygula
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
    private static class LineNumberPanel extends JPanel implements DocumentListener {
        private final JTextPane textPane;
        private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        private final Color lineNumberColor = Color.DARK_GRAY;
        private final int MARGIN = 5;

        public LineNumberPanel(JTextPane tp) {
            this.textPane = tp;
            tp.getDocument().addDocumentListener(this);
            setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
            setBackground(new Color(230, 230, 230));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(font);
            g2.setColor(lineNumberColor);

            StyledDocument doc = textPane.getStyledDocument();
            Element root = doc.getDefaultRootElement();
            FontMetrics fm = g2.getFontMetrics();
            Rectangle clip = g2.getClipBounds();

            int lineCount = root.getElementCount();
            int xBase = getWidth() - MARGIN;

            for (int i = 0; i < lineCount; i++) {
                Element lineElem = root.getElement(i);
                int startOffset = lineElem.getStartOffset();

                try {
                    Rectangle2D r = textPane.modelToView2D(startOffset);
                    if (r == null) continue;
                    double y = r.getY() + fm.getAscent();

                    if (y + fm.getDescent() < clip.y) continue;
                    if (y > clip.y + clip.height) break;

                    String lineNumber = String.valueOf(i + 1);
                    int strWidth = fm.stringWidth(lineNumber);
                    g2.drawString(lineNumber, xBase - strWidth, (float) y);

                } catch (BadLocationException ex) {
                    // Hata durumunda atla
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) { repaint(); }
        @Override
        public void removeUpdate(DocumentEvent e) { repaint(); }
        @Override
        public void changedUpdate(DocumentEvent e) { repaint(); }
    }
}
