package machinelearning.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class StdDevClassifier2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// defining data format

		ArrayList<String> vals = new ArrayList<String>();
		vals.add("1");
		vals.add("0");

		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		Attribute attr1 = new Attribute("goodCirculation", vals);
		Attribute attr2 = new Attribute("chestPain", vals);
		Attribute attr3 = new Attribute("blockedArteries", vals);
		Attribute attr4 = new Attribute("heartDisease", vals);
		attrs.add(attr1);
		attrs.add(attr2);
		attrs.add(attr3);
		attrs.add(attr4);



		// training

		List<Instance> training = generateTrainingData(100, 0, attrs);

		StdDev sd = new StdDev(attrs, attr4, training.size());
		
		CARTNode.Strategy.Builder<StdDev> builder = 
				new CARTNode.Strategy.Builder<StdDev>(sd);

		CARTNode<StdDev> root = builder.build(training);

		System.out.println(root.toAll());


	}

	public static List<Instance> generateTrainingData(int size, int seed, ArrayList<Attribute> attrs) {

		Random rand = new Random(seed);

		Instances training = new Instances("TRAINING", attrs, size);

		for (int i = 0; i < size; i++) {
			Instance data = new DenseInstance(5);

			int gc = rand.nextInt() % 2 == 0 ? 0 : 1;
			int cp = rand.nextInt() % 2 == 0 ? 0 : 1;
			int ba = rand.nextInt() % 2 == 0 ? 0 : 1;

			data.setValue(attrs.get(0), String.valueOf(gc));
			data.setValue(attrs.get(1), String.valueOf(cp));
			data.setValue(attrs.get(2), String.valueOf(ba));

			double diag = rand.nextDouble() + (gc * -0.6 + cp * 0.2 + ba * 0.3);

			data.setValue(attrs.get(3), diag < 0.6 ? "0" : "1");

			training.add(data);
		}

		return training;
	}

	private static class StdDev extends CARTNode.Strategy {

		private int total = 0;

		public StdDev(List<Attribute> attrs, Attribute cls, int size) {
			super(attrs, cls);
			this.total = size;
		}

		@Override
		public CARTNode<StdDev> calculate(double last, List<Attribute> attrs, List<Instance> instances) {

			CARTNode.Strategy.Builder<StdDev> builder = 
					new CARTNode.Strategy.Builder<StdDev>(this);
			DoubleAdder max = new DoubleAdder();
			max.add(Double.MIN_VALUE);

			PlaceHolder<CARTNode<StdDev>> holder = new PlaceHolder<CARTNode<StdDev>>();

			attrs.stream().forEach(p -> {

				CARTNode<StdDev> node = builder.test(p, this.definition().get(p), instances);
				double score = node.score();
				double ratio = (double) instances.size() / this.total;
				
				if (max.doubleValue() < score && ratio > 0.3) {
					max.reset();
					max.add(score);
					holder.data(node);
				}
			});
		
			return holder.data();
		}

		@Override
		public double score(CARTNode<?> node) {
			// TODO Auto-generated method stub
			return sd(node.attr(), node.inputs());
		}
		
		private double sd(Attribute attr, List<Instance> instances) {
			
			Map<String, List<Instance>> map = spreads(attr, instances);
			
			DoubleAdder sum = new DoubleAdder();
			
			map.entrySet().stream().forEach(p -> {
				
				double [] data = p.getValue().stream().mapToDouble(
						v -> Double.valueOf(v.stringValue(cls))).toArray();
				
				if (data.length >= 0) {
					
					double ssd = Math.sqrt(StatUtils.variance(data));
			
					sum.add(ssd * data.length / instances.size());	
				}
			});
		
			double result = ssd(attr, instances) - sum.doubleValue();
			// calculating standard deviation reduction	
			if (result < 0)
				result = 0.00001;
			
			return result;
		}
		
		private double ssd(Attribute attr, List<Instance> instances) {

			// calculating the standard deviation before the splits
			double [] data = instances.stream().mapToDouble(
						p -> Double.valueOf(p.stringValue(cls))).toArray();
			
			if (data.length <= 0)
				return 0;
							
			return Math.sqrt(StatUtils.variance(data));
		}

		private Map<String, List<Instance>> spreads(Attribute attr, List<Instance> instances) {

			return instances.stream().collect(Collectors.groupingBy(p -> p.stringValue(attr)));
		}
	}
}
