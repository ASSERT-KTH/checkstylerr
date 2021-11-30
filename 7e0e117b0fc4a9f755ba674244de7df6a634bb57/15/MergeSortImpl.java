

/*question: design an algorithm to
implement merge sort using ll*/


class Node {

    public int item;
    public Node next;

    public Node(int val) {
        this.item = val;
    }

    public Node() {

    }

    public void displayNode() {
        System.out.print("[" + item + "] ");
    }
}


class myMergesort {

    private Node first;

    public myMergesort() {
        first = null;
    }

    public boolean isEmpty() {
        return (first == null);
    }

    public void insert(int val) {

        Node newNode = new Node(val);

        // add in the back of the queue
        newNode.next = first;
        first = newNode;
    }

    public void append(Node result) {
        first = result;
    }

    public void display() {

        Node current = first;

        while (current != null) {
            current.displayNode();
            current = current.next;
        }
        System.out.println("");
    }

    // get the end of the queue
    public Node extractFirst() {
        return first;
    }

    // this MergeSort returns the head of the sorted LL
    public Node MergeSort(Node headOriginal) {

        if (headOriginal == null || headOriginal.next == null) {
            return headOriginal;
        }

        Node a = headOriginal;
        Node b = headOriginal.next;

        // split the linked list with two parts
        while ((b != null) && (b.next != null)) {
            headOriginal = headOriginal.next;
            b = (b.next).next;
        }

        b = headOriginal.next;
        headOriginal.next = null;

        return merge(MergeSort(a), MergeSort(b));
    }

    public Node merge(Node a, Node b) {

        Node head = new Node();
        Node c = head;

        while ((a != null) && (b != null)) {

            if (a.item <= b.item) {
                c.next = a;
                c = a;
                a = a.next;
            } else {
                c.next = b;
                c = b;
                b = b.next;
            }
        }

        // define the last element of the ll
        c.next = (a == null) ? b : a;
        return head.next;
    }
}
/*END of solution: design an algorithm to
implement merge sort using ll*/



/*question: design an algorithm to implement merge sort*/
public class MergeSortImpl {

	/*Sort each pair of elements. Then, sort every four elements
	by merging every two pairs. Then, sort every 8 elements, etc.*/
    // time complexity:  O(n log n) usual, worst case: O(n log n)
    // space complexity: O(n)

    public static int[] mergeSort(int[] a, int low, int high) {

        int N = high - low;

        if (N <= 1)
            return a;

        int mid = (low + high) / 2;

        mergeSort(a, low, mid);
        mergeSort(a, mid, high);

        int[] temp = new int[N];
        int i = low, j = mid;

        for (int k = 0; k < N; k++) {

            if (i == mid)
                temp[k] = a[j++];

            else if (j == high)
                temp[k] = a[i++];

            else if (a[j] < a[i])
                temp[k] = a[j++];

            else
                temp[k] = a[i++];
        }

        for (int k = 0; k < N; k++) {
            a[low + k] = temp[k];
        }

        return a;
    }
	/*END of solution: design an algorithm
	to implement merge sort*/
    
}








