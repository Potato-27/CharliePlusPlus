public class Main {
	public static int x = 10;
	public static int y = 5;
	public static boolean check = true;
	public static void main(String[] args) {
		System.out.println("Initial values:");
		System.out.println(x);
		System.out.println(y);
		System.out.println(check);
		System.out.println("");
		if ((x + 2) * y > 50 && check) {
		check = false;
		System.out.println("Updated check to false:");
		System.out.println(check);
}

		if (x == 10 || check == true) {
		System.out.println("yay");
}

	int mathResult = ((x - y) * 2) + 1;
		System.out.println(mathResult);
}
}
