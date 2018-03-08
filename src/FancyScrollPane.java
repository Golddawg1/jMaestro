import java.awt.Panel;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class FancyScrollPane {

	FancyScrollPane(int tempo, double totalTime, ScrollPane scroller) {

		double x = scroller.getTranslateX();
		double measures = totalTime / tempo;
		ScrollPane scrollPane = new ScrollPane();
		Pane content = new Pane();
		scrollPane.setContent(content);

		Panel immovableObject = new Panel();
		content.getChildren().add(immovableObject);

		// here we bind circle Y position
		immovableObject.layoutYProperty().bind(
				// to vertical scroll shift (which ranges from 0 to 1)
				scrollPane.vvalueProperty()
						// multiplied by (scrollableAreaHeight - visibleViewportHeight)
						.multiply(content.heightProperty().subtract(new ScrollPaneViewPortHeightBinding(scrollPane))));
	}

	// we need this class because Bounds object doesn't support binding
	private static class ScrollPaneViewPortHeightBinding extends DoubleBinding {

		private final ScrollPane root;

		public ScrollPaneViewPortHeightBinding(ScrollPane root) {
			this.root = root;
			super.bind(root.viewportBoundsProperty());
		}

		@Override
		protected double computeValue() {
			return root.getViewportBounds().getHeight();
		}
	}

	public static void main(String[] args) {
		launch();
	}
}