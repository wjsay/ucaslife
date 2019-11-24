func twoSum(nums []int, target int) []int {
	var mymap = make(map[int]int, 0)
	ret := make([]int, 0)
	for idx, val := range nums {
		i, exist := mymap[val]
		if !exist {
			mymap[val] = idx
		}
		i, exist = mymap[target-val]
		if exist {
			if i != idx {
				ret = append(ret, i)
				ret = append(ret, idx)
				return ret
			}
		}
	}
	return ret
}
