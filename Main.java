public class Main {
public static double clamp(double v, double lo, double hi) {
		if (((Number)(v)).doubleValue()<((Number)lo).doubleValue()) {
	return lo;
}
 else if (((Number)(v)).doubleValue()>((Number)hi).doubleValue()) {
	return hi;
}
 else  {
	return v;
}
}
public static double maxVal(double a, double b) {
		if (((Number)(a)).doubleValue()>((Number)b).doubleValue()) {
	return a;
}
 else  {
	return b;
}
}
public static double minVal(double a, double b) {
		if (((Number)(a)).doubleValue()<((Number)b).doubleValue()) {
	return a;
}
 else  {
	return b;
}
}
public static double absVal(double x) {
		if (x < 0.0) {
	return ((double)ChppMath.negate(x));
}
 else  {
	return x;
}
}
public static double length2(double x, double y) {
	return Math.pow(((Number)(Math.pow(((Number)x).doubleValue(), ((Number)2).doubleValue()) + Math.pow(((Number)y).doubleValue(), ((Number)2).doubleValue()))).doubleValue(), ((Number)0.5).doubleValue());
}
public static double length3(double x, double y, double z) {
	return Math.pow(((Number)(Math.pow(((Number)x).doubleValue(), ((Number)2).doubleValue()) + Math.pow(((Number)y).doubleValue(), ((Number)2).doubleValue()) + Math.pow(((Number)z).doubleValue(), ((Number)2).doubleValue()))).doubleValue(), ((Number)0.5).doubleValue());
}
public static double sdTorus(double px, double py, double pz) {
	double x = px;
	double y = ((double)ChppMath.sub(py, 0.25));
	double z = ((double)ChppMath.sub(pz, 3.0));
	double c1 = Math.cos(((Number)(0.56)).doubleValue());
	double s1 = Math.sin(((Number)(0.56)).doubleValue());
	double ry = ((double)ChppMath.sub(((double)ChppMath.mult(y, c1)), ((double)ChppMath.mult(z, s1))));
	Object rz = ((Object)ChppMath.add(((Object)ChppMath.mult(y, s1)), ((Object)ChppMath.mult(z, c1))));
	double c2 = Math.cos(((Number)(0.35)).doubleValue());
	double s2 = Math.sin(((Number)(0.35)).doubleValue());
	double fx = ((double)ChppMath.sub(((double)ChppMath.mult(x, c2)), ((double)ChppMath.mult(ry, s2))));
	Object fy = ((Object)ChppMath.add(((Object)ChppMath.mult(x, s2)), ((Object)ChppMath.mult(ry, c2))));
	double qx = length2(fx, rz) - 0.85;
	return length2(qx, fy) - 0.3;
}
public static Object sdFloor(Object px, Object py, Object pz) {
	return ((Object)ChppMath.add(py, 0.85));
}
public static double mapScene(double px, double py, double pz) {
	return minVal(sdTorus(px, py, pz), ((double) sdFloor(px, py, pz)));
}
public static double march(Object roX, Object roY, Object roZ, double rdX, double rdY, double rdZ) {
	double t = 0.0;
	int steps = 0;
		while (steps < 72) {
	Object px = ((Object)ChppMath.add(roX, ((Object)ChppMath.mult(rdX, t))));
	Object py = ((Object)ChppMath.add(roY, ((Object)ChppMath.mult(rdY, t))));
	Object pz = ((Object)ChppMath.add(roZ, ((Object)ChppMath.mult(rdZ, t))));
	double d = mapScene(px, py, pz);
		if (d < 0.0015) {
	return t;
}

		t = t + d;
		if (t > 20.0) {
	return ((double)ChppMath.negate(1.0));
}

		steps = steps + 1;
}
	return ((double)ChppMath.negate(1.0));
}
public static double calcShadow(Object roX, Object roY, Object roZ, double rdX, double rdY, double rdZ) {
	double res = 1.0;
	double t = 0.03;
	int steps = 0;
		while (steps < 28) {
	double h = mapScene(((Object)ChppMath.add(roX, ((Object)ChppMath.mult(rdX, t)))), ((Object)ChppMath.add(roY, ((Object)ChppMath.mult(rdY, t)))), ((Object)ChppMath.add(roZ, ((Object)ChppMath.mult(rdZ, t)))));
		if (h < 0.001) {
	return 0.12;
}

		res = minVal(res, (12.0 * h) / t);
		t = t + clamp(h, 0.02, 0.35);
		steps = steps + 1;
}
	return clamp(res, 0.12, 1.0);
}
public static double shadeHit(double px, double py, double pz, double rdX, double rdY, double rdZ) {
	double eps = 0.0015;
	double nx = mapScene(((Object)ChppMath.add(px, eps)), py, pz) - mapScene(((double)ChppMath.sub(px, eps)), py, pz);
	double ny = mapScene(px, ((Object)ChppMath.add(py, eps)), pz) - mapScene(px, ((double)ChppMath.sub(py, eps)), pz);
	double nz = mapScene(px, py, ((Object)ChppMath.add(pz, eps))) - mapScene(px, py, ((double)ChppMath.sub(pz, eps)));
	double nLen = length3(nx, ny, nz);
		nx = nx / nLen;
		ny = ny / nLen;
		nz = nz / nLen;
	double l1x = ((double)ChppMath.negate(0.577));
	double l1y = 0.707;
	double l1z = ((double)ChppMath.negate(0.408));
	double l2x = 0.65;
	double l2y = 0.45;
	double l2z = 0.61;
	double diff1 = maxVal(nx * l1x + ny * l1y + nz * l1z, 0.0);
	double diff2 = maxVal(nx * l2x + ny * l2y + nz * l2z, 0.0);
	double hx = ((double)ChppMath.sub(l1x, rdX));
	double hy = ((double)ChppMath.sub(l1y, rdY));
	double hz = ((double)ChppMath.sub(l1z, rdZ));
	double hLen = length3(hx, hy, hz);
	double spec = Math.pow(((Number)maxVal(((double)ChppMath.div((((double)ChppMath.add(((Object)ChppMath.add(((Object)ChppMath.mult(nx, hx)), ((Object)ChppMath.mult(ny, hy)))), ((Object)ChppMath.mult(nz, hz))))), hLen)), 0.0)).doubleValue(), ((Number)32).doubleValue());
	double ndotv = clamp(((double)ChppMath.negate((((double)ChppMath.add(((Object)ChppMath.add(((Object)ChppMath.mult(nx, rdX)), ((Object)ChppMath.mult(ny, rdY)))), ((Object)ChppMath.mult(nz, rdZ))))))), 0.0, 1.0);
	double fresnel = Math.pow(((Number)(1.0 - ndotv)).doubleValue(), ((Number)3).doubleValue());
	double shadow = calcShadow(((Object)ChppMath.add(px, nx * 0.01)), ((Object)ChppMath.add(py, ny * 0.01)), ((Object)ChppMath.add(pz, nz * 0.01)), l1x, l1y, l1z);
	double r = 0.0;
	double g = 0.0;
	double b = 0.0;
		if (((Number)(sdTorus(px, py, pz))).doubleValue()<((Number)((double) sdFloor(px, py, pz))).doubleValue()) {
	double band = 0.5 + 0.5 * Math.sin(((Number)(((Object)ChppMath.add(((Object)ChppMath.add(((Object)ChppMath.mult(px, 5.0)), ((Object)ChppMath.mult(py, 4.0)))), ((Object)ChppMath.mult(pz, 3.0)))))).doubleValue());
	double baseR = 210.0 * band + 40.0 * (1.0 - band);
	double baseG = 55.0 * band + 215.0 * (1.0 - band);
	double baseB = 195.0 * band + 250.0 * (1.0 - band);
		r = baseR * (0.2 + diff1 * 0.85 * shadow) + 40.0 * diff2 + 255.0 * spec * shadow + 140.0 * fresnel;
		g = baseG * (0.2 + diff1 * 0.85 * shadow) + 160.0 * diff2 + 255.0 * spec * shadow + 210.0 * fresnel;
		b = baseB * (0.2 + diff1 * 0.85 * shadow) + 230.0 * diff2 + 255.0 * spec * shadow + 255.0 * fresnel;
}
 else  {
	double check = Math.sin(((Number)(((double)ChppMath.mult(((double)ChppMath.mult(px, Math.PI)), 2.0)))).doubleValue()) * Math.sin(((Number)(((double)ChppMath.mult(((double)ChppMath.mult(pz, Math.PI)), 2.0)))).doubleValue());
	double tile = 0.22;
		if (check > 0.0) {
		tile = 0.85;
}

	double fade = clamp(((double)ChppMath.sub(1.0, (((double)ChppMath.div(pz, 16.0))))), 0.0, 1.0);
	double light = (0.22 + diff1 * 0.85 * shadow) * fade;
		r = (35.0 + 175.0 * tile) * light + 18.0 * (1.0 - fade);
		g = (45.0 + 185.0 * tile) * light + 22.0 * (1.0 - fade);
		b = (75.0 + 180.0 * tile) * light + 48.0 * (1.0 - fade);
}
	int ir = 0;
	int ig = 0;
	int ib = 0;
	double cr = clamp(r, 0.0, 255.0);
	double cg = clamp(g, 0.0, 255.0);
	double cb = clamp(b, 0.0, 255.0);
		while (ir + 1 <= cr) {
		ir = ir + 1;
}
		while (ig + 1 <= cg) {
		ig = ig + 1;
}
		while (ib + 1 <= cb) {
		ib = ib + 1;
}
	return (ir * 65536.0) + (ig * 256.0) + ib;
}
public static double skyColor(double v) {
	double t = clamp(((double)ChppMath.add(((Object)ChppMath.mult(v, 0.9)), 0.35)), 0.0, 1.0);
	double sr = 14.0 + 38.0 * (1.0 - t);
	double sg = 18.0 + 55.0 * (1.0 - t);
	double sb = 46.0 + 125.0 * (1.0 - t);
	int ir = 0;
	int ig = 0;
	int ib = 0;
		while (ir + 1 <= sr) {
		ir = ir + 1;
}
		while (ig + 1 <= sg) {
		ig = ig + 1;
}
		while (ib + 1 <= sb) {
		ib = ib + 1;
}
	return (ir * 65536.0) + (ig * 256.0) + ib;
}
public static String unpackRgb(double packed) {
	double r = ((double)ChppMath.div((((double)ChppMath.sub(packed, (((Number)(packed)).doubleValue()%((Number)65536.0).doubleValue())))), 65536.0));
	double rem = ((Number)(packed)).doubleValue()%((Number)65536.0).doubleValue();
	double g = ((double)ChppMath.div((((double)ChppMath.sub(rem, (((Number)(rem)).doubleValue()%((Number)256.0).doubleValue())))), 256.0));
	double b = ((Number)(rem)).doubleValue()%((Number)256.0).doubleValue();
	int ir = 0;
	int ig = 0;
	int ib = 0;
		while (((Number)(ir + 1)).doubleValue()<=((Number)r).doubleValue()) {
		ir = ir + 1;
}
		while (((Number)(ig + 1)).doubleValue()<=((Number)g).doubleValue()) {
		ig = ig + 1;
}
		while (((Number)(ib + 1)).doubleValue()<=((Number)b).doubleValue()) {
		ib = ib + 1;
}
	return "" + ir + ";" + ig + ";" + ib;
}
public static String tracePixel(double u, double v) {
	double len = length3(u, v, 1.45);
	double rdX = ((double)ChppMath.div(u, len));
	double rdY = ((double)ChppMath.div(v, len));
	double rdZ = 1.45 / len;
	double dist = march(0.0, 0.25, 0.0, rdX, rdY, rdZ);
		if (dist < 0.0) {
	return unpackRgb(skyColor(v));
}

	double px = ((double)ChppMath.mult(rdX, dist));
	Object py = ((Object)ChppMath.add(0.25, ((Object)ChppMath.mult(rdY, dist))));
	double pz = rdZ * dist;
	double primaryPacked = shadeHit(px, py, pz, rdX, rdY, rdZ);
	double eps = 0.0015;
	double nx = mapScene(((Object)ChppMath.add(px, eps)), py, pz) - mapScene(((double)ChppMath.sub(px, eps)), py, pz);
	double ny = mapScene(px, ((Object)ChppMath.add(py, eps)), pz) - mapScene(px, ((double)ChppMath.sub(py, eps)), pz);
	double nz = mapScene(px, py, pz + eps) - mapScene(px, py, pz - eps);
	double nLen = length3(nx, ny, nz);
		nx = nx / nLen;
		ny = ny / nLen;
		nz = nz / nLen;
	Object dotDN = ((Object)ChppMath.add(((Object)ChppMath.add(((Object)ChppMath.mult(rdX, nx)), ((Object)ChppMath.mult(rdY, ny)))), rdZ * nz));
	double refX = ((double)ChppMath.sub(rdX, ((double)ChppMath.mult(((double)ChppMath.mult(2.0, dotDN)), nx))));
	double refY = ((double)ChppMath.sub(rdY, ((double)ChppMath.mult(((double)ChppMath.mult(2.0, dotDN)), ny))));
	double refZ = ((double)ChppMath.sub(rdZ, ((double)ChppMath.mult(((double)ChppMath.mult(2.0, dotDN)), nz))));
	double refDist = march(((Object)ChppMath.add(px, nx * 0.015)), ((Object)ChppMath.add(py, ny * 0.015)), pz + nz * 0.015, refX, refY, refZ);
	double refPacked = 0.0;
		if (refDist < 0.0) {
		refPacked = skyColor(refY);
}
 else  {
	Object rpx = ((Object)ChppMath.add(((Object)ChppMath.add(px, nx * 0.015)), ((Object)ChppMath.mult(refX, refDist))));
	Object rpy = ((Object)ChppMath.add(((Object)ChppMath.add(py, ny * 0.015)), ((Object)ChppMath.mult(refY, refDist))));
	Object rpz = ((Object)ChppMath.add(pz + nz * 0.015, ((Object)ChppMath.mult(refZ, refDist))));
		refPacked = shadeHit(rpx, rpy, rpz, refX, refY, refZ);
}
	double reflectivity = 0.28;
		if (((Number)(sdTorus(px, py, pz))).doubleValue()<((Number)((double) sdFloor(px, py, pz))).doubleValue()) {
		reflectivity = 0.45;
}

	double r1 = (primaryPacked - (primaryPacked % 65536.0)) / 65536.0;
	double rem1 = primaryPacked % 65536.0;
	double g1 = (rem1 - (rem1 % 256.0)) / 256.0;
	double b1 = rem1 % 256.0;
	double r2 = (refPacked - (refPacked % 65536.0)) / 65536.0;
	double rem2 = refPacked % 65536.0;
	double g2 = (rem2 - (rem2 % 256.0)) / 256.0;
	double b2 = rem2 % 256.0;
	double finalR = r1 * (1.0 - reflectivity) + r2 * reflectivity;
	double finalG = g1 * (1.0 - reflectivity) + g2 * reflectivity;
	double finalB = b1 * (1.0 - reflectivity) + b2 * reflectivity;
	int ir = 0;
	int ig = 0;
	int ib = 0;
		while (ir + 1 <= finalR) {
		ir = ir + 1;
}
		while (ig + 1 <= finalG) {
		ig = ig + 1;
}
		while (ib + 1 <= finalB) {
		ib = ib + 1;
}
	return "" + ir + ";" + ig + ";" + ib;
}
	public static void main(String[] args) {
	double stepX = 0.015;
	double stepY = 0.015;
	double y = 0.65;
		while (y >= ((double)ChppMath.negate(0.65))) {
	String row = "";
	double x = ((double)ChppMath.negate(1.25));
		while (x <= 1.25) {
	String topRgb = tracePixel(x, y);
	String botRgb = tracePixel(x, y - stepY);
		row = row + "\u001B[38;2;" + topRgb + "m\u001B[48;2;" + botRgb + "m▀";
		x = x + stepX;
}
		row = row + "\u001B[0m";
		System.out.println(row);
		y = y - (stepY * 2.0);
}
	return;
}
}
