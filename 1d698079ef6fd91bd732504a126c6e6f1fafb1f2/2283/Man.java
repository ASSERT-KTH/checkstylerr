package cn.bjsxt.oop.encapsulation01;

public class Man {
	private String name;
	private String id;
	private boolean man;
	public static int cc;
	public static final int MAX_SPEED = 120;
	
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isMan() {
		return man;
	}
	public void setMan(boolean man) {
		this.man = man;
	}
	
	
	
	
}
