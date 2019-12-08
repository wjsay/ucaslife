class C {
      public static void main(String[] args) {
        C obj = new C();
        int[] nums = {19};
        int res = obj.smallestDivisor(nums, 5);
        System.out.println(res);
    }
  
    public int smallestDivisor(int[] nums, int threshold) {        
        int l = 1, r = 1000000, mid = l + r >> 1;
        while (l < r) {
            long sum = 0;
            for (int val : nums) {
                sum += (val + mid - 1) / mid;
            }
            if (sum > threshold) {
                l = mid + 1;
            } else {
                r = mid;
            }
            mid = l + r >> 1;
        }
        return r;
    }
    
}