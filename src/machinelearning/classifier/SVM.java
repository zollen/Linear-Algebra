package machinelearning.classifier;

import java.text.DecimalFormat;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.equation.Equation;

public class SVM {
	
	/**
	 * Support Vector Machine
	 * ======================
	 * Let w vector ⊥ to the middle margin
	 * Let u vector points to any sample point
	 * where w * u >= b where b is a constant
	 * --------------------------------------
	 * #1. Decision Rule 
	 * w * u + b >= 0 then +
	 * --------------------------------------
	 * 
	 * Let {x+} is a positive sample to the right of the middle margin
	 * Let {x-} is a negative sample to the left of the middle margin
	 * w * {x+} + b >= 1
	 * w * {x-} + b <= -1
	 * Let's introduce a new variable y so y = 1 if {x+}, y = -1 if {x-}
	 * So we can condense above two inequalities into a single equation as follow:
	 * y * (w * {x+} + b) >= 1    <-- (y = 1)
	 * y * (w * {x-} + b) >= 1    <-- (y = -1) 
	 * 
	 * y * (w * x + b) - 1 >= 0
	 * ---------------------------------------
	 * #2. Insight
	 * y * (w * x + b) - 1 = 0 when x is lie exactly in the boundary of the middle margin
	 * ---------------------------------------
	 * 
	 * Lets find out the exact distance between the two boundaries of the middle margin
	 * by using vector addition/subtraction.
	 * 
	 * Width of the margin = ({x+} - {x-}) * w / ||w||    <-- turn into a unit vector
	 * 
	 * From #2, we know {x+} = 1 - b, {x-} = -1 - b
	 * 
	 * Width of the margin = (1 - b + b + 1) * 1 / ||w|| = 2 / ||w||
	 * 
	 * Max(2 / ||w||), or.. Min(||w||), or.. it is roughly equivalent to Min(1/2||W||^2) for easy math later
	 * 
	 * Now we have an minimization/optimization problem with constraints.
	 *   Optimization problem: Min(1/2||w||^2)
	 *   Constraint:           y * (w * x + b) - 1 = 0
	 * Let's use Lagrange Multiplier to convert the constrained optimization problem into an unconstrained
	 * optimization problem.
	 * Let αi a Lagrange Multiplier for one constraint
	 * L = <optimization problem> - Σ αi (all constraints)
	 * ----------------------------------------
	 * L = 1/2||w||^2 - Σ (αi * (yi(w * xi + b) - 1))
	 * ----------------------------------------
	 * ∂L/∂w = w - Σ αi yi xi = 0,   w = Σ αi yi xi   with w when L is extreme
	 * ∂L/∂b = - Σ αi yi = 0,        Σ αi yi = 0      with b when L is extreme
	 * 
	 * -----------------------------------------
	 * w = Σ αi yi xi   <--- w is a linear sum of all samples - some αi are zeros, others non-zeros αi - xi are support vectors
	 * 0 = Σ αi yi <-- all support vectors αi yi are sum up to zero.
	 * -----------------------------------------
	 * 
	 * Let's plug the above expressions back into the Lagrange optimization equation
	 * 
	 * L = 1/2(Σ αi yi xi)(Σ αi yi xi) - Σ αi yi xi * Σ αi yi xi - Σ αi yi b + Σ xi
	 * Since b is a constant and Σ αi yi = 0
	 * L = 1/2(Σ αi yi xi)(Σ αi yi xi) - Σ αi yi xi * Σ αi yi xi - b Σ αi yi + Σ xi
	 * L = 1/2(Σ αi yi xi)(Σ αi yi xi) - Σ αi yi xi * Σ αi yi xi + Σ xi
	 * 
	 * ----------------------------------------
	 * L = Σ xi - 1/2 ΣΣ αiαj yiyj (xi * xj)    
	 * This optimization formula depends only on the dot product of xi * xj of samples
	 * ----------------------------------------
	 * 
	 * ----------------------------------------
	 * Revised Decision Rule
	 * Let u vector points to an unknown sample point
	 * Σ αi yi xi * u + b >= 0 then +     <-- The decision totally depends on the dot product of xi * u
	 * ----------------------------------------
	 * 
	 * 
	 * We simplified the example not to use kernel (for transforming the mult-dimension spaces so we can
	 * separate the inter-mangled sample points)
	 */
	
	/**
	 * Another Good Example:
	 * https://www.youtube.com/watch?v=1NxnPkZM9bc
	 * Three sample points: (1,1), (2,0), (2,3)
	 * weight vector: w = (2, 3) - (1, 1) = (a, 2a) <-- line parallel to both (1,1) and (2,3)
	 * Two equations and two unknowns
	 * a + 2a + w = -1 for (1, 1)
	 * 2a + 6a + w = 1  for (2, 3)
	 * --------------------------
	 * a = 2/5, w = -11/5
	 * Support Vector: w = (2/5, 4/5)
	 * g(x) = 2/5 x1 + 4/5 x2 - 11/5
	 * g(x) = x1 + 2 x2 - 5.5
	 * g(2,0) = 2 + 2 (0) - 5.5 = -3.8
	 *
	 * 
	 */
	
	private static final DecimalFormat ff = new DecimalFormat("0");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Equation eq = new Equation();
		eq.process("A = [ " + 
						//		x,   y
						//  -----------
							" 1.0, 1.0;" + 
							" 1.0, 2.0;" + 
							" 3.0, 1.0;" +
							" 2.0, 2.0;" +
							
							" 7.0, 8.0;" +
							" 8.0, 9.0;" +
							" 8.0, 8.0;" +
							" 7.0, 10.0" +
						"]");
		
		eq.process("A1 = A(:, 0)");
		eq.process("A2 = A(:, 1)");
		
		eq.process("Y = [" +
							" 1.0;" +
							" 1.0;" +
							" 1.0;" +
							" 1.0;" +
							
							" 0.0;" +
							" 0.0;" +
							" 0.0;" +
							" 0.0" +
						
							"]");
		
		eq.process("W = [ 0.0; 0.0 ]");

		DMatrixRMaj A = eq.lookupDDRM("A");
		DMatrixRMaj A1 = eq.lookupDDRM("A1");
		DMatrixRMaj A2 = eq.lookupDDRM("A2");
		DMatrixRMaj expected = eq.lookupDDRM("Y");
		DMatrixRMaj W = eq.lookupDDRM("W");
		
	
		
		final double ALPHA = 0.0001;  // learning rate
		final int EPOCHS = 10000;
		
		// Min(λ ||w||^2) + Σ ( 1 - yi<xi, wi> )
		// 	∂/∂w(λ ||w||^2)      = 2 λ w
		// 	∂/∂w(1 - yi<xi, wi>) = 0 if yi<xi, wi> >= 1, else - yi * xi
		// 
		// The gradient update for classification correctly
		// 	2 λ w + 0
		// The gradient update for classification incorrectly
		//  2 λ w + yi * xi
		
		for (int epoch = 1; epoch <= EPOCHS; epoch++) {
			
			DMatrixRMaj actual = new DMatrixRMaj(A.numRows, 1);
			
			CommonOps_DDRM.mult(A, W, actual);
			

			double val = CommonOps_DDRM.dot(expected, actual);
			double lambda = (double) 1.0 / epoch;   // regularizing value
			
				
			if (val >= 1.0) {
				// classify correctly
				// w = w - α * (2 λ w)
				W.set(0, 0, W.get(0, 0) - ALPHA * (2.0 * lambda * W.get(0, 0)));
				W.set(1, 0, W.get(1, 0) - ALPHA * (2.0 * lambda * W.get(1, 0)));
			}
			else {
				// classify incorrectly
				// w = w + α * (yi * xi - 2 λ w)
				W.set(0, 0, W.get(0, 0) + ALPHA * (CommonOps_DDRM.dot(A1, expected) - 2.0 * lambda * W.get(0, 0)));
				W.set(1, 0, W.get(1, 0) + ALPHA * (CommonOps_DDRM.dot(A2, expected) - 2.0 * lambda * W.get(1, 0)));
			}			
		}
		
		eq.process("T = [" +
						" 2.0,  3.0;" +
						" 7.0,  8.0;" +
						" 1.0, -1.0;" +
						" 9.0,  9.0;" +
						" 5.5,  5.5;" +
						" 6.6,  6.6 " +
						"]");	
		
		DMatrixRMaj T = eq.lookupDDRM("T");
		DMatrixRMaj R = new DMatrixRMaj(T.numRows, 1);
		
		CommonOps_DDRM.mult(T, W, R);
		
	
		for (int i = 0; i < T.numRows; i++) 
			System.out.println("(" + T.get(i, 0) + ", " + T.get(i, 1) + ") ===> " + ff.format(R.get(i, 0)));
	}

}