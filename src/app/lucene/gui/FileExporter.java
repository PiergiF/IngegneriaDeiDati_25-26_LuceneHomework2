package app.lucene.gui;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileExporter {

    private final Gui gui;

    public FileExporter(Gui gui) { this.gui = gui; }


    public void exportResults(List<String> lastResults) {
        if (lastResults.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Nessun risultato da esportare!");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salva risultati");
        chooser.setSelectedFile(new java.io.File("risultati.csv"));
        if (chooser.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
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
                JOptionPane.showMessageDialog(gui, "✅ Esportato in: " + file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui, "Errore: " + ex.getMessage());
            }
        }
    }
}
