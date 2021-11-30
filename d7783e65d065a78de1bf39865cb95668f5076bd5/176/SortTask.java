

// A recursive resultless ForkJoinTask. This class establishes conventions to parameterize resultless actions as Void ForkJoinTasks. Because null is the only valid value of type Void, methods such as join always return null upon completion.
// Sample Usages. Here is a simple but complete ForkJoin sort that sorts a given long[] array:

 
static class SortTask extends RecursiveAction {

	
   final long[] array; final int lo, hi;
   SortTask(long[] array, int lo, int hi) {
     this.array = array; this.lo = lo; this.hi = hi;
   }
   SortTask(long[] array) { this(array, 0, array.length); }
   protected void compute() {
     if (hi - lo < THRESHOLD)
       sortSequentially(lo, hi);
     else {
       int mid = (lo + hi) >>> 1;
       invokeAll(new SortTask(array, lo, mid),
                 new SortTask(array, mid, hi));
       merge(lo, mid, hi);
     }
   }
   // implementation details follow:
   static final int THRESHOLD = 1000;
   void sortSequentially(int lo, int hi) {
     Arrays.sort(array, lo, hi);
   }
   void merge(int lo, int mid, int hi) {
     long[] buf = Arrays.copyOfRange(array, lo, mid);
     for (int i = 0, j = lo, k = mid; i < buf.length; j++)
       array[j] = (k == hi || buf[i] < array[k]) ?
         buf[i++] : array[k++];
   }
 }