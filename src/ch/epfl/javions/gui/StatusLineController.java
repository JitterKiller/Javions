package ch.epfl.javions.gui;

import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public final class StatusLineController {

    private static final String BORDER_PANE_CSS = "status.css";
    private final BorderPane pane;
    private final IntegerProperty aircraftCount = new SimpleIntegerProperty(0);
    private final LongProperty messageCount = new SimpleLongProperty(0);

    public StatusLineController() {
        pane = new BorderPane();
        pane.getStyleClass().add(BORDER_PANE_CSS);

        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(aircraftCountProperty().asString("Aéronefs visibles : %d"));

        Text messageCountText = new Text();
        messageCountText.textProperty().bind(messageCountProperty().asString("Messages reçus : %d"));

        pane.setLeft(aircraftCountText);
        pane.setRight(messageCountText);
    }

    public ReadOnlyIntegerProperty aircraftCountProperty() {
        return aircraftCount;
    }

    public ReadOnlyLongProperty messageCountProperty() {
        return messageCount;
    }

    public Pane pane() {
        return pane;
    }
}
