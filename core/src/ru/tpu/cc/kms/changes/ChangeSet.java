package ru.tpu.cc.kms.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A set of atomic changes
 * @author I
 */
public class ChangeSet<T extends Comparable<? super T>> extends LinkedHashSet<Change<T>> {

	private static final long serialVersionUID = 1L;

	public ChangeSet() {
	}

	public ChangeSet(final Collection<Change<T>> c) {
		this.addAll(c);
	}

    public ChangeSet(final Set<T> s1, final Set<T> s2) {
        LinkedHashSet<T> removed = new LinkedHashSet<T>(s1);
        LinkedHashSet<T> added = new LinkedHashSet<T>(s2);
        removed.removeAll(s2);
        added.removeAll(s1);
    	for (T i : removed)
    		this.add(new Change<T>(i, Op.REMOVE));
    	for (T i : added)
    		this.add(new Change<T>(i, Op.ADD));
    }

	public final ChangeSet<T> ordered() {
		ArrayList<Change<T>> list = new ArrayList<Change<T>>(this);
		Collections.sort(list);
		return new ChangeSet<T>(list);
	}

	public final ChangeSet<T> getIntersectionWith(final ChangeSet<T> cs) {
		ChangeSet<T> r = new ChangeSet<T>(this);
		r.retainAll(cs);
		return r;
	}

	public final Collection<T> getAdditions() {
		Collection<T> additions = new LinkedHashSet<T>();
		for (Change<T> c : this)
			if (c.getOp() == Op.ADD)
				additions.add(c.getItem());
		return additions;
	}

	public final Collection<T> getRemovals() {
		Collection<T> removals = new LinkedHashSet<T>();
		for (Change<T> c : this)
			if (c.getOp() == Op.REMOVE)
				removals.add(c.getItem());
		return removals;
	}
}
