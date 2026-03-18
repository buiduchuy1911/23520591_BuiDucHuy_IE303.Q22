import java.util.Random;
import java.util.Scanner;

public class bai01 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap ban kinh hinh tron: ");
        double r = sc.nextDouble();
        sc.close();

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
        System.out.println("Dien tich hinh tron: " + dientich);
    }
}
