import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class MouseGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;

		public void makeDraggable(Node node) {
			node.setOnMousePressed(circleOnMousePressedEventHandler);
			node.setOnMouseDragged(circleOnMouseDraggedEventHandler);
		}

		EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				orgSceneX = t.getSceneX();
				orgSceneY = t.getSceneY();

				if (t.getSource() instanceof Rectangle) {

					Rectangle p = ((Rectangle) (t.getSource()));

					orgTranslateX = p.getX();
					orgTranslateY = p.getY();

				} else {

					Node p = ((Node) (t.getSource()));

					orgTranslateX = p.getTranslateX();
					orgTranslateY = p.getTranslateY();

				}
			}
		};

		EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				double offsetX = t.getSceneX() - orgSceneX;
				double offsetY = t.getSceneY() - orgSceneY;

				double newTranslateX = orgTranslateX + offsetX;
				double newTranslateY = orgTranslateY + offsetY;

				if (t.getSource() instanceof Rectangle) {

					Rectangle p = ((Rectangle) (t.getSource()));

					p.setX(newTranslateX);
					//p.setY(newTranslateY);

				} else {

					Node p = ((Node) (t.getSource()));

					p.setTranslateX(newTranslateX);
					p.setTranslateY(newTranslateY);

				}

			}
		};

	}