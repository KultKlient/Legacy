package kult.klient.utils.misc;

public class Vec4 {
    public double x, y, z, w;

    public void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void toScreen() {
        double newW = 1.0 / w * 0.5;

        x = x * newW + 0.5;
        y = y * newW + 0.5;
        z = z * newW + 0.5;
        w = newW;
    }
}
