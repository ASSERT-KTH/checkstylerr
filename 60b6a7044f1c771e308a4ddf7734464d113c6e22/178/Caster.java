package ca.queensu.cs.aggregate;

public class Caster {
	public static Object castMe(String from) {
		try{
			return Double.parseDouble(from);
		}catch (Exception e) {
			
		}
		return from;
	}
}
