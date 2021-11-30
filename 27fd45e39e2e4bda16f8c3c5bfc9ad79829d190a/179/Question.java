//package Q7_12_Hash_Table;
package design.practice.hashTable;

public class Question {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Dummy bob = new Dummy("Bob", 20);
		Dummy jim = new Dummy("Jim", 25);
		Dummy alex = new Dummy("Alex", 30);
		Dummy tim = new Dummy("Tim", 35);
		Dummy maxwell = new Dummy("Maxwell", 40);
		Dummy john = new Dummy("John", 45);
		Dummy julie = new Dummy("Julie", 50);
		Dummy christy = new Dummy("Christy", 55);
		Dummy tim2 = new Dummy("Tim", 100); // This should replace the first "tim"
		
		Dummy[] dummies = {bob, jim, alex, tim, maxwell, john, julie, christy, tim2};
		
		/* Test: Insert Elements. */
		Hasher<String, Dummy> hash = new Hasher<String, Dummy>(3);

		for (Dummy d : dummies) {

			hash.put(d.getName(), d);
		}
		
		hash.printTable();
		
		// Test: Recall
		for (Dummy d : dummies) {

			String name = d.getName();
			Dummy dummy = hash.get(name);
			System.out.println("Dummy named " + name + ": " + dummy.toString());
		}
	}
}
