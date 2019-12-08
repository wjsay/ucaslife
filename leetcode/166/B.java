import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class B {
    public static void main(String[] args) {
        B obj = new B();
        int[] data = {2,1,3,3,3,2};
        obj.groupThePeople(data);
    }

    public List<List<Integer>> groupThePeople(int[] groupSizes) {
        List<List<Integer>> lists = new ArrayList<List<Integer>>();
        HashMap<Integer, List<Integer>> map = new HashMap();
        for (int i = 0; i < groupSizes.length; ++i) {
            List<Integer> tmp = map.getOrDefault(groupSizes[i], null);
            if (tmp == null) {
                map.put(groupSizes[i], tmp = new ArrayList<>());
            }
            tmp.add(i);
        }
        for (Integer key : map.keySet()) {
            List<Integer> tmp = map.get(key);
            for (int i = tmp.size() / key - 1; i >= 0; --i) {
                List<Integer> group = new ArrayList();
                for (int j = 0; j < key; ++j) {
                    group.add(tmp.get(i * key + j));
                }
                lists.add(group);
            }
        }
        return lists;
    }

}