import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import jm.JMC;
import jm.music.data.Part;

public class MeasurePane extends Pane implements JMC {

	int num, den;
	MeasurePane myInstance;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getDen() {
		return den;
	}

	public void setDen(int den) {
		this.den = den;
	}

	public double getMeasures() {
		return measures;
	}

	public void setMeasures(double measures) {
		this.measures = measures;
	}

	double measures;
	MouseGestures mg;

	public MeasurePane(int num, int den, double numMeasures) {
		super();
		this.num = num;
		this.den = den;
		measures = numMeasures;
		mg = new MouseGestures();
		myInstance = this;

		mg.makeClickable(this);
		populate();

		System.out.println(num + "/" + den);
	}

	/*
	 * This populates the measures with numbers and the lines which are actually
	 * rectangles of width 2
	 */
	public void populate() {

		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						double currentX = 0;
						for (int i = 1; i <= (int)measures; i++) {
							Rectangle l = new Rectangle(currentX, 0, 2, 10);
							l.setFill(Color.RED);
							Text t = new Text();
							t.setFill(Color.WHITE);
							mg.makeClickable(l);
							t.setText(i + "");
							t.setY(10);
							t.setX(currentX + 3);
							currentX += 80 / myInstance.den;
							myInstance.getChildren().addAll(l, t);
							for (int j = 1; j < num; j++) {
								Rectangle g = new Rectangle(currentX, 0, 2, 5);
								g.setFill(Color.WHITE);
								currentX += 20;
								myInstance.getChildren().add(g);
							}

						}
					}
				});

				return null;
			}
		};

		BasicOpsTest.executor.execute(task);

	}

	public void clear() {
		this.getChildren().clear();
	}

	private class MouseGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;

		public void makeClickable(Node node) {
			node.setOnMousePressed(circleOnMousePressedEventHandler);
			node.setOnMouseDragged(circleOnMouseDraggedEventHandler);
		}

		EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				if (t.getButton() == MouseButton.PRIMARY) {
					orgSceneX = t.getSceneX();
					orgSceneY = t.getSceneY();

					if (t.getSource() instanceof MeasurePane) {

						Node p = ((Node) (t.getSource()));

						double x = p.getLayoutX();
						double y = p.getTranslateY();

						System.out.println(((MeasurePane) p).getLayoutX()
								+ "this is your spot in the music at location " + t.getX());

						BasicOpsTest.setSequencer((long) t.getX());

					} else {

						// Node p = ((Node) (t.getSource()));

						// orgTranslateX = p.getTranslateX();
						// orgTranslateY = p.getTranslateY();

					}
				}

				else if (t.getButton() == MouseButton.SECONDARY) {

				}

			}

		};

		EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				double offsetX = t.getSceneX() - orgSceneX;
				double offsetY = t.getSceneY() - orgSceneY;

				// double newTranslateX = orgTranslateX + offsetX;
				// double newTranslateY = orgTranslateY + offsetY;

				if (t.getSource() instanceof MIDINoteBar) {

					// MIDINoteBar p = ((MIDINoteBar) (t.getSource()));
					//
					// currentNote = p;
					//
					// // p.setX(newTranslateX);
					// // p.setY(newTranslateY);
					// System.out.println(p.noteInfo());

				} else {

					Node p = ((Node) (t.getSource()));
				}

			}
		};

	}
}
