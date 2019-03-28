package machinelearning.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class EntropyClassifier2 {

	private static final String VALUE_OUTLOOK_SUNNY = "sunny";
	private static final String VALUE_OUTLOOK_OVERCAST = "overcast";
	private static final String VALUE_OUTLOOK_RAINY = "rainy";
	
	private static final String VALUE_TEMP_HOT = "hot";
	private static final String VALUE_TEMP_MILD = "mild";
	private static final String VALUE_TEMP_COOL = "cool";
	
	private static final String VALUE_HUMIDITY_HIGH = "high";
	private static final String VALUE_HUMIDITY_NORMAL = "normal";
	
	private static final String VALUE_WINDY_TRUE = "true";
	private static final String VALUE_WINDY_FALSE = "false";
	
	private static final String VALUE_PLAY_YES = "Yes";
	private static final String VALUE_PLAY_NO = "No";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// defining data format

		ArrayList<String> outlookVals = new ArrayList<String>();
		outlookVals.add(VALUE_OUTLOOK_SUNNY);
		outlookVals.add(VALUE_OUTLOOK_OVERCAST);
		outlookVals.add(VALUE_OUTLOOK_RAINY);
		
		ArrayList<String> tempVals = new ArrayList<String>();
		tempVals.add(VALUE_TEMP_HOT);
		tempVals.add(VALUE_TEMP_MILD);
		tempVals.add(VALUE_TEMP_COOL);
		
		ArrayList<String> humidityVals = new ArrayList<String>();
		humidityVals.add(VALUE_HUMIDITY_HIGH);
		humidityVals.add(VALUE_HUMIDITY_NORMAL);
		
		ArrayList<String> windyVals = new ArrayList<String>();
		windyVals.add(VALUE_WINDY_TRUE);
		windyVals.add(VALUE_WINDY_FALSE);
		
		ArrayList<String> playVals = new ArrayList<String>();
		playVals.add(VALUE_PLAY_YES);
		playVals.add(VALUE_PLAY_NO);
		

		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		Attribute attr1 = new Attribute("outlook", outlookVals);
		Attribute attr2 = new Attribute("temperaure", tempVals);
		Attribute attr3 = new Attribute("humidity", humidityVals);
		Attribute attr4 = new Attribute("windy", windyVals);
		Attribute attr5 = new Attribute("play", playVals);
		attrs.add(attr1);
		attrs.add(attr2);
		attrs.add(attr3);
		attrs.add(attr4);
		attrs.add(attr5);
		
		
		// training

		List<Instance> training = generateTrainingData(attrs);

		Entropy entropy = new Entropy(attrs, attr5);

		CARTNode.Strategy.Builder<Entropy> builder = new CARTNode.Strategy.Builder<Entropy>(entropy);

		CARTNode<Entropy> root = builder.build(training);

		System.out.println(root.toAll());
	}

	public static List<Instance> generateTrainingData(ArrayList<Attribute> attrs) {

		Instances training = new Instances("TRAINING", attrs, 14);
		
		// AnswerS:
		// outlook
		//   +-<sunny>    - humidity
		//						+-<high> - no
		//						+-<normal> - yes
		//   +-<overcast> - yes
		//   +-<rainy>    - windy
		//						+-<false> - yes
		//						+-<true>  - no
		
		Instance data1 = new DenseInstance(5);	
		data1.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data1.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data1.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data1.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data1.setValue(attrs.get(4), VALUE_PLAY_NO);
		training.add(data1);
		
		Instance data2 = new DenseInstance(5);	
		data2.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data2.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data2.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data2.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data2.setValue(attrs.get(4), VALUE_PLAY_NO);
		training.add(data2);
		
		Instance data3 = new DenseInstance(5);	
		data3.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data3.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data3.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data3.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data3.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data3);
		
		Instance data4 = new DenseInstance(5);	
		data4.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data4.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data4.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data4.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data4.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data4);
		
		Instance data5 = new DenseInstance(5);	
		data5.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data5.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data5.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data5.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data5.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data5);
		
		Instance data6 = new DenseInstance(5);	
		data6.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data6.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data6.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data6.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data6.setValue(attrs.get(4), VALUE_PLAY_NO);
		training.add(data6);
		
		Instance data7 = new DenseInstance(5);	
		data7.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data7.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data7.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data7.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data7.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data7);
		
		Instance data8 = new DenseInstance(5);	
		data8.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data8.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data8.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data8.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data8.setValue(attrs.get(4), VALUE_PLAY_NO);
		training.add(data8);
		
		Instance data9 = new DenseInstance(5);	
		data9.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data9.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data9.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data9.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data9.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data9);
		
		Instance data10 = new DenseInstance(5);	
		data10.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data10.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data10.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data10.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data10.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data10);
		
		Instance data11 = new DenseInstance(5);	
		data11.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data11.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data11.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data11.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data11.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data11);
		
		Instance data12 = new DenseInstance(5);	
		data12.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data12.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data12.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data12.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data12.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data12);
		
		Instance data13 = new DenseInstance(5);	
		data13.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data13.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data13.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data13.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data13.setValue(attrs.get(4), VALUE_PLAY_YES);
		training.add(data13);
		
		Instance data14 = new DenseInstance(5);	
		data14.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data14.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data14.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data14.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data14.setValue(attrs.get(4), VALUE_PLAY_NO);
		training.add(data14);

		return training;
	}

	private static class Entropy extends CARTNode.Strategy {

		public Entropy(List<Attribute> attrs, Attribute cls) {
			super(attrs, cls);
		}

		@Override
		public CARTNode<Entropy> calculate(double last, List<Attribute> attrs, List<Instance> instances) {

			CARTNode.Strategy.Builder<Entropy> builder = new CARTNode.Strategy.Builder<Entropy>(this);
			DoubleAdder max = new DoubleAdder();
			max.add(Double.MIN_VALUE);

			PlaceHolder<CARTNode<Entropy>> holder = new PlaceHolder<CARTNode<Entropy>>();

			attrs.stream().forEach(p -> {

				CARTNode<Entropy> node = builder.test(p, this.definition().get(p), instances);
				double score = node.score();
				
				if (max.doubleValue() < score) {
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
			return entropy(node.attr(), node.inputs());
		}

		@Override
		public List<Instance> filter(boolean binary, CARTNode<?> node, Object value, List<Instance> instances) {

			return instances.stream().filter(p -> value.equals(p.stringValue(node.attr())))
					.collect(Collectors.toList());
		}
		
		private double gain(Attribute attr, List<Instance> instances) {
			
			Map<String, List<Instance>> profitCategory = this.spreads(cls, instances);
			DoubleAdder info = new DoubleAdder();

			List<Integer> terms = new ArrayList<Integer>();

			profitCategory.entrySet().stream().forEach(p -> {
				terms.add(p.getValue().size());
			});

			terms.stream().forEach(p -> {
				if (p.doubleValue() != 0)
					info.add(-1 * p.doubleValue() / instances.size()
							* Math.log(p.doubleValue() / instances.size()) / Math.log(2));
			});
				
			return info.doubleValue();
		}

		private double entropy(Attribute attr, List<Instance> instances) {

			Map<String, List<Instance>> profitCategory = this.spreads(cls, instances);
			double gain = gain(attr, instances);


			DoubleAdder entropies = new DoubleAdder();

			this.definition().get(attr).stream().forEach(v -> {

				DoubleAdder entropy = new DoubleAdder();
				DoubleAdder subtotal = new DoubleAdder();
				List<Integer> terms = new ArrayList<Integer>();

				profitCategory.entrySet().stream().forEach(c -> {

					List<Instance> list = this.spreads(attr, c.getValue()).get(v);
					int size = 0;
					if (list != null)
						size = list.size();

					subtotal.add(size);
					terms.add(size);
				});
				
				terms.stream().forEach(e -> {
					if (e != 0) {
						entropy.add(-1 * e.doubleValue() / subtotal.doubleValue()
								* Math.log(e.doubleValue() / subtotal.doubleValue()) / Math.log(2));
					}
				});

				if (entropy.doubleValue() != 0 && instances.size() != 0)
					entropies.add(entropy.doubleValue() * subtotal.doubleValue() / instances.size());
			});

			return gain - entropies.doubleValue();
		}

		private Map<String, List<Instance>> spreads(Attribute attr, List<Instance> instances) {

			return instances.stream().collect(Collectors.groupingBy(p -> p.stringValue(attr)));
		}
	}
}
