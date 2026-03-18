import java.util.Random;

public class bai02 {
    public static void main(String[] args) {
        double r = 1;

        Random rand = new Random();
        long n = 10000000; // Số lượng điểm ngẫu nhiên
        long inCricle = 0;

        for (long i = 0; i < n; i++) {
            double x = (rand.nextDouble() * 2 - 1) * r; // Tọa độ x ngẫu nhiên
            double y = (rand.nextDouble() * 2 - 1) * r; // Tọa độ y ngẫu nhiên

            if (x * x + y * y <= r * r) {
                inCricle++;
            }
        }       
        double dientich = 4.0 * r * r * inCricle / n; // Diện tích hình tròn
        double pi = dientich / (r * r); // Tính giá trị pi
        System.out.println("Gia tri pi: " + pi);
    }
}