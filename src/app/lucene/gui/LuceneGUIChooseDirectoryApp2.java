package app.lucene.gui;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LuceneGUIChooseDirectoryApp2 extends JFrame {

    private static final Path INDEX_PATH = Paths.get("luceneIndex");
    private final JTextArea logArea;
    private final JTextField queryField;
    private final JComboBox<String> fieldSelector;
    private final JButton indexButton, searchButton, chooseDirButton, recreateButton, exportButton;
    private final JLabel selectedDirLabel;
    private final JProgressBar progressBar;
    private final JCheckBox showFileTimesCheckbox;
    private final AtomicReference<Path> selectedDir = new AtomicReference<>(Paths.get("luceneData"));
    private final List<String> lastResults = new ArrayList<>();

    public LuceneGUIChooseDirectoryApp2() {
        setTitle("Lucene Search Engine (Italian) — GUI");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        enableDarkMode();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // === TOP: query input ===
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        fieldSelector = new JComboBox<>(new String[]{"contenuto", "nome"});
        queryField = new JTextField();
        searchButton = new JButton("🔍 Cerca");
        searchButton.addActionListener(this::searchAction);
        topPanel.add(fieldSelector, BorderLayout.WEST);
        topPanel.add(queryField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        // === CENTER: log area ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // === BOTTOM: controls ===
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        // --- Directory selection ---
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chooseDirButton = new JButton("📁 Scegli cartella .txt");
        chooseDirButton.addActionListener(this::chooseDirectory);
        //selectedDirLabel = new JLabel("Cartella corrente: luceneData/");
        selectedDirLabel = new JLabel("Cartella corrente: " + selectedDir);
        dirPanel.add(chooseDirButton);
        dirPanel.add(selectedDirLabel);

        // --- Indexing controls ---
        JPanel indexPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        indexButton = new JButton("Indicizza file");
        indexButton.addActionListener(this::indexAction);
        recreateButton = new JButton("♻️ Ricrea indice");
        recreateButton.addActionListener(this::recreateAction);
        showFileTimesCheckbox = new JCheckBox("Mostra tempi per file");
        indexPanel.add(indexButton);
        indexPanel.add(recreateButton);
        indexPanel.add(showFileTimesCheckbox);

        // --- Progress bar ---
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        // --- Export results ---
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportButton = new JButton("💾 Esporta risultati");
        exportButton.addActionListener(this::exportResults);
        exportPanel.add(exportButton);

        bottomPanel.add(dirPanel);
        bottomPanel.add(indexPanel);
        bottomPanel.add(progressPanel);
        bottomPanel.add(exportPanel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
    }

    // ============================================================
    // DARK MODE
    // ============================================================
    private void enableDarkMode() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        Color bg = new Color(35, 35, 40);
        Color fg = new Color(220, 220, 220);
        Color accent = new Color(60, 60, 65);

        UIManager.put("Panel.background", bg);
        UIManager.put("OptionPane.background", bg);
        UIManager.put("OptionPane.messageForeground", fg);
        UIManager.put("TextArea.background", accent);
        UIManager.put("TextArea.foreground", fg);
        UIManager.put("TextField.background", accent);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("ComboBox.background", accent);
        UIManager.put("ComboBox.foreground", fg);
        UIManager.put("Button.background", new Color(70, 70, 75));
        UIManager.put("Button.foreground", fg);
        UIManager.put("Label.foreground", fg);
        UIManager.put("CheckBox.background", bg);
        UIManager.put("CheckBox.foreground", fg);
    }

    // ============================================================
    // SELEZIONE CARTELLA
    // ============================================================
    private void chooseDirectory(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Scegli la cartella dei file .txt");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path chosen = chooser.getSelectedFile().toPath();
            selectedDir.set(chosen);
            selectedDirLabel.setText("Cartella corrente: " + chosen);
            log("📁 Selezionata cartella: " + chosen);
        }
    }

    // ============================================================
    // INDICIZZAZIONE (con tempi e checkbox)
    // ============================================================
    private void indexAction(ActionEvent e) {
        new Thread(() -> indexDirectory(false)).start();
    }

    private void recreateAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Vuoi cancellare e ricreare l'indice?",
                "Conferma", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> indexDirectory(true)).start();
        }
    }

    private void indexDirectory(boolean recreate) {
        Path docsPath = selectedDir.get();
        boolean showFileTimes = showFileTimesCheckbox.isSelected();

        try {
            if (!Files.exists(docsPath)) {
                log("❌ La cartella selezionata non esiste!");
                return;
            }

            if (recreate && Files.exists(INDEX_PATH)) {
                log("🗑️ Cancellazione indice...");
                try (var paths = Files.walk(INDEX_PATH)) {
                    paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
                }
            }

            log("🔄 Inizio indicizzazione...");
            long startTime = System.nanoTime();

            Directory dir = FSDirectory.open(INDEX_PATH);
            IndexWriterConfig cfg = new IndexWriterConfig(new ItalianAnalyzer());
            IndexWriter writer = new IndexWriter(dir, cfg);

            List<Path> files = Files.walk(docsPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                log("⚠️ Nessun file .txt trovato nella cartella.");
                writer.close();
                return;
            }

            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(true);
                progressBar.setMaximum(files.size());
                progressBar.setValue(0);
            });

            int count = 0;
            for (Path path : files) {
                long fileStart = System.nanoTime();
                try {
                    String content = Files.readString(path);
                    Document doc = new Document();
                    doc.add(new TextField("nome", path.getFileName().toString(), Field.Store.YES));
                    doc.add(new TextField("contenuto", content, Field.Store.YES));
                    writer.updateDocument(new Term("nome", path.getFileName().toString()), doc);
                    long fileEnd = System.nanoTime();
                    if (showFileTimes) {
                        double fileTimeMs = (fileEnd - fileStart) / 1_000_000.0;
                        log(String.format("Indicizzato: %s (%.1f ms)", path.getFileName(), fileTimeMs));
                    } else {
                        log("Indicizzato: " + path.getFileName());
                    }
                } catch (Exception ex) {
                    log("Errore su " + path + ": " + ex.getMessage());
                }

                final int progress = ++count;
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }

            writer.close();

            long endTime = System.nanoTime();
            double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
            double avgSpeed = count / elapsedSeconds;

            SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
            log(String.format("✅ Indicizzazione completata in %.3f secondi (%d file, %.2f file/sec)",
                    elapsedSeconds, count, avgSpeed));

            // SwingUtilities.invokeLater(() -> {
            //     progressBar.setVisible(false);
            //     log(String.format("✅ Indicizzazione completata in %.3f secondi (%d file, %.2f file/sec)",
            //             elapsedSeconds, count, avgSpeed));

            //     // 🔔 Suono di notifica
            //     Toolkit.getDefaultToolkit().beep();

            //     // 💬 Popup informativo
            //     JOptionPane.showMessageDialog(
            //             this,
            //             String.format("Indicizzazione completata!\n\n" +
            //                     "File indicizzati: %d\nTempo totale: %.2f sec\nVelocità media: %.2f file/sec",
            //                     count, elapsedSeconds, avgSpeed),
            //             "✅ Operazione completata",
            //             JOptionPane.INFORMATION_MESSAGE
            //     );
            // });

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
            log("❌ Errore: " + ex.getMessage());
        }
    }

    // ============================================================
    // RICERCA
    // ============================================================
    private void searchAction(ActionEvent e) {
        new Thread(() -> {
            try {
                if (!Files.exists(INDEX_PATH)) {
                    log("❌ Indice non trovato. Indicizza prima.");
                    return;
                }

                String field = (String) fieldSelector.getSelectedItem();
                String qText = queryField.getText().trim();
                if (qText.isEmpty()) {
                    log("⚠️ Inserisci una query.");
                    return;
                }

                Directory dir = FSDirectory.open(INDEX_PATH);
                DirectoryReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);
                QueryParser parser = new QueryParser(field, new ItalianAnalyzer());
                parser.setAllowLeadingWildcard(true);
                Query query = parser.parse(qText);

                TopDocs results = searcher.search(query, 50);
                lastResults.clear();

                log("\n🔍 Ricerca [" + field + "]: " + qText);
                log("Risultati: " + results.totalHits.value());

                for (ScoreDoc sd : results.scoreDocs) {
                    Document doc = searcher.storedFields().document(sd.doc);
                    String res = doc.get("nome") + " (score: " + String.format("%.3f", sd.score) + ")";
                    lastResults.add(res);
                    log(" - " + res);
                }

                reader.close();
            } catch (Exception ex) {
                log("❌ Errore ricerca: " + ex.getMessage());
            }
        }).start();
    }

    // ============================================================
    // ESPORTAZIONE RISULTATI
    // ============================================================
    private void exportResults(ActionEvent e) {
        if (lastResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun risultato da esportare!");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salva risultati");
        chooser.setSelectedFile(new java.io.File("risultati.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path file = chooser.getSelectedFile().toPath();
            try (FileWriter out = new FileWriter(file.toFile())) {
                if (file.toString().endsWith(".csv")) {
                    out.write("File,Score\n");
                    for (String s : lastResults) {
                        out.write(s.replace(" (score:", ",").replace(")", "") + "\n");
                    }
                } else {
                    for (String s : lastResults) out.write(s + "\n");
                }
                JOptionPane.showMessageDialog(this, "✅ Esportato in: " + file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        }
    }

    // ============================================================
    // LOG AREA
    // ============================================================
    private void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LuceneGUIChooseDirectoryApp2().setVisible(true));
    }
}

