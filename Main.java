public class Main {
	public static void main(String[] args) {
		renderTrueColorMandelbrot();
	return;
}
public static int wave(int t) {
	int x = t % 200;
		if (x < 0) {
		x = x + 200;
}

		if (x < 100) {
	return (x * (100 - x)) / 25;
}

	int y = x - 100;
	return -(y * (100 - y)) / 25;
}
public static int mandelIter(int cx, int cy) {
	int zr = 0;
	int zi = 0;
	int iter = 0;
	int maxIter = 200;
		while (iter < maxIter && ((zr * zr + zi * zi) / 10000) <= 40000) {
	int temp = ((zr * zr - zi * zi) / 10000) + cx;
		zi = ((2 * zr * zi) / 10000) + cy;
		zr = temp;
		iter = iter + 1;
}
	return iter;
}
public static String rgbValues(int iter) {
		if (iter == 200) {
	return "0;0;0";
}

	int scaledIter = (iter * 2);
	int r = 127 + (wave(scaledIter + 0) * 127) / 100;
	int g = 127 + (wave(scaledIter + 66) * 127) / 100;
	int b = 127 + (wave(scaledIter + 133) * 127) / 100;
		if (r < 0) {
}

		if (r > 255) {
}

		if (g < 0) {
}

		if (g > 255) {
}

		if (b < 0) {
}

		if (b > 255) {
}

	return "" + r + ";" + g + ";" + b;
}
public static void renderTrueColorMandelbrot() {
	int y = -11500;
	int stepX = 90;
	int stepY = 90;
		while (y <= 11500) {
	String row = "";
	int x = -21000;
		while (x <= 8000) {
	int iterTop = mandelIter(x, y);
	int iterBot = mandelIter(x, y + stepY);
	String topRgb = rgbValues(iterTop);
	String botRgb = rgbValues(iterBot);
		row = row + "\u001B[38;2;" + topRgb + "m\u001B[48;2;" + botRgb + "m▀";
		x = x + stepX;
}
		row = row + "\u001B[0m";
		System.out.println(row);
		y = y + (stepY * 2);
}
	return;
}
}
