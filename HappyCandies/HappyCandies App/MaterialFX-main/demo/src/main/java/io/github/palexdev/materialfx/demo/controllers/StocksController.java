package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVFormat;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVRecord;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;



/**
 * Contrôleur pour la gestion des stocks.
 * Ce contrôleur permet de charger les données des stocks depuis un fichier CSV et d'afficher les informations correspondantes.
 */
public class StocksController implements Initializable {

    private final Stage stage;

    @FXML
    private MFXComboBox<String> boxElements;

    @FXML
    private Text right1stock;

    @FXML
    private Text right2stock;

    @FXML
    private Text right3stock;


    /**
     * Contrôleur pour la gestion des stocks.
     * Ce contrôleur permet de charger les données des stocks depuis un fichier CSV et d'afficher les informations correspondantes.
     */
    public StocksController(Stage stage) {
        this.stage = stage;
    }


    /**
     * Initialise le contrôleur après le chargement de son élément racine.
     *
     * @param url L'emplacement utilisé pour résoudre les chemins relatifs pour l'objet racine, ou null si l'emplacement n'est pas connu.
     * @param resourceBundle Les ressources utilisées pour localiser l'objet racine, ou null si les ressources ne sont pas connues.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            this.montrerStock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Charge les données des stocks depuis un fichier CSV et les affiche dans l'interface utilisateur.
     *
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    public void montrerStock() throws IOException {
        Reader in = null;
        try {
            in = new FileReader("src/main/resources/Elements.csv");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Iterable<CSVRecord> recordStock = null;
        try {
            CSVFormat format = CSVFormat.Builder.create().setDelimiter(";").build();
            recordStock = format.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ObservableList<String> elemlist1 = FXCollections.observableArrayList();
        for (CSVRecord record : recordStock) {
            String elem = record.get("Nom");
            elemlist1.add(elem);
        }

        boxElements.setItems(elemlist1);

        String selection = boxElements.getSelectedItem();
        Reader myreader = null;
        try {
            myreader = new FileReader("src/main/resources/Elements.csv");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Iterable<CSVRecord> records = null;
        try {
            CSVFormat format = CSVFormat.Builder.create().setDelimiter(";").build();
            records = format.withFirstRecordAsHeader().parse(myreader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (CSVRecord record : records) {
            if(Objects.equals(record.get("Nom"), selection)) {
                right1stock.setText(record.get("Code"));
                right2stock.setText(record.get("Quantite"));
                right3stock.setText(record.get("Unite"));
            }
        }
        try {
            myreader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}