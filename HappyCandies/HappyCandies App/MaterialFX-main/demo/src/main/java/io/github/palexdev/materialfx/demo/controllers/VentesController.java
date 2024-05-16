package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVFormat;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVParser;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVRecord;
import io.github.palexdev.materialfx.demo.apache.commons.csv.CSVPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static java.lang.Thread.sleep;

/**
 * Contrôleur pour la gestion des ventes.
 * Ce contrôleur permet de gérer les opérations de vente, y compris la sélection des produits,
 * la mise à jour des quantités et des prix, et l'enregistrement des transactions.
 */
public class VentesController implements Initializable {

    private final Stage stage;
    private int quantite = 0;
    private float total = 0;
    private float prixUnite = 0;

    private String maNotif;

    @FXML
    private Text textFieldQte;

    @FXML
    private Text monPrixVente;

    @FXML
    private Text notificationVente;

    @FXML
    private Text totalPrixVente;

    @FXML
    private MFXComboBox<String> Produits;

    private Iterable<CSVRecord> mesPrix = null;
    private Iterable<CSVRecord> mesElems = null;

    private final ObservableList<String> elemlist;
    private final ObservableList<String> codelist;

    private boolean produitDispo;


    /**
     * Constructeur du contrôleur VentesController.
     * Initialise les listes de produits et de prix à partir de fichiers CSV.
     *
     * @param stage La fenêtre principale de l'application.
     */
    public VentesController(Stage stage) {
        this.stage = stage;
        Reader prixR = null;
        Reader elems = null;
        this.elemlist = FXCollections.observableArrayList();
        this.codelist = FXCollections.observableArrayList();
        try {
            prixR = new FileReader("src/main/resources/Prix.csv");
            elems = new FileReader("src/main/resources/Elements.csv");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            CSVFormat format = CSVFormat.Builder.create().setDelimiter(";").build();
            this.mesPrix = format.withFirstRecordAsHeader().parse(prixR);
            this.mesElems = format.withFirstRecordAsHeader().parse(elems);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Initialise le contrôleur après le chargement de son élément racine.
     * Remplit la liste des produits disponibles pour la vente.
     *
     * @param url L'emplacement utilisé pour résoudre les chemins relatifs pour l'objet racine, ou null si l'emplacement n'est pas connu.
     * @param resourceBundle Les ressources utilisées pour localiser l'objet racine, ou null si les ressources ne sont pas connues.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (CSVRecord element : this.mesElems) {
            String elem = element.get("Code");
            if (Objects.equals(elem.substring(0, 1), "B")) {
                this.elemlist.add(element.get("Nom"));
                this.codelist.add(elem);

            }
        }
        Produits.setItems(elemlist);
        textFieldQte.setText(String.valueOf(this.quantite));
    }


    /**
     * Augmente la quantité du produit sélectionné.
     * Met à jour le champ de texte et le prix total.
     */

    public void augmenterV() {
        this.total = Float.parseFloat(this.monPrixVente.getText());
        if (quantite < 100) {
            quantite++;
            textFieldQte.setText(String.valueOf(quantite));
            this.total = this.total * this.quantite;
            this.totalPrixVente.setText(String.valueOf(this.total));
        }
    }

    /**
     * Diminue la quantité du produit sélectionné.
     * Met à jour le champ de texte et le prix total.
     */

    public void diminuerV() {
        this.total = Float.parseFloat(this.monPrixVente.getText());
        if (quantite > 0) {
            quantite--;
            textFieldQte.setText(String.valueOf(quantite));
            this.total = this.total * this.quantite;
            this.totalPrixVente.setText(String.valueOf(this.total));

        }
    }


    /**
     * Affiche le prix unitaire du produit sélectionné.
     * Réinitialise la quantité à zéro et met à jour le prix total.
     */

    public void afficherPrixV() {
        // Champ de texte pour afficher la quantité actuelle
        this.quantite = 0;
        textFieldQte.setText(String.valueOf(quantite));

        this.total = 0;
        String monElem = Produits.getSelectedItem();
        for (int i = 0; i < this.elemlist.size(); i++) {
            if (this.elemlist.get(i).equals(monElem)) {
                for (CSVRecord elemprix : this.mesPrix) {
                    if (this.codelist.get(i).equals(elemprix.get("Code"))) {
                        this.prixUnite = Float.parseFloat(elemprix.get("Prix Vente"));
                        this.total = this.prixUnite;
                        break;
                    }

                }
                break;
            }
        }
        this.monPrixVente.setText(String.valueOf(this.prixUnite));
        this.total = this.total * this.quantite;
        this.totalPrixVente.setText(String.valueOf(total));


    }

    /**
     * Effectue la vente du produit sélectionné.
     * Met à jour les stocks et le solde de l'usine, et affiche une notification de vente.
     *
     * @throws IOException Si une erreur se produit lors de la lecture ou de l'écriture des fichiers CSV.
     */
    public void vendre() throws IOException {

        this.maNotif = "Produits vendus! :)";
        this.produitDispo = true;


        // récupérer les stocks et les mettre à jour
        String inputFile = "src/main/resources/Elements.csv";
        String nomModif = this.Produits.getSelectedItem();
        int qteVendue = Integer.parseInt(this.textFieldQte.getText());

        CSVFormat myFormat = CSVFormat.DEFAULT.builder().setDelimiter(";").build();

        Reader myreader = new FileReader(inputFile);
        Iterable<CSVRecord> myrecords = null;
        try {
            myrecords = myFormat.withFirstRecordAsHeader().parse(myreader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        List<String[]> mynewcsv = new ArrayList<>();
        for (CSVRecord record : myrecords) {
            if (record.get("Nom").equals(nomModif)) {
                String codeElem = record.get("Code");
                String qteActuelle = record.get("Quantite");
                String uniteElem = record.get("Unite");
                mynewcsv.add(new String[]{codeElem, nomModif, String.valueOf(Integer.parseInt(qteActuelle) - qteVendue), uniteElem});
                if((Integer.parseInt(qteActuelle) - qteVendue)<0){
                    this.maNotif = "Stocks insuffisants! :(";
                    this.produitDispo = false;
                }

            }
            else {
                mynewcsv.add(new String[]{record.get("Code"), record.get("Nom"), record.get("Quantite"), record.get("Unite")});
            }
        }

        if(produitDispo) {
            Writer mywriter = new FileWriter(inputFile, false);
            CSVPrinter printer = new CSVPrinter(mywriter, CSVFormat.DEFAULT.withHeader("Code", "Nom", "Quantite", "Unite").withDelimiter(';'));
            printer.printRecords(mynewcsv);
            printer.flush();
            printer.close();

            // récupérer le solde de l'usine pour le mettre à jour
            String soldeUsine;
            try (Reader reader1 = new FileReader("src/main/resources/MonUsine.csv");
                 CSVParser csvParser1 = new CSVParser(reader1, CSVFormat.DEFAULT)) {

                CSVRecord csvRecord = csvParser1.getRecords().get(0);
                soldeUsine = csvRecord.get(0);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            float nouveauMontant = Float.parseFloat(soldeUsine) + Float.parseFloat(this.totalPrixVente.getText());

            // Écrire le nouveau montant dans le fichier CSV
            try (Writer writer1 = new FileWriter("src/main/resources/MonUsine.csv", StandardCharsets.UTF_8);
                 CSVPrinter csvPrinter1 = new CSVPrinter(writer1, CSVFormat.DEFAULT)) {

                csvPrinter1.printRecord(nouveauMontant);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Écrire la nouvelle vente dans le fichier historique
        try (Writer writer2 = new FileWriter("src/main/resources/Historique.csv", StandardCharsets.UTF_8,true);
             CSVPrinter csvPrinter2 = new CSVPrinter(writer2, CSVFormat.DEFAULT.withDelimiter(';'))) {

            LocalDate dateActuelle = LocalDate.now();

            // Définir un formatteur pour la date
            DateTimeFormatter formatteur = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Formater la date actuelle
            String dateFormatee = dateActuelle.format(formatteur);
            csvPrinter2.printRecord(dateFormatee,"Vente",this.Produits.getSelectedItem(),this.totalPrixVente.getText());
            writer2.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.notificationVente.setText(maNotif);
        // Créer un nouveau thread pour attendre avant de vider le texte
        Thread thread = new Thread(() -> {
            try {
                // Attendre pendant 5 secondes
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Effacer le texte après l'attente
            Platform.runLater(() -> {
                this.notificationVente.setText("");
            });
        });

// Démarrer le thread
        thread.start();



    }


}

