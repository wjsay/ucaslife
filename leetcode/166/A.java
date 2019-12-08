import java.util.HashMap;

class A {
    public static void main(String[] arsgs) {
        A obj = new A();
        System.out.println(obj.subtractProductAndSum(4421));     
    }
    public int subtractProductAndSum(int n) {
        int product = 1, sum = 0;
        while (n > 0) {
            int tmp = n % 10;
            product *= tmp;
            sum += tmp;
            n /= 10;
        }
        return product - sum;
    }
}