package ch.epfl.javions.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * La classe StatusLineController du sous-paquetage gui gère la ligne d'état.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class StatusLineController {

    private static final String BORDER_PANE_CSS = "status.css";
    private final IntegerProperty aircraftCount = new SimpleIntegerProperty(0);
    private final LongProperty messageCount = new SimpleLongProperty(0);
    private BorderPane pane;

    /**
     * Constructeur par défaut de la classe StatusLineController
     * qui construit le graphe de scène.
     */
    public StatusLineController() {
        initializePane();
        setupBindings();
    }

    /**
     * Méthode appelée dans le constructeur qui initialise le panneau JavaFX
     * sur lequel la ligne d'état est affichée.
     */
    private void initializePane() {
        pane = new BorderPane();
        pane.getStyleClass().add(BORDER_PANE_CSS);
    }

    /**
     * Méthode appelée dans le constructeur, permet d'afficher de la bonne manière
     * le bon nombre d'aéronefs visible et le nombre de messages reçus.
     */
    private void setupBindings() {
        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(aircraftCountProperty().asString("Aéronefs visibles : %d"));

        Text messageCountText = new Text();
        messageCountText.textProperty().bind(messageCountProperty().asString("Messages reçus : %d"));

        pane.setLeft(aircraftCountText);
        pane.setRight(messageCountText);
    }

    /**
     * Méthode qui retourne la propriété (modifiable) contenant le nombre d'aéronefs actuellement visibles.
     *
     * @return La propriété (modifiable) contenant le nombre d'aéronefs actuellement visibles.
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCount;
    }

    /**
     * Méthode qui retourne la propriété (modifiable) contenant le nombre de
     * messages reçus depuis le début de l'exécution du programme.
     *
     * @return La propriété (modifiable) contenant le nombre de messages reçus
     *         depuis le début de l'exécution du programme.
     */
    public LongProperty messageCountProperty() {
        return messageCount;
    }

    /**
     * Méthode permettant d'ajouter un message à la propriété contenant le nombre de messages reçus
     * depuis le début de l'exécution du programme.
     */
    public void addMessage() {
        messageCount.set(messageCount.get() + 1);
    }

    /**
     * Méthode qui retourne le panneau contenant la ligne d'état.
     *
     * @return Le panneau contenant la ligne d'état.
     */
    public Pane pane() {
        return pane;
    }
}