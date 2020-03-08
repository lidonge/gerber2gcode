package tools;

import java.util.ArrayList;

public class SortedArray {
	private ArrayList<IComparable> list = new ArrayList<>();

	public IComparable findSame(IComparable e) {
		int beg = 0, end = list.size();
		int half = end /2;
		int compare = 0;
//		System.out.println("adding "+ e );
//		System.out.println("beg "+ beg + ", half:" + half+ ",end:" + end);
		while(half != 0) {
			int curIdx = beg + half;
			IComparable curObj = (IComparable)list.get(curIdx);
			compare = e.compareTo(curObj);
//			System.out.println("beg "+ beg + ", half:" + half + ",end:" + end +", curIdx:" + curIdx +", cur:" + curObj + ", compare:" + compare);

			if(compare == 0) {
				return curObj;
			}else if(compare > 0) {
				beg = curIdx + 1;
			}else {
				end = curIdx;
			}
			half = (end -beg)/2;
		}		
		if(beg != end){
			IComparable curObj = (IComparable)list.get(beg);
			compare = e.compareTo(curObj);
			if(compare == 0) {
				return curObj;
			}
		}
		return null;
	}
	
	public void add(IComparable e) {
		int beg = 0, end = list.size();
		int half = end /2;
		int compare = 0;
//		System.out.println("adding "+ e );
//		System.out.println("beg "+ beg + ", half:" + half+ ",end:" + end);
		while(half != 0) {
			int curIdx = beg + half;
			IComparable curObj = (IComparable)list.get(curIdx);
			compare = e.compareTo(curObj);
//			System.out.println("beg "+ beg + ", half:" + half + ",end:" + end +", curIdx:" + curIdx +", cur:" + curObj + ", compare:" + compare);

			if(compare == 0) {
				beg = curIdx;
				break;
			}else if(compare > 0) {
				beg = curIdx + 1;
			}else {
				end = curIdx;
			}
			half = (end -beg)/2;
		}
		if(beg == end)
			list.add(beg, e);
		else {
			IComparable curObj = (IComparable)list.get(beg);
			compare = e.compareTo(curObj);
			if(compare > 0) {
				list.add(end,e);
			}else {
				list.add(beg,e);
			}
		}
//		System.out.println(list);
//		System.out.println("=========================");
	}
	
	public int size() {
		return list.size();
	}
	
	public IComparable get(int index) {
		return list.get(index);
	}
	
	public IComparable remove(int index) {
		return list.remove(index);
	}
	
	public String toString() {
		return ""+list;
	}
	private static class TestComp implements IComparable{
		public int value;
		public TestComp(int v) {
			this.value = v;
		}
		@Override
		public int compareTo(Object target) {
			return value - ((TestComp)target).value;
		}
		
		public String toString() {
			return ""+value;
		}
		
	}
	private static void testFix(SortedArray test) {
		int[] org = new int[] {908, 60, 795, 825, 925, 656, 217, 237, 146, 115};
		for(int i = 0;i<org.length;i++) {
			int v = org[i];
//			cmps[i] =new TestComp(((int)Math.random())*1000);
			test.add(new TestComp(v));
		}	
	}
	private static void testDyn(SortedArray test) {
		ArrayList<Integer> org = new ArrayList<>();
//		TestComp[] cmps = new TestComp[10];
		for(int i = 0;i<10;i++) {
			int v = (int)(Math.random()*1000);
//			cmps[i] =new TestComp(((int)Math.random())*1000);
			test.add(new TestComp(v));
			org.add(v); 
		}
		System.out.println("===org:" +org);
	}
	public static final void main(String[] args) {
		SortedArray test = new SortedArray();
//		testFix(test);
		testDyn(test);
		System.out.println(test);
	}
}
