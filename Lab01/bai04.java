import java.util.*;

public class bai04 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = sc.nextInt();

        // dp[i][j] = độ dài lớn nhất của dãy con từ i phần tử đầu có tổng = j
        // -1 nghĩa là trạng thái không thể đạt được
        int[][] dp = new int[n + 1][k + 1];
        for (int[] row : dp) Arrays.fill(row, -1);
        for (int i = 0; i <= n; i++) dp[i][0] = 0; // tổng = 0 luôn đạt được (chọn rỗng)

        boolean[][] take = new boolean[n + 1][k + 1]; // take[i][j] = có lấy phần tử i không?

        for (int i = 1; i <= n; i++) {
            int val = a[i - 1];
            for (int j = 0; j <= k; j++) {

                // Trường hợp 1: không lấy a[i-1]
                dp[i][j] = dp[i - 1][j];
                take[i][j] = false;

                // Trường hợp 2: lấy a[i-1] (nếu đủ điều kiện)
                if (j >= val && dp[i - 1][j - val] != -1) {
                    int candidate = dp[i - 1][j - val] + 1;
                    if (candidate > dp[i][j]) {
                        dp[i][j] = candidate;
                        take[i][j] = true;
                    }
                }
            }
        }

        // Kiểm tra có nghiệm không
        if (dp[n][k] == -1) {
            System.out.println("Không tồn tại dãy con có tổng bằng " + k);
            return;
        }

        // Truy vết để lấy các phần tử
        List<Integer> result = new ArrayList<>();
        int rem = k;
        for (int i = n; i >= 1; i--) {
            if (take[i][rem]) {
                result.add(a[i - 1]);
                rem -= a[i - 1];
            }
        }
        Collections.reverse(result);

        System.out.println("Độ dài dãy con dài nhất: " + dp[n][k]);
        System.out.print("Dãy con: ");
        for (int x : result) System.out.print(x + " ");
        System.out.println();
    }
}