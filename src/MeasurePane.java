import java.util.ArrayList;
import java.util.List;

import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import jm.JMC;
import jm.music.data.Part;

public class MeasurePane extends Pane implements JMC {

	int num, den;

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

	public MeasurePane(int num, int den, double numMeasures) {
		super();
		this.num = num;
		this.den = den;
		measures = numMeasures;

		populate();

		System.out.println(num + "/" + den);
	}

	public void populate() {
		double currentX = 0;
		for (int i = 1; i <= measures+1; i++) {
			Line l = new Line(currentX, 0, currentX, 10);
			Text t = new Text();
			t.setText(i + "");
			t.setY(10);
			t.setX(currentX + 1);
			currentX += 20;
			this.getChildren().addAll(l, t);
			for (int j = 1; j < num; j++) {
				Line g = new Line(currentX, 0, currentX, 5);
				currentX += 20;
				this.getChildren().add(g);
			}
		}

	}

	public void clear() {
		this.getChildren().clear();
	}
}
