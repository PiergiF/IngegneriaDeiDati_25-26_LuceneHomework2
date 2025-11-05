package app.lucene.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
//import javax.swing.SwingUtilities;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.*;

public class Indexer {

    public final Path INDEX_PATH;
    private final Gui gui;

    public Path getIndexPath(){
        return INDEX_PATH;
    }

    //############## CONSTRUCTOR ##################
    /**
     * This is the constructor of the GUI.
     * @param indexPathString : string indicating the directory path
     */
    public Indexer(String indexPathString, Gui gui){
        this.gui = gui;
        INDEX_PATH = Paths.get(indexPathString);
        try{
            if (!Files.exists(INDEX_PATH)) {
                Files.createDirectories(INDEX_PATH);
            }
        }catch (IOException e){
            System.err.println("Errore nella ricerca e creazione della directory");
            e.printStackTrace();
        }
    }

    // ============================================================
    // INDICIZZAZIONE (con tempi e checkbox)
    // ============================================================
    //private void indexAction(ActionEvent e) {
    // public void indexAction(ActionEvent e) {
    //     new Thread(() -> indexDirectory(false)).start();
    // }

    //private void recreateAction(ActionEvent e) {
    public void recreateAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(gui,
                "Vuoi cancellare e ricreare l'indice?",
                "Conferma", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> indexDirectory(this.getIndexPath(), true, gui.getShowFileTimesCheckbox().isSelected())).start();
        }
    }

    // private void indexDirectory(boolean recreate) {
    //     Path docsPath = selectedDir.get();
    //     boolean showFileTimes = showFileTimesCheckbox.isSelected();

    //     try {
    //         if (!Files.exists(docsPath)) {
    //             log("❌ La cartella selezionata non esiste!");
    //             return;
    //         }

    //         if (recreate && Files.exists(INDEX_PATH)) {
    //             log("🗑️ Cancellazione indice...");
    //             try (var paths = Files.walk(INDEX_PATH)) {
    //                 paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
    //                     try { Files.delete(p); } catch (IOException ignored) {}
    //                 });
    //             }
    //         }

    //         log("🔄 Inizio indicizzazione...");
    //         long startTime = System.nanoTime();

    //         Directory dir = FSDirectory.open(INDEX_PATH);
    //         IndexWriterConfig cfg = new IndexWriterConfig(new ItalianAnalyzer());
    //         IndexWriter writer = new IndexWriter(dir, cfg);

    //         List<Path> files = Files.walk(docsPath)
    //                 .filter(Files::isRegularFile)
    //                 .filter(p -> p.toString().endsWith(".txt"))
    //                 .collect(Collectors.toList());

    //         if (files.isEmpty()) {
    //             log("⚠️ Nessun file .txt trovato nella cartella.");
    //             writer.close();
    //             return;
    //         }

    //         SwingUtilities.invokeLater(() -> {
    //             progressBar.setVisible(true);
    //             progressBar.setMaximum(files.size());
    //             progressBar.setValue(0);
    //         });

    //         int count = 0;
    //         for (Path path : files) {
    //             long fileStart = System.nanoTime();
    //             try {
    //                 String content = Files.readString(path);
    //                 Document doc = new Document();
    //                 doc.add(new TextField("nome", path.getFileName().toString(), Field.Store.YES));
    //                 doc.add(new TextField("contenuto", content, Field.Store.YES));
    //                 writer.updateDocument(new Term("nome", path.getFileName().toString()), doc);
    //                 long fileEnd = System.nanoTime();
    //                 if (showFileTimes) {
    //                     double fileTimeMs = (fileEnd - fileStart) / 1_000_000.0;
    //                     log(String.format("Indicizzato: %s (%.1f ms)", path.getFileName(), fileTimeMs));
    //                 } else {
    //                     log("Indicizzato: " + path.getFileName());
    //                 }
    //             } catch (Exception ex) {
    //                 log("Errore su " + path + ": " + ex.getMessage());
    //             }

    //             final int progress = ++count;
    //             SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    //         }

    //         writer.close();

    //         long endTime = System.nanoTime();
    //         double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
    //         double avgSpeed = count / elapsedSeconds;

    //         SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
    //         log(String.format("✅ Indicizzazione completata in %.3f secondi (%d file, %.2f file/sec)",
    //                 elapsedSeconds, count, avgSpeed));

    //         // SwingUtilities.invokeLater(() -> {
    //         //     progressBar.setVisible(false);
    //         //     log(String.format("✅ Indicizzazione completata in %.3f secondi (%d file, %.2f file/sec)",
    //         //             elapsedSeconds, count, avgSpeed));

    //         //     // 🔔 Suono di notifica
    //         //     Toolkit.getDefaultToolkit().beep();

    //         //     // 💬 Popup informativo
    //         //     JOptionPane.showMessageDialog(
    //         //             this,
    //         //             String.format("Indicizzazione completata!\n\n" +
    //         //                     "File indicizzati: %d\nTempo totale: %.2f sec\nVelocità media: %.2f file/sec",
    //         //                     count, elapsedSeconds, avgSpeed),
    //         //             "✅ Operazione completata",
    //         //             JOptionPane.INFORMATION_MESSAGE
    //         //     );
    //         // });

    //     } catch (Exception ex) {
    //         SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
    //         log("❌ Errore: " + ex.getMessage());
    //     }
    // }

    public void indexDirectory(Path dataPath, boolean recreate, boolean showTimes) {
        try {
            if (!Files.exists(dataPath)) {
                gui.log("❌ La cartella selezionata non esiste!");
                return;
            }

            if (recreate && Files.exists(INDEX_PATH)) {
                gui.log("🗑️ Cancellazione indice...");
                try (var paths = Files.walk(INDEX_PATH)) {
                    paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
                }
            }

            gui.log("🔄 Inizio indicizzazione...");
            long startTime = System.nanoTime();

            Directory dir = FSDirectory.open(INDEX_PATH);
            IndexWriterConfig cfg = new IndexWriterConfig(new ItalianAnalyzer());
            IndexWriter writer = new IndexWriter(dir, cfg);

            List<Path> files = Files.walk(dataPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                gui.log("⚠️ Nessun file .txt trovato nella cartella.");
                writer.close();
                return;
            }

            SwingUtilities.invokeLater(() -> {
                gui.getProgressBar().setVisible(true);
                gui.getProgressBar().setMaximum(files.size());
                gui.getProgressBar().setValue(0);
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
                    if (showTimes) {
                        double fileTimeMs = (fileEnd - fileStart) / 1_000_000.0;
                        gui.log(String.format("Indicizzato: %s (%.1f ms)", path.getFileName(), fileTimeMs));
                    } else {
                        gui.log("Indicizzato: " + path.getFileName());
                    }
                } catch (Exception ex) {
                    gui.log("Errore su " + path + ": " + ex.getMessage());
                }

                final int progress = ++count;
                SwingUtilities.invokeLater(() -> gui.getProgressBar().setValue(progress));
            }

            writer.close();

            long endTime = System.nanoTime();
            double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
            double avgSpeed = count / elapsedSeconds;

            SwingUtilities.invokeLater(() -> gui.getProgressBar().setVisible(false));
            gui.log(String.format("✅ Indicizzazione completata in %.3f secondi (%d file, %.2f file/sec)",
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
            SwingUtilities.invokeLater(() -> gui.getProgressBar().setVisible(false));
            gui.log("❌ Errore: " + ex.getMessage());
        }
    }
}
