public class Main {
	public static void main(String[] args) {
	int answer = 5;
		answer = squared(answer) * 4 + 37;
		System.out.println(answer);
}
public static Object squared(int x) {
	return x * x;
}
}
