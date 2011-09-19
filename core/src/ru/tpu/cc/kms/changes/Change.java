package ru.tpu.cc.kms.changes;

/**
 * Atomic change
 * @author I
 *
 * @param <T> Type of the item which changes
 */

enum Op { ADD, REMOVE }

public class Change<T extends Comparable<? super T>>
    implements Comparable<Change<T>> {

    private Op operation;
    private T item;

    /**
     *
     * @param item      Item
     * @param operation Operation (false - removal; true - addition)
     */
    public Change(final T item, final Op operation) {
        this.item = item;
        this.operation = operation;
    }
    @Override
    public final String toString() {
        if (operation == Op.ADD)
            return "+ " + item.toString();
        else
            return "- " + item.toString();
    }
    @Override
    public final int compareTo(final Change<T> o) {
        if ((operation == Op.REMOVE) && (o.operation == Op.ADD))
            return -1;
        if ((operation == Op.ADD) && (o.operation == Op.REMOVE))
            return 1;
        if (operation == o.operation) {
            if (item.equals(o.item))
                return 0;
            else
                return item.compareTo(o.item);
        }
        return 0;
    }
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (this.getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        Change<T> other = (Change<T>) obj;
        return (operation == other.operation) && item.equals(other.item);
    }

    @Override
    public final int hashCode() {
        int h = item.hashCode() * 2;
        if (operation == Op.ADD)
            h++;
        return h;
    }
	/**
	 * @return the operation
	 */
	public Op getOp() {
		return operation;
	}
	/**
	 * @return the item
	 */
	public T getItem() {
		return item;
	}
}
