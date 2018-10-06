package edu.ung.phys;

import processing.core.PApplet;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File; 

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

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
  public BasicNetwork network;


  public void settings() {
    size(28*20, 28*20);
  }


  public void setup() {
    frRate = 60;
    frameRate(frameRate);
    pixels = new ArrayList<Integer>(Collections.nCopies(28*28, 0));

    network = new BasicNetwork();
    network.addLayer(new BasicLayer(null, true, 28*28)); // input layer
    network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 20)); // hidden layer with 20 neurons
    network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 20)); // hidden layer with 20 neurons
    network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 10)); // output layer, answer can be 0-9
    network.getStructure().finalizeStructure();
    network.reset();

    try {
      trainNetwork();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }


  public void trainNetwork() throws IOException {
    MLDataSet trainingSet = new BasicMLDataSet();
    BufferedReader dataReader = createReader("mnist_10k.txt");
    String currentLine = dataReader.readLine();
    while(currentLine != null) {
      String[] currentLineParts = currentLine.split(",");
      BasicMLData inputData = getInputData(currentLineParts);
      BasicMLData idealOutputData = getIdealOutputData(Integer.parseInt(currentLineParts[0]));
      trainingSet.add(inputData, idealOutputData);
      currentLine = dataReader.readLine();
    }
    dataReader.close();

    final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
    int epoch = 1;
    while(train.getError() > 0.035 || epoch == 1) {
      train.iteration();
      System.out.println(epoch + " " + train.getError());
      epoch++;
    }
    train.finishTraining();
    System.out.println("");
  }


  public BasicMLData getIdealOutputData(int correctDigit) {
    double[] idealOutput = new double[10];
    for(int k = 0; k < 10; k++) idealOutput[k] = 0.0;
    idealOutput[correctDigit] = 1.0;
    return new BasicMLData(idealOutput);
  }


  public BasicMLData getInputData(String[] csvValues) {
    double[] inputInts = new double[csvValues.length - 1];
    for(int k = 1; k < csvValues.length; k++) {
      inputInts[k-1] = Double.parseDouble(csvValues[k]);
    }
    return new BasicMLData(inputInts);
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

    fill(75, 225, 75);
    ellipse(50, height - 50, 40, 40);
    fill(0, 0, 255);
    ellipse(100, height - 50, 40, 40);
    fill(225, 75, 75);
    ellipse(150, height - 50, 40, 40);
  }


  public void mouseDragged() {
    int ix = mouseX/(width/28);
    int iy = mouseY/(height/28);
    int i = iy*28 + ix;
    pixels.set(i, 255);
  }


  public void mouseClicked() {
    if(Math.sqrt(Math.pow(mouseX - 50, 2.0) + Math.pow(mouseY - (height-50), 2.0)) < 20) {
      System.out.println("green button clicked");
      queryNetwork();
    }

    if(Math.sqrt(Math.pow(mouseX - 100, 2.0) + Math.pow(mouseY - (height-50), 2.0)) < 20) {
      System.out.println("blue button clicked - pixels reset");
      System.out.println("");
      for(int k = 0; k < 28*28; k++) pixels.set(k, 0);
    }

    if(Math.sqrt(Math.pow(mouseX - 150, 2.0) + Math.pow(mouseY - (height-50), 2.0)) < 20) {
      System.out.print("red button clicked - random example loaded ");
      try {
        loadRandom();
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }
  }


  public void loadRandom() throws IOException {
    Random r = new Random();
    Scanner sc = new Scanner(new File("data/mnist_10k.txt"));

    String line = sc.nextLine();
    for(int k = 0; k < r.nextInt(1000); k++) {
      line = sc.nextLine();
    }

    int[] pxarray = Arrays.asList(line.split(","))
                      .stream()
                      .map(String::trim)
                      .mapToInt(Integer::parseInt).toArray();

    System.out.println(pxarray[0]);

    for(int k = 0; k < 28*28; k++) {
      pixels.set(k, pxarray[k+1]);
    }
  }


  public void queryNetwork() {
    double[] pixelArray = new double[28*28];
    for(int k = 0; k < 28*28; k++) {
      pixelArray[k] = (double) pixels.get(k);
    }
    BasicMLData queryData = new BasicMLData(pixelArray);
    final MLData networkOutputData = network.compute(queryData);
    ArrayList<Double> networkOutput = new ArrayList<>();
    for(int k = 0; k < 10; k++) networkOutput.add(networkOutputData.getData(k));
    for(int k = 0; k < 10; k++) {
      System.out.print(k + " " + networkOutput.get(k) + " ");
      if(networkOutput.get(k) == Collections.max(networkOutput)) System.out.print("<-----");
      System.out.println("");
    }
    System.out.println("");
  }

}
