public class Main {
@FunctionalInterface
interface Fn__FUN_NUMBER_to_NUMBER{
	double apply(double a0);
}

	public static final java.util.Scanner _in = new java.util.Scanner(System.in);	public static void main(String[] args) {
		renderWaveBanner();
		System.out.println("=== CHARLIE++ FEATURE SHOWCASE ===");
		System.out.println("polynomial(3) = " + polynomial_NUMBER(3.0));
		System.out.println("wave(pi / 4)  = " + wave_NUMBER(Math.PI / 4.0));
		System.out.println("relu(-5)      = " + relu_NUMBER(-5.0) + ", relu(8) = " + relu_NUMBER(8.0));
		System.out.println("d/dx [3x^2 - 4x + 7] at x=3  -> " + calc__derive_FUN_NUMBER_to_T152_NUMBER(Main::polynomial_NUMBER, 3.0));
		System.out.println("Integral of x^2 from 0 to 3  -> " + integrate_FUN_NUMBER_to_T183_NUMBER_NUMBER_NUMBER(Main::square_NUMBER, 0.0, 3.0, 1000.0));
		System.out.println("doubleValue(21)      = " + doubleValue_NUMBER(21.0));
		System.out.println("doubleValue('Chpp!') = " + doubleValue_STRING("Chpp!"));
	double i0 = 0.0;
	double i1 = 1.0;
	double i2 = 2.0;
	ChppArray vec = new ChppArray(2.0, 4.0, 6.0);
	ChppArray doubledVec = doubleValue_Listof_NUMBER(vec);
		System.out.println("doubleValue([2, 4, 6]) = [" + ((int)((double)doubledVec.get((int)(i0)))) + ", " + ((int)((double)doubledVec.get((int)(i1)))) + ", " + ((int)((double)doubledVec.get((int)(i2)))) + "]");
	ChppArray matrix = new ChppArray(new ChppArray(10.0, 20.0), new ChppArray(30.0, 40.0));
		((ChppArray)matrix.get((int)(i1))).set((int)i0, 99.0);
		System.out.println("Mutated matrix[1][0] = " + ((int)((double)((ChppArray)matrix.get((int)(i1))).get((int)(i0)))));
	ChppArray scores = new ChppArray(72.0, 88.0, 95.0, 64.0, 91.0, 85.0);
		System.out.println("Honors Scores (>= 85):");
for (Object _raw_s : scores){
double s = (double) _raw_s;
{
if (s >= 85.0) {
		System.out.println("  Score " + ((int)s) + " -> Grade " + classifyScore_NUMBER(s));
}
}}		renderWaveBanner();
}
public static void renderWaveBanner() {
	String esc = "\u001B";
	String bar = "";
for (double i = 0.0; i <= 59.0; i = i + 1) {
	double t = i * 0.14;
	double r = 127.5 + 127.5 * Math.sin(t);
	double g = 127.5 + 127.5 * Math.sin(t + 2.0944);
	double b = 127.5 + 127.5 * Math.sin(t + 4.1888);
		bar = bar + esc + "[38;2;" + ((int)r) + ";" + ((int)g) + ";" + ((int)b) + "m█";
}
		System.out.println(bar + esc + "[0m");
}
public static double polynomial_NUMBER(double x) {
	return 3.0 * Math.pow(x, 2.0) - 4.0 * x + 7.0;
}
public static double wave_NUMBER(double x) {
	return Math.sin(2.0 * x) * Math.cos(x);
}
public static double relu_NUMBER(double x) {
		if (x < 0.0) {
	return 0.0;
}
 else  {
	return x;
}
}
public static double calc__derive_FUN_NUMBER_to_T152_NUMBER(Fn__FUN_NUMBER_to_NUMBER f, double x) {
	double h = 1.0E-5;
	double left = f.apply(x - h);
	double right = f.apply(x + h);
		if (left != left) {
	double center = f.apply(x);
		if (center != center || right != right) {
	return center;
}

	return (right - center) / h;
}

		if (right != right) {
	double center = f.apply(x);
		if (center != center) {
	return center;
}

	return (center - left) / h;
}

	return (right - left) / (2.0 * h);
}
public static double integrate_FUN_NUMBER_to_T183_NUMBER_NUMBER_NUMBER(Fn__FUN_NUMBER_to_NUMBER f, double a, double b, double steps) {
	double dx = (b - a) / steps;
	double total = 0.0;
for (double i = 0.0; i <= steps - 1.0; i = i + 1) {
	double mid = a + (i + 0.5) * dx;
		total = total + f.apply(mid) * dx;
}
	return total;
}
public static double square_NUMBER(double x) {
	return Math.pow(x, 2.0);
}
public static double doubleValue_NUMBER(double v) {
	return v + v;
}
public static String doubleValue_STRING(String v) {
	return v + v;
}
public static ChppArray doubleValue_Listof_NUMBER(ChppArray v) {
	return ChppArray.addElements(v, v);
}
public static String classifyScore_NUMBER(double s) {
		if (s >= 90.0) {
	return "A";
}
 else if (s >= 80.0) {
	return "B";
}
 else if (s >= 70.0) {
	return "C";
}
 else  {
	return "Review";
}
}
}
