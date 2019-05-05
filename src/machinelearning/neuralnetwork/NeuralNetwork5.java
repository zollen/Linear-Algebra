package machinelearning.neuralnetwork;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.ImmutablePair;

public class NeuralNetwork5 {
	
	private static DecimalFormat ff = new DecimalFormat("0.000");
	
	private static final Random rand = new Random(0);
	
	// Autoencoder
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub	
		System.out.println("Building model....");
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(0)
				.weightInit(WeightInit.XAVIER)
				.updater(new AdaGrad(0.05))
				.activation(Activation.RELU)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.l2(0.0001)
				.list()
				.layer(0, new DenseLayer.Builder().nIn(784).nOut(250).build())
				.layer(1, new DenseLayer.Builder().nIn(250).nOut(10).build())
				.layer(2, new DenseLayer.Builder().nIn(10).nOut(250).build())
				.layer(3, new OutputLayer.Builder().nIn(250).nOut(784)
								.lossFunction(LossFunctions.LossFunction.MSE).build())
				.build();
		
		
		MultiLayerNetwork network = new MultiLayerNetwork(conf);
		network.init();
		network.setListeners(new ScoreIterationListener(1));
		
		System.out.println("Downloading Data...");
		DataSetIterator itr = new MnistDataSetIterator(10000, 50000, false);
		
		
		List<INDArray> featuresTrain = new ArrayList<INDArray>();
		List<INDArray> featuresTest = new ArrayList<INDArray>();
		List<INDArray> labelsTest = new ArrayList<INDArray>();
		
		System.out.println("Preprocessing Data...");
		while (itr.hasNext()) {
			
			DataSet data = itr.next();
			
			SplitTestAndTrain samples = data.splitTestAndTrain(80, rand);
			
			DataSet training = samples.getTrain();
			DataSet testing = samples.getTest();
			
			featuresTrain.add(training.getFeatures());
			featuresTest.add(testing.getFeatures());
			
			labelsTest.add(Nd4j.argMax(testing.getLabels(), 1));
		}
		
		System.out.println("Begin Unsupervised Training...");
		for (int nEpochs = 0; nEpochs < 30; nEpochs++) {
			
			for (int i = 0; i < featuresTrain.size(); i++) {
				
				INDArray data = featuresTrain.get(i);
				network.fit(data, data);
			}
			
			System.out.println("  --> Epoch " + nEpochs + " complete");
		}
		
		
	
		System.out.println("Begin Testing...");
		
		Map<Integer, List<ImmutablePair<Double, INDArray>>> map = 
				new HashMap<Integer, List<ImmutablePair<Double, INDArray>>>();
		
		for (int i = 0; i < 10; i++)
			map.put(i, new ArrayList<ImmutablePair<Double, INDArray>>());
		
		for (int sample = 0; sample < featuresTest.size(); sample++) {
			
			INDArray tests = featuresTest.get(sample);
			INDArray labels = labelsTest.get(sample);
			
			for (int row = 0; row < tests.rows(); row++) {
				
				INDArray test = tests.getRow(row);
				int index = labels.getInt(row);
				
				double score = network.score(new DataSet(test, test));
				
				List<ImmutablePair<Double, INDArray>> list = map.get(index);
				list.add(new ImmutablePair<Double, INDArray>(score, test));
			}
		}
		

		map.values().stream().forEach(p -> {
			
			Collections.sort(p, new Comparator<ImmutablePair<Double, INDArray>>() {

				@Override
				public int compare(ImmutablePair<Double, INDArray> o1, ImmutablePair<Double, INDArray> o2) {
					// TODO Auto-generated method stub
					return o1.getKey().compareTo(o2.getKey()) * -1;
				}	
			});
		});
		
		
		
		map.entrySet().stream().forEach(p -> {
			
			System.out.println("[" + p.getKey() + "]: " +
					p.getValue().stream().limit(5).map(k -> ff.format(k.getKey())).collect(Collectors.joining(", ")));
		});
	}
	
}
