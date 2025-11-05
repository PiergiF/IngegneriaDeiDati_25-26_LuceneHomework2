package app.lucene.gui;

import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

import javax.swing.*;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;
//import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.document.*;

public class Searcher {

    // public Searcher(SetupGUI gui){
    //     gui.searchButton.addActionListener(this::searchAction);
    // }

    private final AtomicReference<Path> selectedDir;
    private final Gui gui;
    private final List<String> lastResults = new ArrayList<>();

    public Searcher(String defaultDataPathString, Gui gui){
        this.gui = gui;
        this.selectedDir = new AtomicReference<>(Paths.get(defaultDataPathString));
    }


    public AtomicReference<Path> getDataPath() { return this.selectedDir; }

    public List<String> getlastResults() { return this.lastResults; }

    public void search(Indexer indexer){
        try {
                if (!Files.exists(indexer.getIndexPath())) {
                    gui.log("❌ Indice non trovato. Indicizza prima.");
                    return;
                }

                String field = (String) gui.getFieldSelector().getSelectedItem();
                String qText = gui.getQueryField().getText().trim();
                if (qText.isEmpty()) {
                    gui.log("⚠️ Inserisci una query.");
                    return;
                }

                Directory dir = FSDirectory.open(indexer.getIndexPath());
                DirectoryReader reader = DirectoryReader.open(dir);
                IndexSearcher indexSearcher = new IndexSearcher(reader);
                QueryParser parser = new QueryParser(field, new ItalianAnalyzer());
                parser.setAllowLeadingWildcard(true);
                Query query = parser.parse(qText);

                TopDocs results = indexSearcher.search(query, 50);
                lastResults.clear();

                gui.log("\n🔍 Ricerca [" + field + "]: " + qText);
                gui.log("Risultati: " + results.totalHits.value());

                for (ScoreDoc sd : results.scoreDocs) {
                    Document doc = indexSearcher.storedFields().document(sd.doc);
                    String res = doc.get("nome") + " (score: " + String.format("%.3f", sd.score) + ")";
                    lastResults.add(res);
                    gui.log(" - " + res);
                }

                reader.close();
            } catch (Exception ex) {
                gui.log("❌ Errore ricerca: " + ex.getMessage());
            }
    }

    // // ============================================================
    // // RICERCA
    // // ============================================================
    // //private void searchAction(ActionEvent e) {
    // public void searchAction(ActionEvent e) {
    //     new Thread(() -> {
    //         try {
    //             if (!Files.exists(INDEX_PATH)) {
    //                 log("Indice non trovato. Indicizza prima.");
    //                 return;
    //             }

    //             String field = (String) fieldSelector.getSelectedItem();
    //             String qText = queryField.getText().trim();
    //             if (qText.isEmpty()) {
    //                 log("Inserisci una query.");
    //                 return;
    //             }

    //             Directory dir = FSDirectory.open(INDEX_PATH);
    //             DirectoryReader reader = DirectoryReader.open(dir);
    //             IndexSearcher searcher = new IndexSearcher(reader);
    //             QueryParser parser = new QueryParser(field, new ItalianAnalyzer());
    //             parser.setAllowLeadingWildcard(true);
    //             Query query = parser.parse(qText);

    //             TopDocs results = searcher.search(query, 50);
    //             lastResults.clear();

    //             log("\n🔍 Ricerca [" + field + "]: " + qText);
    //             log("Risultati: " + results.totalHits.value());

    //             for (ScoreDoc sd : results.scoreDocs) {
    //                 Document doc = searcher.storedFields().document(sd.doc);
    //                 String res = doc.get("nome") + " (score: " + String.format("%.3f", sd.score) + ")";
    //                 lastResults.add(res);
    //                 log(" - " + res);
    //             }

    //             reader.close();
    //         } catch (Exception ex) {
    //             log("❌ Errore ricerca: " + ex.getMessage());
    //         }
    //     }).start();
    // }
}
