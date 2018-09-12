package edu.ung.phys;

import processing.core.PApplet;

import java.util.Collections;
import java.util.ArrayList;

/**
 * @author naharrison
 * handwritten characters are drawn on a 28x28 grid since
 * the mnist training data uses that convention. (28*28 = 784)
 */
public class HandwritingReader extends PApplet {

	public static void main(String[] args) {
		PApplet.main("edu.ung.phys.HandwritingReader");
	}


  public int frRate;
  public ArrayList<Integer> pixels;

	
	public void settings() {
		size(28*20, 28*20);
	}


	public void setup() {
		frRate = 60;
		frameRate(frameRate);
    pixels = new ArrayList<Integer>(Collections.nCopies(28*28, 0));
	}

	
	public void draw() {
    background(200);

    int ix = 0;
    int iy = 0;
    for(int k = 0; k < 28*28; k++) {
      fill(pixels.get(k));
      stroke(pixels.get(k));
      rect(ix*(width/28), iy*(height/28), width/28, height/28);
      ix++;
      if(ix % 28 == 0) {
        ix = 0;
        iy++;
      }
    }

    stroke(255);
    strokeWeight(2);
    for(int k = 1; k < 28; k++) {
      line(0, k*height/28, width, k*height/28);
      line(k*width/28, 0, k*width/28, height);
    }
	}


	public void mouseDragged() {
    int ix = mouseX/(width/28);
    int iy = mouseY/(height/28);
    int i = iy*28 + ix;
    pixels.set(i, 255);
	}

}
