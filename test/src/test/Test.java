package test;

public class Test {
	public Test(String[] args){
		for(String str : args){
			if(str.trim().matches("^\\{[A-Za-z0-9.]+\\}$")){
				System.out.println("Match!");
			} else {
				System.out.println("No match :'(");
			}
		}
	}
	public static void main(String[] args) {
		String[] args2 = {"{bier.bier}", "hoi", "test", "{asdflj21351234}", "1"};
		Test test = new Test(args2);
	}

}
