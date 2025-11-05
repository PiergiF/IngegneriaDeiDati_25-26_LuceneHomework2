// package app.lucene.gui;

// import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.concurrent.atomic.AtomicReference;

// import javax.swing.*;
// // import javax.swing.JButton;
// // import javax.swing.JCheckBox;
// // import javax.swing.JComboBox;
// // import javax.swing.JFileChooser;
// // import javax.swing.JFrame;
// // import javax.swing.JLabel;
// // import javax.swing.JPanel;
// // import javax.swing.JProgressBar;
// // import javax.swing.JScrollPane;
// // import javax.swing.JTextArea;
// // import javax.swing.JTextField;
// // import javax.swing.UIManager;
// import javax.swing.border.EmptyBorder;

// public class SetupGUI extends JFrame{

//     private final JTextArea logArea;
//     private final JTextField queryField;
//     private final JComboBox<String> fieldSelector;
//     protected final JButton indexButton, searchButton, chooseDirButton, recreateButton, exportButton;
//     private final JLabel selectedDirLabel;
//     private final JProgressBar progressBar;
//     private final JCheckBox showFileTimesCheckbox;
//     //private final AtomicReference<Path> selectedDir = new AtomicReference<>(Paths.get("luceneData"));
//     private final AtomicReference<Path> selectedDir;

//     //################ CONSTRUCTOR ##################
//     /**
//       * This is the constructor of the GUI
//       */
//     public SetupGUI(Indexer indexer, Searcher searcher, AtomicReference<Path> selectedDataDir){
//         setTitle("Lucene Search Engine (Italian) — GUI");
//         setSize(900, 600);
//         setDefaultCloseOperation(EXIT_ON_CLOSE);
//         setLocationRelativeTo(null);

//         this.selectedDir = selectedDataDir;

//         enableDarkMode();
//         setupPanel(indexer, searcher);
//     }


//     //################ DARK MODE ##################
//     //     /**
//     //      * This Function setup dark mode in the GUI
//     //      */
//     private void enableDarkMode() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//         } catch (Exception ignored) {}

//         Color bg = new Color(35, 35, 40);
//         Color fg = new Color(220, 220, 220);
//         Color accent = new Color(60, 60, 65);

//         UIManager.put("Panel.background", bg);
//         UIManager.put("OptionPane.background", bg);
//         UIManager.put("OptionPane.messageForeground", fg);
//         UIManager.put("TextArea.background", accent);
//         UIManager.put("TextArea.foreground", fg);
//         UIManager.put("TextField.background", accent);
//         UIManager.put("TextField.foreground", fg);
//         UIManager.put("ComboBox.background", accent);
//         UIManager.put("ComboBox.foreground", fg);
//         UIManager.put("Button.background", new Color(70, 70, 75));
//         UIManager.put("Button.foreground", fg);
//         UIManager.put("Label.foreground", fg);
//         UIManager.put("CheckBox.background", bg);
//         UIManager.put("CheckBox.foreground", fg);
//     }



//     private void setupPanel(Indexer indexer, Searcher searcher) {
//         JPanel panel = new JPanel(new BorderLayout(10, 10));
//         panel.setBorder(new EmptyBorder(10, 10, 10, 10));

//         // === TOP: query input ===
//         JPanel topPanel = new JPanel(new BorderLayout(5, 5));
//         fieldSelector = new JComboBox<>(new String[]{"contenuto", "nome"});
//         queryField = new JTextField();
//         searchButton = new JButton("🔍 Cerca");
//         //searchButton.addActionListener(this::searchAction);
//         searchButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e){
//                 searcher.searchAction(e);
//             }
//         });
//         topPanel.add(fieldSelector, BorderLayout.WEST);
//         topPanel.add(queryField, BorderLayout.CENTER);
//         topPanel.add(searchButton, BorderLayout.EAST);

//         // === CENTER: log area ===
//         logArea = new JTextArea();
//         logArea.setEditable(false);
//         JScrollPane scrollPane = new JScrollPane(logArea);

//         // === BOTTOM: controls ===
//         JPanel bottomPanel = new JPanel();
//         bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

//         // --- Directory selection ---
//         JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//         chooseDirButton = new JButton("📁 Scegli cartella .txt");
//         chooseDirButton.addActionListener(this::chooseDirectory);
//         //selectedDirLabel = new JLabel("Cartella corrente: luceneData/");
//         selectedDirLabel = new JLabel("Cartella corrente: " + selectedDir);
//         dirPanel.add(chooseDirButton);
//         dirPanel.add(selectedDirLabel);

//         // --- Indexing controls ---
//         JPanel indexPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//         indexButton = new JButton("Indicizza file");
//         //indexButton.addActionListener(this::indexAction);
//         indexButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e){
//                 indexer.indexAction(e);
//             }
//         });
//         recreateButton = new JButton("♻️ Ricrea indice");
//         recreateButton.addActionListener(this::recreateAction);
//         showFileTimesCheckbox = new JCheckBox("Mostra tempi per file");
//         indexPanel.add(indexButton);
//         indexPanel.add(recreateButton);
//         indexPanel.add(showFileTimesCheckbox);

//         // --- Progress bar ---
//         progressBar = new JProgressBar();
//         progressBar.setStringPainted(true);
//         progressBar.setVisible(false);
//         JPanel progressPanel = new JPanel(new BorderLayout());
//         progressPanel.add(progressBar, BorderLayout.CENTER);

//         // --- Export results ---
//         JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//         exportButton = new JButton("💾 Esporta risultati");
//         exportButton.addActionListener(this::exportResults);
//         exportPanel.add(exportButton);

//         bottomPanel.add(dirPanel);
//         bottomPanel.add(indexPanel);
//         bottomPanel.add(progressPanel);
//         bottomPanel.add(exportPanel);

//         panel.add(topPanel, BorderLayout.NORTH);
//         panel.add(scrollPane, BorderLayout.CENTER);
//         panel.add(bottomPanel, BorderLayout.SOUTH);

//         add(panel);
//     }

//     // ============================================================
//     // SELEZIONE CARTELLA
//     // ============================================================
//     private void chooseDirectory(ActionEvent e) {
//         JFileChooser chooser = new JFileChooser();
//         chooser.setDialogTitle("Scegli la cartella dei file .txt");
//         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

//         if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//             Path chosen = chooser.getSelectedFile().toPath();
//             selectedDir.set(chosen);
//             selectedDirLabel.setText("Cartella corrente: " + chosen);
//             log("📁 Selezionata cartella: " + chosen);
//         }
//     }

//     // ============================================================
//     // LOG AREA
//     // ============================================================
//     private void log(String msg) {
//         SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
//     }
// }




package app.lucene.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import app.lucene.gui.Gui.ThemeMode;

public class Gui extends JFrame{

    private static final Path CONFIG_PATH = Paths.get("lucene_gui.properties");
    private JTextArea logArea;
    private JTextField queryField;
    private JComboBox<String> fieldSelector;
    private  JComboBox<String> themeSelector;
    //private final JButton indexButton, searchButton, chooseDirButton, recreateButton, exportButton;
    private JButton indexButton, searchButton, chooseDirButton, recreateButton, exportButton;
    private JLabel selectedDirLabel;
    private JProgressBar progressBar;
    private JCheckBox showFileTimesCheckbox;
    //private final AtomicReference<Path> selectedDir = new AtomicReference<>(Paths.get("luceneData"));
    //private final AtomicReference<Path> selectedDir;
    //private final java.util.List<String> lastResults = new ArrayList<>();
    //private ThemeMode currentTheme = ThemeMode.DARK;

    private Indexer indexer;
    private Searcher searcher;
    private final FileExporter exporter;

    private ThemeMode currentTheme = ThemeMode.DARK;
    enum ThemeMode { DARK, LIGHT, SYSTEM }

    //################ CONSTRUCTOR ##################
    /**
      * This is the constructor of the GUI
      * @param indexer : refer to Indexer
      * @param searcher : refer to Searcher
      */
    public Gui(String indexPathString, String defaultDataPathString){

        setTitle("Lucene Search Engine (Italian) — GUI");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadUserPreferences();
        applyTheme(currentTheme);

        //this.selectedDir = new AtomicReference<>(Paths.get(DEFAULT_DATA_PATH_STRING));
        this.indexer = new Indexer(indexPathString, this);
        this.searcher = new Searcher(defaultDataPathString, this);
        this.exporter = new FileExporter(this);

        //enableDarkMode();
        //setupPanel(indexer, searcher);
        setupPanel();
    }

    //################ DARK MODE ##################
    /**
     * This Function setup dark mode in the GUI
     */
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

    //################ SETUP GUI PANEL ##################
    /**
     * This Function setup gui panel
     *  @param indexer : refer to Indexer
     *  @param searcher : refer to Searcher
     */
    //private void setupPanel(Indexer indexer, Searcher searcher) {
    private void setupPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // *** TOP ***
        // +++ query input zone +++
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        fieldSelector = new JComboBox<>(new String[]{"contenuto", "nome"});
        queryField = new JTextField();
        searchButton = new JButton("🔍 Cerca");
        searchButton.addActionListener( e -> new Thread(() -> searcher.search(indexer)).start());
        topPanel.add(fieldSelector, BorderLayout.WEST);
        topPanel.add(queryField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        // +++ theme setup +++
        themeSelector = new JComboBox<>(new String[]{"🌙 Dark Mode", "☀️ Light Mode", "🖥️ Sistema"});
        themeSelector.setSelectedIndex(themeIndexFor(currentTheme));
        themeSelector.addActionListener(e -> {
            String sel = (String) themeSelector.getSelectedItem();
            if (sel.contains("Dark")) currentTheme = ThemeMode.DARK;
            else if (sel.contains("Light")) currentTheme = ThemeMode.LIGHT;
            else currentTheme = ThemeMode.SYSTEM;
            applyTheme(currentTheme);
            saveUserPreferences();
            SwingUtilities.updateComponentTreeUI(this);
        });

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        themePanel.add(new JLabel("Tema:"));
        themePanel.add(themeSelector);

        //topPanel.add(queryPanel, BorderLayout.CENTER);
        topPanel.add(themePanel, BorderLayout.EAST);

        // *** CENTER ***
        // +++ log area zone +++
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // *** BOTTOM: controls zone ***
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        // +++ Directory selection section +++
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chooseDirButton = new JButton("📁 Scegli cartella .txt");
        chooseDirButton.addActionListener(this::chooseDirectory);
        selectedDirLabel = new JLabel("Cartella corrente: " + searcher.getDataPath());
        dirPanel.add(chooseDirButton);
        dirPanel.add(selectedDirLabel);

        // +++ indexing controls section +++
        JPanel indexPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        indexButton = new JButton("Indicizza file");
        //indexButton.addActionListener(this::indexAction);
        indexButton.addActionListener(e -> new Thread(() -> indexer.indexDirectory(searcher.getDataPath().get(), false, showFileTimesCheckbox.isSelected())).start());
        recreateButton = new JButton("Ricrea indice");//♻️
        recreateButton.addActionListener(this::recreateAction);
        showFileTimesCheckbox = new JCheckBox("Mostra tempi per file");
        indexPanel.add(indexButton);
        indexPanel.add(recreateButton);
        indexPanel.add(showFileTimesCheckbox);

        // +++ Progress bar for indexing section +++
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        // +++ Export results section +++
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportButton = new JButton("💾 Esporta risultati");
        //exportButton.addActionListener(this::exportResults);
        exportButton.addActionListener(e -> exporter.exportResults(searcher.getlastResults()));
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

    // private void indexAction(ActionEvent e) {
    //     new Thread(() -> indexer.indexDirectory(searcher.getDataPath().get(), false, showFileTimesCheckbox.isSelected())).start();
    // }

    // private void searchAction(ActionEvent e) {
    //     String field = (String) fieldSelector.getSelectedItem();
    //     String queryText = queryField.getText().trim();
    //     new Thread(() -> searcher.search(field, queryText, indexer, lastResults)).start();
    // }

    //####### CHOOSE NEW DATA DIRECTORY ######## 
    private void chooseDirectory(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Scegli la cartella dei file .txt");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path chosen = chooser.getSelectedFile().toPath();
            searcher.getDataPath().set(chosen);
            selectedDirLabel.setText("Cartella corrente: " + chosen);
            log("📁 Selezionata cartella: " + chosen); //codice unicode cartella: U+1F4C1
            saveUserPreferences();
        }
    }

    private void recreateAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Vuoi cancellare e ricreare l'indice?",
                "Conferma", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> indexer.indexDirectory(searcher.getDataPath().get(), true, showFileTimesCheckbox.isSelected())).start();
        }
    }

    private void saveUserPreferences() {
        try {
            Properties props = new Properties();
            props.setProperty("theme", currentTheme.name());
            props.setProperty("directory", searcher.getDataPath().get().toString());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "Lucene GUI Config");
            }
        } catch (IOException ignored) {}
    }

    private void loadUserPreferences() {
        if (!Files.exists(CONFIG_PATH)) return;
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Properties props = new Properties();
            props.load(in);
            currentTheme = ThemeMode.valueOf(props.getProperty("theme", "DARK"));
            searcher.getDataPath().set(Paths.get(props.getProperty("directory", "luceneData")));
        } catch (Exception ignored) {}
    }

    // === Tema e preferenze ===
    private void applyTheme(ThemeMode mode) {
        try {
            switch (mode) {
                case SYSTEM -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                case LIGHT -> UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                case DARK -> { enableDarkMode(); }
            }
        } catch (Exception ignored) {}
    }

    // private void applyTheme(ThemeMode mode) {
    //     Color bg = new Color(30, 30, 35), fg = new Color(230, 230, 230), panel = new Color(245, 245, 245), accent = new Color(60, 60, 65);;
    //     //try {
    //         switch (mode) {
    //             case SYSTEM -> {
    //                 try {
    //                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    //                 } catch (Exception ignored) {} 
    //             }
    //             case LIGHT -> {
    //                 bg = Color.WHITE;
    //                 fg = Color.BLACK;
    //                 panel = new Color(245, 245, 245);
    //                 accent = new Color(230, 230, 230);
    //                 try {
    //                     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    //                 } catch (Exception ignored) {}
    //             }//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    //             case DARK -> {
    //                 bg = new Color(35, 35, 40);
    //                 fg = new Color(220, 220, 220);
    //                 accent = new Color(60, 60, 65);

    //                 panel = new Color(45, 45, 50);
    //                 try{
    //                     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    //                 } catch (Exception ignored) {}
    //                 //enableDarkMode();
    //             }
    //             default -> {
    //                 try {
    //                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    //                 } catch (Exception ignored) {}
    //                 return;
    //             }
    //         }
    //     //} catch (Exception ignored) {}

    //     // UIManager.put("Panel.background", bg);
    //     // UIManager.put("OptionPane.background", bg);
    //     // UIManager.put("OptionPane.messageForeground", fg);
    //     // UIManager.put("TextArea.background", accent);
    //     // UIManager.put("TextArea.foreground", fg);
    //     // UIManager.put("TextField.background", accent);
    //     // UIManager.put("TextField.foreground", fg);
    //     // UIManager.put("ComboBox.background", accent);
    //     // UIManager.put("ComboBox.foreground", fg);
    //     // UIManager.put("Button.background", new Color(70, 70, 75));
    //     // UIManager.put("Button.foreground", fg);
    //     // UIManager.put("Label.foreground", fg);
    //     // UIManager.put("CheckBox.background", bg);
    //     // UIManager.put("CheckBox.foreground", fg);

    //     UIManager.put("Panel.background", panel);
    //     UIManager.put("TextArea.background", bg);
    //     UIManager.put("TextArea.foreground", fg);
    //     UIManager.put("TextField.background", accent);
    //     UIManager.put("TextField.foreground", fg);
    //     UIManager.put("Label.foreground", fg);
    //     UIManager.put("Button.background", accent);
    //     UIManager.put("Button.foreground", fg);
    //     UIManager.put("ComboBox.background", accent);
    //     UIManager.put("ComboBox.foreground", fg);
    // }



    private int themeIndexFor(ThemeMode mode) {
        return switch (mode) { case DARK -> 0; case LIGHT -> 1; case SYSTEM -> 2; };
    }

    // private void exportResults(ActionEvent e) {
    //     if (lastResults.isEmpty()) {
    //         JOptionPane.showMessageDialog(this, "Nessun risultato da esportare!");
    //         return;
    //     }

    //     JFileChooser chooser = new JFileChooser();
    //     chooser.setDialogTitle("Salva risultati");
    //     chooser.setSelectedFile(new java.io.File("risultati.csv"));
    //     if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    //         Path file = chooser.getSelectedFile().toPath();
    //         try (FileWriter out = new FileWriter(file.toFile())) {
    //             if (file.toString().endsWith(".csv")) {
    //                 out.write("File,Score\n");
    //                 for (String s : lastResults) {
    //                     out.write(s.replace(" (score:", ",").replace(")", "") + "\n");
    //                 }
    //             } else {
    //                 for (String s : lastResults) out.write(s + "\n");
    //             }
    //             JOptionPane.showMessageDialog(this, "✅ Esportato in: " + file);
    //         } catch (IOException ex) {
    //             JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
    //         }
    //     }
    // }

    // ########### LOG ##########
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    // ######### GETTER AND SETTER ############
    public JComboBox<String> getFieldSelector() { return this.fieldSelector; }

    public JTextField getQueryField() { return this.queryField; }

    public JCheckBox getShowFileTimesCheckbox() { return this.showFileTimesCheckbox; }

    public JProgressBar getProgressBar() { return this.progressBar; }

}