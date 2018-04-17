import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ZoomableScrollPane extends ScrollPane {
	private double scaleValue = 0.7;
	private double zoomIntensity = 0.02;
	private Node target;
	private Node zoomNode;
	private ZoomableScrollPane myInstance;

	public ZoomableScrollPane(Node target) {
		super();
		this.target = target;
		this.zoomNode = new Group(target);
		myInstance = this;

		setContent(outerNode(zoomNode));

		// setPannable(true);
		setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		// setFitToHeight(true); //center
		// setFitToWidth(true); //center

		this.getStylesheets().add("application.css");
		updateScale();
	}

	private Node outerNode(Node node) {
		Node outerNode = centeredNode(node);
		outerNode.setOnScroll(e -> {
			e.consume();
			onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
		});
		return outerNode;
	}

	private Node centeredNode(Node node) {
		VBox vBox = new VBox(node);
		vBox.setAlignment(Pos.CENTER);
		return vBox;
	}

	private void updateScale() {
		target.setScaleX(scaleValue);
		target.setScaleY(scaleValue);
	}

	private void onScroll(double wheelDelta, Point2D mousePoint) {

		// Runnable lr = () -> {
		//
		// double zoomFactor = Math.exp(wheelDelta * zoomIntensity);
		//
		// Bounds innerBounds = zoomNode.getLayoutBounds();
		// Bounds viewportBounds = getViewportBounds();
		//
		// // calculate pixel offsets from [0, 1] range
		// double valX = myInstance.getHvalue() * (innerBounds.getWidth() -
		// viewportBounds.getWidth());
		// double valY = myInstance.getVvalue() * (innerBounds.getHeight() -
		// viewportBounds.getHeight());
		//
		// scaleValue = scaleValue * zoomFactor;
		// updateScale();
		// myInstance.layout(); // refresh ScrollPane scroll positions & target bounds
		//
		// // convert target coordinates to zoomTarget coordinates
		// Point2D posInZoomTarget =
		// target.parentToLocal(zoomNode.parentToLocal(mousePoint));
		//
		// // calculate adjustment of scroll position (pixels)
		// Point2D adjustment = target.getLocalToParentTransform()
		// .deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));
		//
		// // convert back to [0, 1] range
		// // (too large/small values are automatically corrected by ScrollPane)
		// Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
		// myInstance.setHvalue(
		// (valX + adjustment.getX()) / (updatedInnerBounds.getWidth() -
		// viewportBounds.getWidth()));
		// myInstance.setVvalue(
		// (valY + adjustment.getY()) / (updatedInnerBounds.getHeight() -
		// viewportBounds.getHeight()));
		//
		// };

		// BasicOpsTest.executor.execute(lr);

		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

						Bounds innerBounds = zoomNode.getLayoutBounds();
						Bounds viewportBounds = getViewportBounds();

						// calculate pixel offsets from [0, 1] range
						double valX = myInstance.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
						double valY = myInstance.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

						scaleValue = scaleValue * zoomFactor;
						updateScale();
						myInstance.layout(); // refresh ScrollPane scroll positions & target bounds

						// convert target coordinates to zoomTarget coordinates
						Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

						// calculate adjustment of scroll position (pixels)
						Point2D adjustment = target.getLocalToParentTransform()
								.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

						// convert back to [0, 1] range
						// (too large/small values are automatically corrected by ScrollPane)
						Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
						myInstance.setHvalue((valX + adjustment.getX())
								/ (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
						myInstance.setVvalue((valY + adjustment.getY())
								/ (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));

					}
				});

				return null;
			}
		};

		BasicOpsTest.executor.execute(task);
	};

}