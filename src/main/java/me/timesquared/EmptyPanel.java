package me.timesquared;

import javax.swing.*;
import java.awt.*;

public class EmptyPanel extends JPanel {
	public EmptyPanel() {
		setMinimumSize(new Dimension(5, 5));
	}
	
	public EmptyPanel(int width, int height) {
		setMinimumSize(new Dimension(width, height));
	}
}
