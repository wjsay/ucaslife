import java.util.HashMap;
class Solution {
    public int romanToInt(String s) {
        if (s == null || s.length() == 0) return 0;
        HashMap<Character, Integer> map = new HashMap<>();
        map.put(Character.valueOf('I'), 1);
        map.put(Character.valueOf('V'), 5);
        map.put('X', 10);
        map.put('L', 50);
        map.put('C', 100);
        map.put('D', 500);
        map.put('M', 1000);
        int ret = map.get(s.charAt(s.length()-1));
        int pre = ret;
        for (int i = s.length() - 2; i >= 0; --i) {
            int cur = map.get(s.charAt(i));
            ret += (cur < pre ? -cur : cur);
            pre = cur;
        }
        return ret;
    }
}