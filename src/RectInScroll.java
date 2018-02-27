import java.awt.Color;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class RectInScroll extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
        root.getChildren().addAll(infrastructurePane());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public ScrollPane infrastructurePane() {

        final FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 5, 5, 5));
        flow.setVgap(5);
        flow.setHgap(5);
        flow.setAlignment(Pos.CENTER);

        final ScrollPane scroll = new ScrollPane();

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Horizontal scroll bar
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Vertical scroll bar
        scroll.setContent(flow);
        scroll.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                flow.setPrefWidth(bounds.getWidth());
                flow.setPrefHeight(bounds.getHeight());
            }
        });

        //flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: yellow;");

        for (int i = 0; i < 28; i++) {
            flow.getChildren().add(generateRectangle());
        }



        return scroll;
    }

    public Rectangle generateRectangle() {

        Rectangle rect2 = new Rectangle(10, 10, 10, 10);
        rect2.setId("app");
        rect2.setArcHeight(8);
        rect2.setArcWidth(8);
        //rect2.setX(10);
        //rect2.setY(160);
        rect2.setStrokeWidth(1);
        rect2.setWidth(220);
        rect2.setHeight(180);
        return rect2;
    }

    public static void main(String[] args) {
        launch(args);
    }
}