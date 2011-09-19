package ru.tpu.cc.kms;

import java.net.URISyntaxException;

import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.ChangeSet;
import ru.tpu.cc.kms.changes.ComparableOntology;
import ru.tpu.cc.kms.statements.Statement;

/**
 * This class is used to classify changes into 3 categories:
 *  - common changes (if it happens to both parties to make same changes);
 *  - conflicting changes (which have links to common ontology elements);
 *  - other changes (non-conflicting).
 * @author I
 *
 */
public class ConflictFinder {

	private ChangeSet<Statement> common;
	private ChangeSet<Statement> conflictsRemote;
	private ChangeSet<Statement> conflictsLocal;
	private ChangeSet<Statement> otherRemote;
	private ChangeSet<Statement> otherLocal;

	public ConflictFinder(final ComparableOntology base, final ComparableOntology remote, final ComparableOntology local) throws URISyntaxException
	{
		ChangeSet<Statement> remoteChanges = new ChangeSet<Statement>(base.getStatements(), remote.getStatements());
		Signature remoteChangesSig = new Signature(remoteChanges);
		ChangeSet<Statement> localChanges = new ChangeSet<Statement>(base.getStatements(), local.getStatements());
		Signature localChangesSig = new Signature(localChanges);
		common = remoteChanges.getIntersectionWith(localChanges);
		localChanges.removeAll(common);
		remoteChanges.removeAll(common);
		conflictsRemote = new ChangeSet<Statement>();
		conflictsLocal = new ChangeSet<Statement>();
		otherRemote = new ChangeSet<Statement>(remoteChanges);
		otherLocal = new ChangeSet<Statement>(localChanges);
		for (Change<Statement> c : remoteChanges) {
			if (!localChangesSig.isIntersectionEmpty(new Signature(c))) {
				conflictsRemote.add(c);
				otherRemote.remove(c);
			}
		}
		for (Change<Statement> c : localChanges) {
			if (!remoteChangesSig.isIntersectionEmpty(new Signature(c))) {
				conflictsLocal.add(c);
				otherLocal.remove(c);
			}
		}
	}

	public ChangeSet<Statement> getCommonChanges() {
		return common;
	}

	public ChangeSet<Statement> getLocalConflicts() {
		return conflictsLocal;
	}

	public ChangeSet<Statement> getRemoteConflicts() {
		return conflictsRemote;
	}

	public ChangeSet<Statement> getLocalNonconflictingChanges() {
		return otherLocal;
	}

	public ChangeSet<Statement> getRemoteNonconflictingChanges() {
		return otherRemote;
	}

	public boolean isConflict() {
		return (conflictsLocal.size() > 0) || (conflictsRemote.size() > 0);
	}

	public int getConflictsCount() {
		return conflictsLocal.size() + conflictsRemote.size();
	}

}
