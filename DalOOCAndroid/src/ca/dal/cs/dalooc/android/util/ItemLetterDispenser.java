package ca.dal.cs.dalooc.android.util;

public class ItemLetterDispenser {
	
	private static final char[] items = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	private int lastItemTaken;
	
	public ItemLetterDispenser() {
		this.lastItemTaken = -1;
	}
	
	public char getNextItem() {
		if (this.lastItemTaken++ < ItemLetterDispenser.items.length) {
			return ItemLetterDispenser.items[this.lastItemTaken];
		}
		return '\0';
	}
	
	public char getLateItemTaken() {
		return ItemLetterDispenser.items[this.lastItemTaken];
	}
	
	public int getLastItemNumberTaken() {
		return this.lastItemTaken;
	}

	public static char getLetterInPosition(int position) {
		return items[position];
	}
}
