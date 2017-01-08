import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Boon {
	
	protected List<Integer> stacks = new ArrayList<Integer>();
	protected int max_stacks;
	private String type;
	
	public Boon(int max_stacks, String type) {	
		this.max_stacks = max_stacks;
		this.type = type;
		clear();
	}
	
	// Abstract Methods
	abstract void update(int time_passed);
	
	// Public Methods
	public void add(int duration) {
		
		// Find an empty slot
		for (int i = 0; i < stacks.size(); i++) {
			if (stacks.get(i) == 0) {
				stacks.set(i, Integer.valueOf(duration));
				sort();
				return;
			}
		}
		
		// No empty slots so replace the lowest
		Collections.sort(stacks);
		if (stacks.get(0) < duration) {
			stacks.set(0, Integer.valueOf(duration));
		}
		sort();
		return;
		
	}
	
	// Protected Methods
	protected void sort() {
		Collections.sort(stacks, Collections.reverseOrder());
	}
	
	protected void clear() {
		stacks.clear();
		for (int i = 0; i < max_stacks; i++) {
			this.stacks.add(Integer.valueOf(0));
		}
	}
	
	// Getters
	public String get_type() {
		return this.type;
	}
	
	public int get_stack_duration() {
		return stacks.stream().mapToInt(Integer::intValue).sum();
	}
	
	public int get_stack_count() {
		int stack_count = 0;
		for (Integer i : stacks) {
//			System.out.println(i);
			if (i != 0) {
				stack_count++;
			}
		}
//		System.out.println(stack_count);
//		System.exit(0);
		return stack_count;
	}
	
	
	
	

}