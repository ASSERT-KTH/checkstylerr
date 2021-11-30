
/**
 * ≤‚ ‘break∫Õcontinue
 * 
 * @author dell
 * 
 */
public class TestBreakContinue {
	public static void main(String[] args) {
		int total = 0;
		System.out.println("Begin");
		while (true) {
			total++;
			int i = (int) Math.round(100 * Math.random());
			if (i == 88) {
				break;
			}
		}
		System.out.println("Game over, used " + total + " times.");

		System.out.println("###########################");
		for (int i = 100; i < 150; i++) {
			if (i % 3 == 0) {
				continue;
			}
			System.out.println(i);
		}

		System.out.println("**************************");
		int count = 0;
		outer: for (int i = 101; i < 150; i++) {
			for (int j = 2; j < i / 2; j++) {
				if (i % j == 0)
					continue outer;
			}
			System.out.print(i + "  ");
		}

	}
}
