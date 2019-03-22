package machinelearning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class CARTExercise2 {
	
	private static final String VALUE_COLOR_GREEN = "Green";
	private static final String VALUE_COLOR_RED = "Red";
	private static final String VALUE_COLOR_YELLOW = "Yellow";
	
	private static final String VALUE_FRUIT_APPLE = "Apple";
	private static final String VALUE_FRUIT_GRAPH = "Graph";
	private static final String VALUE_FRUIT_LEMON = "Lemon";
	
	private static final String VALUE_DIAMETER_1 = "1";
	private static final String VALUE_DIAMETER_3 = "3";
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// defining data format

		ArrayList<String> colorVals = new ArrayList<String>();
		colorVals.add(VALUE_COLOR_GREEN);
		colorVals.add(VALUE_COLOR_RED);
		colorVals.add(VALUE_COLOR_YELLOW);
		
		ArrayList<String> diamVals = new ArrayList<String>();
		diamVals.add(VALUE_DIAMETER_1);
		diamVals.add(VALUE_DIAMETER_3);
		
		ArrayList<String> fruitVals = new ArrayList<String>();
		fruitVals.add(VALUE_FRUIT_APPLE);
		fruitVals.add(VALUE_FRUIT_GRAPH);
		fruitVals.add(VALUE_FRUIT_LEMON);
		

		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		Attribute attr1 = new Attribute("color", colorVals);
		Attribute attr2 = new Attribute("diameter", diamVals);
		Attribute attr3 = new Attribute("fruit", fruitVals);
		attrs.add(attr1);
		attrs.add(attr2);
		attrs.add(attr3);
		

		// defining data dictionary

		Map<Attribute, List<String>> definition = new LinkedHashMap<Attribute, List<String>>();
		definition.put(attr1, colorVals);
		definition.put(attr2, diamVals);
		definition.put(attr3, fruitVals);
		
	
		// training

		List<Instance> training = generateData(attrs);

		Gini gini = new Gini(definition, attr3);

		CARTNode.Strategy.Builder<Gini> builder = new CARTNode.Strategy.Builder<Gini>(gini);
		
		CARTNode<Gini> root = builder.build(training);
		
		System.out.println(root.toAll());
	}

	public static List<Instance> generateData(ArrayList<Attribute> attrs) {

		Instances training = new Instances("TRAINING", attrs, 5);
		
		Instance data1 = new DenseInstance(3);	
		data1.setValue(attrs.get(0), VALUE_COLOR_GREEN);
		data1.setValue(attrs.get(1), VALUE_DIAMETER_3);
		data1.setValue(attrs.get(2), VALUE_FRUIT_APPLE);
		training.add(data1);
		
		Instance data2 = new DenseInstance(3);	
		data2.setValue(attrs.get(0), VALUE_COLOR_YELLOW);
		data2.setValue(attrs.get(1), VALUE_DIAMETER_3);
		data2.setValue(attrs.get(2), VALUE_FRUIT_APPLE);
		training.add(data2);
		
		Instance data3 = new DenseInstance(3);	
		data3.setValue(attrs.get(0), VALUE_COLOR_RED);
		data3.setValue(attrs.get(1), VALUE_DIAMETER_1);
		data3.setValue(attrs.get(2), VALUE_FRUIT_GRAPH);
		training.add(data3);
		
		Instance data4 = new DenseInstance(3);	
		data4.setValue(attrs.get(0), VALUE_COLOR_RED);
		data4.setValue(attrs.get(1), VALUE_DIAMETER_1);
		data4.setValue(attrs.get(2), VALUE_FRUIT_GRAPH);
		training.add(data4);
		
		Instance data5 = new DenseInstance(3);	
		data5.setValue(attrs.get(0), VALUE_COLOR_YELLOW);
		data5.setValue(attrs.get(1), VALUE_DIAMETER_3);
		data5.setValue(attrs.get(2), VALUE_FRUIT_LEMON);
		training.add(data5);

		return training;
	}

	public static class Gini implements CARTNode.Strategy {

		private Map<Attribute, List<String>> definition = null;
		private List<Attribute> attrs = null;
		private Attribute cls = null;

		public Gini(Map<Attribute, List<String>> definition, Attribute cls) {
			this.definition = definition;
			this.attrs = definition.keySet().stream().collect(Collectors.toList());

			this.cls = cls;
			this.attrs.remove(cls);
		}
		
		@Override
		public Map<Attribute, List<String>> definition() {
			return definition;
		}
		
		@Override
		public Attribute cls() {
			return cls;
		}
		
		@Override
		public CARTNode<Gini> calculate(double ggini, List<Instance> instances) {
			
			CARTNode.Strategy.Builder<Gini> builder = new CARTNode.Strategy.Builder<Gini>(this);
			DoubleAdder min = new DoubleAdder();
			min.add(ggini);
			
			PlaceHolder<CARTNode<Gini>> holder = new PlaceHolder<CARTNode<Gini>>();
			
			this.definition.entrySet().stream().forEach(p -> {
				
				if (p.getKey() != this.cls) {
				
					p.getValue().stream().forEach(v -> {
					
						List<String> list = new ArrayList<String>();
						list.add(v);
						list.add(v);
					
						CARTNode<Gini> node = builder.test(p.getKey(), list, instances);
						double score = node.score();
					
						if (min.doubleValue() > score) {
							min.reset();
							min.add(score);
							node.value(v);
							holder.data(node);
						}
					});
				}
			});
			
			
			return holder.data();
		}

		@Override
		public double score(CARTNode<?> node) {
			// TODO Auto-generated method stub

			// gini impurities
			DoubleAdder sum = new DoubleAdder();

			if (node.inputs().size() <= 0)
				return 0.0;

			if (node.children().size() <= 0) {

				node.data().entrySet().stream().forEach(p -> {
					sum.add(Math.pow((double) p.getValue().size() / node.inputs().size(), 2));
				});

				return 1 - sum.doubleValue();
			} else {

				node.children().entrySet().stream().forEach(p -> {

					sum.add((double) node.data().get(p.getKey()).size() / node.inputs().size() * score(p.getValue()));
				});

				return sum.doubleValue();
			}
		}

		@Override
		public List<Instance> filter(boolean binary, CARTNode<?> node, String value, List<Instance> instances) {
			
			List<Instance> res = null;
			
			switch(node.attr().name()) {
			case "diameter":
				if (binary)
					res = instances.stream().filter(p -> p.value(node.attr()) < Integer.valueOf(value)).collect(Collectors.toList());
				else
					res = instances.stream().filter(p -> p.value(node.attr()) >= Integer.valueOf(value)).collect(Collectors.toList());
			break;
			case "color":
			default:
				if (binary)
					res = instances.stream().filter(p -> !value.equals(p.stringValue(node.attr()))).collect(Collectors.toList());
				else
					res = instances.stream().filter(p -> value.equals(p.stringValue(node.attr()))).collect(Collectors.toList());
			}
			
			return res;
		}
	}
}