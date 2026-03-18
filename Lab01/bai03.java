import java.util.*;

public class bai03 {

    static long cross(long[] O, long[] A, long[] B) {
        return (A[0] - O[0]) * (B[1] - O[1]) - (A[1] - O[1]) * (B[0] - O[0]);
    }

    static List<long[]> grahamScan(long[][] points) {
        int n = points.length;
        if (n < 3) return new ArrayList<>(Arrays.asList(points));

        // Tìm điểm pivot: y nhỏ nhất, nếu bằng thì x nhỏ nhất
        int pivotIdx = 0;
        for (int i = 1; i < n; i++) {
            if (points[i][1] < points[pivotIdx][1]
                    || (points[i][1] == points[pivotIdx][1] && points[i][0] < points[pivotIdx][0])) {
                pivotIdx = i;
            }
        }

        // Đưa pivot về đầu mảng
        long[] tmp = points[0];
        points[0] = points[pivotIdx];
        points[pivotIdx] = tmp;

        final long[] pivot = points[0];

        // Sắp xếp theo góc cực so với pivot
        long[][] rest = Arrays.copyOfRange(points, 1, n);
        Arrays.sort(rest, (a, b) -> {
            long cp = cross(pivot, a, b);
            if (cp != 0) return (cp > 0) ? -1 : 1;
            // Cùng góc: lấy điểm xa hơn
            long da = (a[0]-pivot[0])*(a[0]-pivot[0]) + (a[1]-pivot[1])*(a[1]-pivot[1]);
            long db = (b[0]-pivot[0])*(b[0]-pivot[0]) + (b[1]-pivot[1])*(b[1]-pivot[1]);
            return Long.compare(da, db);
        });

        // Xây dựng convex hull
        Deque<long[]> stack = new ArrayDeque<>();
        stack.push(pivot);
        stack.push(rest[0]);

        for (int i = 1; i < rest.length; i++) {
            while (stack.size() >= 2) {
                long[] top = stack.peek();
                long[] second = ((ArrayDeque<long[]>) stack).peekLast();
                // Xem phần tử thứ 2 từ đỉnh
                Iterator<long[]> it = stack.iterator();
                top = it.next();
                second = it.next();

                if (cross(second, top, rest[i]) <= 0) {
                    stack.pop();
                } else break;
            }
            stack.push(rest[i]);
        }

        return new ArrayList<>(stack);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        long[][] points = new long[n][2];
        for (int i = 0; i < n; i++) {
            points[i][0] = sc.nextLong();
            points[i][1] = sc.nextLong();
        }

        List<long[]> hull = grahamScan(points);

        System.out.println("Các trạm cảnh báo:");
        for (long[] p : hull) {
            System.out.println(p[0] + " " + p[1]);
        }
    }
}