package app.lucene.gui;

// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;

public class LuceneApp {

    private static final String INDEX_PATH_STRING = "luceneIndex";
    //private static final AtomicReference<Path> selectedDataDir = new AtomicReference<>(Paths.get("luceneData"));
    private static final String DEFAULT_DATA_PATH_STRING = "luceneData";

    //############## MAIN ##################
    /**
     * This is the start of the app.
     * @param args
     */
    public static void main(String[] args) {
        //SetupGUI gui = new SetupGUI(indexer, searcher)
        //Indexer indexer = new Indexer(INDEX_PATH_STRING);
        //Searcher searcher = new Searcher(DEFAULT_DATA_PATH_STRING);
        //SwingUtilities.invokeLater(() -> new SetupGUI(indexer, searcher, selectedDataDir).setVisible(true)); //gui.setVisible(true));
        SwingUtilities.invokeLater(() -> new Gui(INDEX_PATH_STRING, DEFAULT_DATA_PATH_STRING).setVisible(true));
    }
}