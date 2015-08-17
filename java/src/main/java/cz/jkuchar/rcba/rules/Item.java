package cz.jkuchar.rcba.rules;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Item {

	private Map<String, String> memory;
	private long id;

	public Item() {
		this(0);
	}

	public Item(int id) {
		this.memory = new HashMap<String, String>();
		this.id = id;
	}

	public boolean containsKey(String key) {
		return this.memory.containsKey(key);
	}

	public String get(String key) {
		return this.memory.get(key);
	}

	public void put(String key, String value) {
		this.memory.put(key, value);
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Item [memory=" + memory + "]";
	}
	

}
