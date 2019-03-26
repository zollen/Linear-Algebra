package machinelearning.classifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class StdDevClassifier1 {

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
		playVals.add("23");
		playVals.add("26");
		playVals.add("30");
		playVals.add("36");
		playVals.add("38");
		playVals.add("43");
		playVals.add("44");
		playVals.add("46");
		playVals.add("48");
		playVals.add("62");
		
		

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
		

		// defining data dictionary

		Map<Attribute, List<String>> definition = new LinkedHashMap<Attribute, List<String>>();
		definition.put(attr1, outlookVals);
		definition.put(attr2, tempVals);
		definition.put(attr3, humidityVals);
		definition.put(attr4, windyVals);
		definition.put(attr5, playVals);
		
		
		// training

		List<Instance> training = generateTrainingData(attrs);
			
		StdDev sd = new StdDev(definition, attr5, training.size());

		CARTNode.Strategy.Builder<StdDev> builder = 
				new CARTNode.Strategy.Builder<StdDev>(sd);

		CARTNode<StdDev> root = builder.build(training);

		System.out.println(root.toAll());

	}

	public static List<Instance> generateTrainingData(ArrayList<Attribute> attrs) {

		Instances training = new Instances("TRAINING", attrs, 14);
		
		// Answers:
		// outlook
		//   +-<sunny>    - windy
		//						+-<true>  - 26.5
		//						+-<false> - 47.7
		//   +-<overcast> - 46.3
		//   +-<rainy>    - temp
		//						+-<cool> - 38
		//						+-<hot>  - 27.5
		//						+-<mild> - 41.5
		
		Instance data1 = new DenseInstance(5);	
		data1.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data1.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data1.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data1.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data1.setValue(attrs.get(4), "26");
		training.add(data1);
		
		Instance data2 = new DenseInstance(5);	
		data2.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data2.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data2.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data2.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data2.setValue(attrs.get(4), "30");
		training.add(data2);
		
		Instance data3 = new DenseInstance(5);	
		data3.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data3.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data3.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data3.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data3.setValue(attrs.get(4), "48");
		training.add(data3);
		
		Instance data4 = new DenseInstance(5);	
		data4.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data4.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data4.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data4.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data4.setValue(attrs.get(4), "46");
		training.add(data4);
		
		Instance data5 = new DenseInstance(5);	
		data5.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data5.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data5.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data5.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data5.setValue(attrs.get(4), "62");
		training.add(data5);
		
		Instance data6 = new DenseInstance(5);	
		data6.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data6.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data6.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data6.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data6.setValue(attrs.get(4), "23");
		training.add(data6);
		
		Instance data7 = new DenseInstance(5);	
		data7.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data7.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data7.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data7.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data7.setValue(attrs.get(4), "43");
		training.add(data7);
		
		Instance data8 = new DenseInstance(5);	
		data8.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data8.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data8.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data8.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data8.setValue(attrs.get(4), "36");
		training.add(data8);
		
		Instance data9 = new DenseInstance(5);	
		data9.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data9.setValue(attrs.get(1), VALUE_TEMP_COOL);
		data9.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data9.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data9.setValue(attrs.get(4), "38");
		training.add(data9);
		
		Instance data10 = new DenseInstance(5);	
		data10.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data10.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data10.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data10.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data10.setValue(attrs.get(4), "48");
		training.add(data10);
		
		Instance data11 = new DenseInstance(5);	
		data11.setValue(attrs.get(0), VALUE_OUTLOOK_RAINY);
		data11.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data11.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data11.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data11.setValue(attrs.get(4), "48");
		training.add(data11);
		
		Instance data12 = new DenseInstance(5);	
		data12.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data12.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data12.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data12.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data12.setValue(attrs.get(4), "62");
		training.add(data12);
		
		Instance data13 = new DenseInstance(5);	
		data13.setValue(attrs.get(0), VALUE_OUTLOOK_OVERCAST);
		data13.setValue(attrs.get(1), VALUE_TEMP_HOT);
		data13.setValue(attrs.get(2), VALUE_HUMIDITY_NORMAL);
		data13.setValue(attrs.get(3), VALUE_WINDY_FALSE);
		data13.setValue(attrs.get(4), "44");
		training.add(data13);
		
		Instance data14 = new DenseInstance(5);	
		data14.setValue(attrs.get(0), VALUE_OUTLOOK_SUNNY);
		data14.setValue(attrs.get(1), VALUE_TEMP_MILD);
		data14.setValue(attrs.get(2), VALUE_HUMIDITY_HIGH);
		data14.setValue(attrs.get(3), VALUE_WINDY_TRUE);
		data14.setValue(attrs.get(4), "30");
		training.add(data14);

		return training;
	}

	private static class StdDev implements CARTNode.Strategy {

		private Map<Attribute, List<String>> definition = null;
		private List<Attribute> attrs = null;
		private Attribute cls = null;
		private int total = 0;

		public StdDev(Map<Attribute, List<String>> definition, Attribute cls, int size) {
			this.definition = definition;
			this.attrs = definition.keySet().stream().collect(Collectors.toList());
			this.total = size;

			this.cls = cls;
			this.attrs.remove(cls);
		}

		@Override
		public Map<Attribute, List<String>> definition() {
			return definition;
		}

		@Override
		public String op() {
			return " == ";
		}

		@Override
		public Attribute cls() {
			return cls;
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

		@Override
		public List<Instance> filter(boolean binary, CARTNode<?> node, String value, List<Instance> instances) {

			return instances.stream().filter(p -> value.equals(p.stringValue(node.attr())))
					.collect(Collectors.toList());
		}
		
		@SuppressWarnings("unused")
		private double cv(List<Instance> instances) {
			
			double [] data = instances.stream().mapToDouble(
					v -> Double.valueOf(v.stringValue(cls))).toArray();
			
			double mean = StatUtils.mean(data);
			double sd = StatUtils.variance(data);
			if (sd == 0)
				return 0;
			
			return Math.sqrt(sd) / mean;
		}
		
		private double sd(Attribute attr, List<Instance> instances) {
			
			Map<String, List<Instance>> map = spreads(attr, instances);
			
			DoubleAdder sum = new DoubleAdder();
			
			map.entrySet().stream().forEach(p -> {
				
				double [] data = p.getValue().stream().mapToDouble(
						v -> Double.valueOf(v.stringValue(cls))).toArray();
				
				if (data.length > 0) {
					
					double ssd = Math.sqrt(StatUtils.variance(data));
					
					sum.add(ssd * data.length / instances.size());	
				}
			});
				
			// calculating standard deviation reduction	
			double result = ssd(instances) - sum.doubleValue();
			// calculating standard deviation reduction	
			if (result < 0)
				result = 0.00001;
			
			return result;
		}
		
		private double ssd(List<Instance> instances) {

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
