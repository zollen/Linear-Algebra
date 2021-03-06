package machinelearning.hmm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.nd4j.linalg.primitives.Pair;

public class Viterbi implements HMMAlgothrim<Double> {
	
	private VirterbiAlgorithm algorithm = VirterbiAlgorithm.BAYES_RULES_ALGO;
	private UnderFlowStrategy strategy = UnderFlowStrategy.NONE;
	
	public static void main(String[] args) throws Exception {
		/**
		 * Bayes Rule Viterbi Intuition
		 * ============================
		 *  X1 -> X2 -> X3
		 *   |     |     |
		 *  \|/   \|/   \|/
		 *  E1     E2    E3
		 *  
		 *  Step: 
		 *  1. V1 = max( Σi=0..2  S[i,0] * E[i,'E1'] ); 
		 *  2. V2 = max( Σj=0..2  T[i(V1),j] * E[j,'E2'] * prob(V1) )
		 *  3. V3 = max( Σk=0..2  T[j(V2),k] * E[k,'E3'] * prob(V2) )
		 *  4. Each level of recursion, one list (out of many *completed* lists) with the highest
		 *     last state probability would be chosen to return to the caller level.   
		 *  
		 */
		
		DecimalFormat ff = new DecimalFormat("0.0000");
		
		String[] states = { "#", "NN", "VB" };
		String[] observations = { "I", "write", "a letter" };
		int [] converter = { 0, 1, 2 };
		double[] start_probability = { 0.3, 0.4, 0.3 };
		
		System.out.print("States: ");
		for (int i = 0; i < states.length; i++) {
			System.out.print(states[i] + ", ");
		}
		System.out.print("\nObservations: ");
		for (int i = 0; i < observations.length; i++) {
			System.out.print(observations[i] + ", ");
		}
		System.out.print("\nStart probability: ");
		for (int i = 0; i < states.length; i++) {
			System.out.print(states[i] + ": " + start_probability[i] + ", ");
		}
		
		System.out.println();
		
		
		Equation eq = new Equation();	

		eq.process("T = [" +
					/*      #,   NN,   VB      */
	/* # */				" 0.2, 0.2, 0.6;" +
	/* NN */			" 0.4, 0.1, 0.5;" +
	/* VB */			" 0.1, 0.8, 0.1 " +
						"]");
		
		eq.process("E = [" +
					/*      I,    write,   a letter      */
	/* # */				" 0.01,	   0.02,     0.02;" +
	/* NN */			"  0.8,    0.01,      0.5;" +
	/* VB */			" 0.19,    0.97,     0.48 " +
						"]");
		
		eq.process("S = [ 0.3; 0.4; 0.3 ]");
				
		DMatrixRMaj T = eq.lookupDDRM("T");
		DMatrixRMaj E = eq.lookupDDRM("E");
		DMatrixRMaj S = eq.lookupDDRM("S");
		
		Printer p = new Printer(ff);
		
		{
			System.out.print("Wiki Proposed ALGO: ");
			
			double start = System.nanoTime();
			
			Viterbi v = new Viterbi(VirterbiAlgorithm.WIKI_PROPOSED_ALGO);
			String output  = p.display(states, v.fit(converter, S, T, E));
			
			double end = System.nanoTime();

			System.out.println(output + "    Performance: " + ((end - start) / 1000000.0) + " ms");
		}

		{
			System.out.print("Bayes Rules ALGO  : ");
			
			double start = System.nanoTime();
			
			Viterbi v = new Viterbi(VirterbiAlgorithm.BAYES_RULES_ALGO);
			String output  = p.display(states, v.fit(converter, S, T, E));
			
			double end = System.nanoTime();

			
			System.out.println(output + "    Performance: " + ((end - start) / 1000000.0) + " ms");
		}

	}
	
	
	
	public Viterbi() {}
	
	public Viterbi(VirterbiAlgorithm algorithm) {
		this.setAlgorithm(algorithm);
	}
	
	public Viterbi(UnderFlowStrategy strategy) {
		this.setStrategy(strategy);
	}
	
	public Viterbi(VirterbiAlgorithm algorithm, UnderFlowStrategy strategy) {
		this.setAlgorithm(algorithm);
		this.setStrategy(strategy);
	}
	
	public VirterbiAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(VirterbiAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	public UnderFlowStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(UnderFlowStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public List<Pair<Integer, Double>> fit(int [] converter, DMatrixRMaj S, DMatrixRMaj T, DMatrixRMaj E) {
		
		if (this.algorithm == VirterbiAlgorithm.WIKI_PROPOSED_ALGO) {
			return wiki(converter, S, T, E);
		}
		else {
			return bayes(converter, S, T, E);
		}
	}
	
	public double probability(List<Pair<Integer, Double>> list) {
		
		double prob = list.get(list.size() - 1).getSecond();
		
		if (this.strategy == UnderFlowStrategy.ENABLED) {
			prob = Math.exp(prob);
		}
		
		return prob;
	}
	
	

	private static class TNode {
		public List<Pair<Integer, Double>> v_path;
		public double v_prob;

		public TNode(List<Pair<Integer, Double>> v_path, double v_prob) {
			this.v_path = new ArrayList<Pair<Integer, Double>>(v_path);
			this.v_prob = v_prob;
		}
	}

	public List<Pair<Integer, Double>> wiki(int [] converter, DMatrixRMaj sp, DMatrixRMaj tp, DMatrixRMaj ep) {
		
		TNode[] T = new TNode[tp.numRows];
		for (int state = 0; state < tp.numRows; state++) {
			List<Pair<Integer, Double>> intArray = new ArrayList<Pair<Integer, Double>>();
			
			double v_prob = sp.get(state, 0) * ep.get(state, converter[0]);
			
			if (this.strategy == UnderFlowStrategy.ENABLED) {
				v_prob = Math.log(v_prob == 0 ? Double.MIN_VALUE : v_prob);
			}

			
			intArray.add(new Pair<>(state, v_prob));
			T[state] = new TNode(intArray, v_prob);
		}

		
		for (int output = 1; output < converter.length; output++) {
			
			TNode[] U = new TNode[tp.numRows];
			for (int next_state = 0; next_state < tp.numRows; next_state++) {
				
				List<Pair<Integer, Double>> argmax = null;
				double valmax = Double.NEGATIVE_INFINITY;
				
				for (int current_state = 0; current_state < tp.numRows; current_state++) {
					
					List<Pair<Integer, Double>> v_path = new ArrayList<Pair<Integer, Double>>(T[current_state].v_path);
					
					double v_prob = T[current_state].v_prob;
					
					double p = ep.get(next_state, converter[output]) * tp.get(current_state, next_state);

					if (this.strategy == UnderFlowStrategy.NONE) {
						v_prob *= p;
					}
					else {
						v_prob += Math.log(p == 0 ? Double.MIN_VALUE : p);
					}
				
					
					if (v_prob > valmax) {
						
						if (v_path.size() == converter.length) {
							argmax = v_path;
						} else {
							argmax = v_path;
							argmax.add(new Pair<>(next_state, v_prob));
						}
						
						valmax = v_prob;
					}
				}

				U[next_state] = new TNode(argmax, valmax);
			}
			
			T = U;
		}
		
		// apply sum/max to the final states:
		List<Pair<Integer, Double>> argmax = null;
		double valmax = Double.NEGATIVE_INFINITY;
		
		for (int state = 0; state < tp.numRows; state++) {
			
			List<Pair<Integer, Double>> v_path = new ArrayList<Pair<Integer, Double>>(T[state].v_path);
			double v_prob = T[state].v_prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}
	
		return argmax;
	}
	
	public List<Pair<Integer, Double>> bayes(int [] converter, DMatrixRMaj S, DMatrixRMaj T, DMatrixRMaj E) {

		List<Pair<Integer, Double>> desirable = null; 
		double maxProb = Double.NEGATIVE_INFINITY;
		
		for (int row = 0; row < S.numRows; row++) {
			
			List<Pair<Integer, Double>> list = new ArrayList<Pair<Integer, Double>>();
			
			double prob = S.get(row, 0) * E.get(row, converter[0]);
			if (prob > 0) {
				
				if (this.strategy == UnderFlowStrategy.ENABLED) {
					prob = Math.log(prob);
				}
				
				list.add(new Pair<>(row, prob));
				
				list = compute(converter, T, E, list);
				
				double pp = list.get(list.size() - 1).getSecond();
				
				if (pp > maxProb) {
					
					maxProb = pp; 
					desirable = list;
				}
				
			}	
		}
		
		return desirable;
	}
	
	private List<Pair<Integer, Double>> compute(int [] converter, DMatrixRMaj T, DMatrixRMaj E, List<Pair<Integer, Double>> list) {
		
		Pair<Integer, Double> last = list.get(list.size() - 1);
	
		if (list.size() == converter.length) {
			return list;
		}
		
			
		double maxProb = Double.NEGATIVE_INFINITY;
		List<Pair<Integer, Double>> desirable = null;
		
		for (int col = 0; col < T.numCols; col++ ) {
			
			double prob = 0.0;
			
			if (this.strategy == UnderFlowStrategy.NONE) {
				prob = last.getSecond() * T.get(last.getFirst(), col) * E.get(col, converter[list.size()]);
			}
			else {
				
				double tmp = T.get(last.getFirst(), col) * E.get(col, converter[list.size()]);			
				prob = last.getSecond() + Math.log(tmp == 0 ? Double.MIN_VALUE : tmp); 
			}
			
			List<Pair<Integer, Double>> tmp = new ArrayList<Pair<Integer, Double>>(list);
			tmp.add(new Pair<Integer, Double>(col, prob));
			
			tmp = compute(converter, T, E, tmp);
			double pp = tmp.get(tmp.size() - 1).getSecond();
			
			if (pp > maxProb) {
				
				maxProb = pp; 
				desirable = tmp;
			}
		}
		
		return desirable;
	}
	
}