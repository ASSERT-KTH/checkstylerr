

public class App{

	// The following example illustrates some refinements and idioms that 
	// may lead to better performance: RecursiveActions need not be fully 
	// recursive, so long as they maintain the basic divide-and-conquer 
	// approach. Here is a class that sums the squares of each element of 
	// a double array, by subdividing out only the right-hand-sides of repeated 
	// divisions by two, and keeping track of them with a chain of next references. 
	// It uses a dynamic threshold based on method getSurplusQueuedTaskCount, but 
	// counterbalances potential excess partitioning by directly performing leaf 
	// actions on unstolen tasks rather than further subdividing.

	double sumOfSquares(ForkJoinPool pool, double[] array) {
		int n = array.length;
		Applyer a = new Applyer(array, 0, n, null);
		pool.invoke(a);
		return a.result;
	}


	public static void main(String[] args) {
		System.out.println("Hello Berlin!");
	}	
}