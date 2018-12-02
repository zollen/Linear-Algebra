import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;

public class HiddenMarkovModelExercise1 {

	private static final DecimalFormat ff = new DecimalFormat("0.######");
	
	private static final String[] STATES = { "A", "C", "T", "G" };

	private static DMatrixRMaj T = null;
	private static DMatrixRMaj E = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/**
		 * states = ('0', '1') observations = ('A', 'C', 'T', 'G')
		 * 
		 * start_probability = {'0': 0.5, '1': 0.5}
		 * 
		 * transition_probability = { '0' : {'0': 0.75, '1': 0.25}, '1' : {'0': 0.25,
		 * '1': 0.75}, }
		 * 
		 * emission_probability = { '0' : {'A': 0.45, 'C': 0.05, 'T': 0.05, 'G': 0.45},
		 * '1' : {'A': 0.05, 'C': 0.45, 'T': 0.45, 'G': 0.05}, }
		 */

		Equation eq = new Equation();
		eq.process("T = [ " +
						/* 0, 1 */
			/* 0 */ "   0.75,  0.25;" +
			/* 1 */ "   0.25,  0.75 " +
						"]");

		eq.process("E = [" +
					/* 0, 1 */
		/* A */ "   0.45,  0.05;" +
		/* C */ "   0.05,  0.45;" +
		/* T */ "   0.05,  0.45;" +
		/* G */ "   0.45,  0.05 " + 
					"]");

		T = eq.lookupDDRM("T");
		E = eq.lookupDDRM("E");
		
		System.out.println("===================Forward===================");

		// FORWARD Algorithm with N states(0, 1) and M emissions(A, C, T, G)
		// f(0, k) = startprob(i) * E(i)    <-- 1 <= i <= N, 1 <= k <= M
		// for k in { A, C, T, G }          <-- specific sequence of emissions
		//    for i in 1..N
		//       sum(i) = 0
		//       for j in i..N
		//	        sum(i) += f(i - 1) * P(state(i) <- state(j)) * E(i, k)     <-- state(i) and *current* emission state
		//       f(i, k) = sum(i)
		
		// A0 = 0.225 = 0.5 * 0.75 * 0.45 + 0.5 * 0.25 * 0.45
		// A1 = 0.025 = 0.5 * 0.75 * 0.05 + 0.5 * 0.25 * 0.05
		// C0 = 0.00875 = A0 * 0.75 * 0.05 + A1 * 0.25 * 0.05
		// C1 = 0.003375 = A1 * 0.75 * 0.45 + A0 * 0.25 * 0.45
		// T0 = 0.00075 = C0 * 0.75 * 0.05 + C1 * 0.25 * 0.05
		// T1 = 0.012375 = C1 * 0.75 * 0.45 + C0 * 0.25 * 0.45
		// G0 = 0.001645 = T0 * 0.75 * 0.05 + T1 * 0.25 * 0.05
		// G1 = 0.000473 = T1 * 0.75 * 0.45 + T0 * 0.25 * 0.45

		Map<Integer, Double> starts = new HashMap<Integer, Double>();
		starts.put(0, 0.5d);
		starts.put(1, 0.5d);

		forward(starts);
		
		System.out.println("==================Viterbi====================");
		
		viterbi();
		
		System.out.println("===================Backward===================");
		
		// BACKWARD Algorithm with N states(0, 1) and M emissions(A, C, T, G)
		// f(0, k) = 1                    <-- 1 <= i <= N, 1 <= k <= M
		// for k in { G, T, C, A }        <-- reversed sequence of emissions
		//    for i in 1..N
		//       sum(i) = 0
		//       for j in 1..N
		//          sum(i) += f(i - 1) * P(state(i) -> state(j)) * E(j, k - 1)  <-- state(j) and *last* emission state
		//       f(i, k) = sum(i)
		
		// G0 = 1
		// G1 = 1
		// T0 = 0.35 = 1 * 0.75 * 0.45 + 1 * 0.25 * 0.05 
		// T1 = 0.15 = 1 * 0.75 * 0.05 + 1 * 0.25 * 0.45 
		// C0 = 0.03 = t0 * 0.75 * 0.05 + t1 * 0.25 * 0.45   
		// C1 = 0.055 = t1 * 0.75 * 0.45 + t0 * 0.25 * 0.05  
		// A0 = 0.007313 = c0 * 0.75 * 0.05 + c1 * 0.25 * 0.45 
		// A1 = 0.018938 = c1 * 0.75 * 0.45 + c0 * 0.25 * 0.05
		
		Map<Integer, Double> ends = new HashMap<Integer, Double>();
		ends.put(0, 1d);
		ends.put(1, 1d);
		
		backward(ends);
	}
	
	public static void backward(Map<Integer, Double> ends) {
			
		Map<Integer, Double> states = new HashMap<Integer, Double>();
		final Map<Integer, Double> ss = states;
		
		ends.entrySet().stream().forEach(p -> ss.put(p.getKey(), p.getValue())); 
		
		ss.entrySet().stream().forEach(p -> System.out.println(STATES[STATES.length - 1] + p.getKey() + " ===> " + p.getValue()));
		
		for (int gene = STATES.length - 1; gene > 0; gene--) {
			
			Map<Integer, Double> nexts = new HashMap<Integer, Double>();
			Set<Integer> froms = states.keySet();
			
			for (Integer from : froms) {
				
				double sum = 0d;
				
				for (int to = 0; to < T.numRows; to++) {
				
					if (T.get(to, from) > 0 && E.get(gene, to) > 0) {
						
						double last = 0d;
						if (states.get(to) != null)
							last = states.get(to);
						
						sum += (double) last * T.get(to, from) * E.get(gene, to);
					}
				}
				
				System.out.println(STATES[gene - 1] + from + " ===> " + ff.format(sum));
				nexts.put(from, sum);
			}
			
			states = nexts;
		}
	}
	
	public static void viterbi() {
		
		
	}

	public static void forward(Map<Integer, Double> starts) {

		Map<Integer, Double> states = new HashMap<Integer, Double>();
		final Map<Integer, Double> ss = states;
		
		starts.entrySet().stream().forEach(p -> { 
				ss.put(p.getKey(), p.getValue() * E.get(0, p.getKey())); 
		});
		
		ss.entrySet().stream().forEach(p -> System.out.println(STATES[0] + p.getKey() + " ===> " + p.getValue()));
		
		
		for (int gene = 0 + 1; gene < E.numRows; gene++) {

			Map<Integer, Double> nexts = new HashMap<Integer, Double>();
			Set<Integer> tos = states.keySet();
		
			for (Integer to : tos) {
				
				double sum = 0d;
				
				for (int from = 0; from < T.numCols; from++) {
					
					if (T.get(to, from) > 0 && E.get(gene, to) > 0) {
					
						double last = 0d;
						if (states.get(from) != null)
							last = states.get(from);
					
						sum += (double) last * T.get(to, from) * E.get(gene, to);
					}
				}
				
				System.out.println(STATES[gene] + to + " ===> " + ff.format(sum));
				nexts.put(to, sum);
			}
			
			states = nexts;
		}
	}

}