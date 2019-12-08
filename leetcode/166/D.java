import java.util.LinkedList;
import java.util.Queue;

class D {
    public static void main(String[] args) {
        D obj = new D();
        int[][] mat = {{1,0,0},{1,0,0}};
        int ret = obj.minFlips(mat);
        System.out.println(ret);
    }

    public int minFlips(int[][] mat) {
        int n = mat.length;
        int m = mat[0].length;
        int[] vis = new int[1 << n*m];
        // 将状态转化为数字
        int state = 0;
        for (int i = 0; i < mat.length; ++i) {
            for (int j = 0; j < mat[0].length; ++j) {
                state |= mat[i][j] << i * mat[0].length + j;
            }
        }
        if (state == 0) return 0;
        Queue<Integer> que = new LinkedList<Integer>();
        que.offer(state);
        vis[state] = 1;
        while (!que.isEmpty()) {
            state = que.poll();
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < m; ++j) {
                    int state_ = state ^ (1 << i*m+j);
                    if (i - 1 >= 0) state_ ^= 1 << (i-1)*m+j;
                    if (i + 1 < n) state_ ^= 1 << (i+1)*m+j;
                    if (j - 1 >= 0) state_ ^= 1 << i*m+j-1;
                    if (j + 1 < m) state_ ^= 1 << i*m+j+1;
                    if (state_ == 0) return vis[state];
                    if (vis[state_] == 0) {
                        vis[state_] = vis[state] + 1;
                        que.offer(state_);
                    }
                }
            }
        }
        return -1;
    }



}