package ru.tpu.cc.kms.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ru.tpu.cc.kms.statements.AxiomStatement;
import ru.tpu.cc.kms.statements.ImportStatement;
import ru.tpu.cc.kms.statements.NamespacePrefixStatement;
import ru.tpu.cc.kms.statements.OntologyFormatStatement;
import ru.tpu.cc.kms.statements.OntologyIRIStatement;
import ru.tpu.cc.kms.statements.VersionIRIStatement;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.StatementType;

/**
 * @author I
 *
 */
public class ChangesSummary {

	private CategorizedChangeSet changeSet;

	private String newFormat;
	private IRI newOntologyIRI;
	private IRI newVersionIRI;

	private Collection<String> newPrefixes = new LinkedHashSet<String>();
	private Collection<String> removedPrefixes = new LinkedHashSet<String>();
	private Collection<String> modifiedPrefixes = new LinkedHashSet<String>();

	private Collection<OWLImportsDeclaration> newImports = new LinkedHashSet<OWLImportsDeclaration>();
	private Collection<OWLImportsDeclaration> removedImports = new LinkedHashSet<OWLImportsDeclaration>();

	private List<OWLEntity> newEntities = new ArrayList<OWLEntity>();
	private List<OWLEntity> removedEntities = new ArrayList<OWLEntity>();
	private List<OWLEntity> modifiedEntities = new ArrayList<OWLEntity>();

	public CategorizedChangeSet getChangeSet() {
		return changeSet;
	}

	public String getNewFormat() {
		return newFormat;
	}

	public IRI getNewOntologyIRI() {
		return newOntologyIRI;
	}

	public IRI getNewVersionIRI() {
		return newVersionIRI;
	}

	public List<OWLEntity> getNewEntities() {
		return newEntities;
	}

	public List<OWLEntity> getRemovedEntities() {
		return removedEntities;
	}

	public List<OWLEntity> getModifiedEntities() {
		return modifiedEntities;
	}

	public ChangesSummary(final CategorizedChangeSet cs)
			throws OWLOntologyCreationException {
		this.changeSet = cs;
		// Format
		if (cs.getChangesByType(StatementType.FORMAT).size() == 2) {
			Collection<Statement> f = cs.getChangesByType(StatementType.FORMAT).getAddedItems();
			newFormat = ((OntologyFormatStatement) f.toArray()[0]).getFormat();
		}
		// IDs
		if (cs.getChangesByType(StatementType.OIRI).size() == 2) {
			Collection<Statement> i = cs.getChangesByType(StatementType.OIRI).getAddedItems();
			newOntologyIRI = ((OntologyIRIStatement) i.toArray()[0]).getIRI();
		}
		if (cs.getChangesByType(StatementType.VIRI).size() == 2) {
			Collection<Statement> i = cs.getChangesByType(StatementType.VIRI).getAddedItems();
			newVersionIRI = ((VersionIRIStatement) i.toArray()[0]).getIRI();
		}
		// Prefixes
		for (Statement s : cs.getChangesByType(StatementType.PREFIX).getAddedItems())
			newPrefixes.add(((NamespacePrefixStatement) s).getPrefix());
		for (Statement s : cs.getChangesByType(StatementType.PREFIX).getRemovedItems())
			removedPrefixes.add(((NamespacePrefixStatement) s).getPrefix());
		modifiedPrefixes.addAll(newPrefixes);
		modifiedPrefixes.retainAll(removedPrefixes);
		newPrefixes.removeAll(modifiedPrefixes);
		removedPrefixes.removeAll(modifiedPrefixes);
		// Imports
		for (Statement s : cs.getChangesByType(StatementType.IMPORT).getAddedItems())
			newImports.add(((ImportStatement) s).getImport());
		for (Statement s : cs.getChangesByType(StatementType.IMPORT).getRemovedItems())
			removedImports.add(((ImportStatement) s).getImport());
		// Entities
		Collection<OWLEntity> newEntitiesSet =
				new LinkedHashSet<OWLEntity>();
		Collection<OWLEntity> modifiedEntitiesSet =
				new LinkedHashSet<OWLEntity>();
		Collection<OWLEntity> removedEntitiesSet =
				new LinkedHashSet<OWLEntity>();
		for (Change<Statement> c : cs.getChangesByType(StatementType.AXIOM)) {
			OWLAxiom a = ((AxiomStatement) c.getItem()).getAxiom();
			if (c.getOp() == Op.ADD)
				for (OWLEntity e : a.getSignature())
					newEntitiesSet.add(e);
			else
				for (OWLEntity e : a.getSignature())
					removedEntitiesSet.add(e);
		}
		for (OWLEntity e : newEntitiesSet)
			if (cs.getParent().getOntology().containsEntityInSignature(e, false))
				modifiedEntitiesSet.add(e);
		newEntitiesSet.removeAll(modifiedEntitiesSet);
		for (OWLEntity e : removedEntitiesSet)
			if (cs.getChild().getOntology().containsEntityInSignature(e, false))
				modifiedEntitiesSet.add(e);
		removedEntitiesSet.removeAll(modifiedEntitiesSet);

		newEntities = new ArrayList<OWLEntity>(newEntitiesSet);
		Collections.sort(newEntities);
		modifiedEntities = new ArrayList<OWLEntity>(modifiedEntitiesSet);
		Collections.sort(modifiedEntities);
		removedEntities = new ArrayList<OWLEntity>(removedEntitiesSet);
		Collections.sort(removedEntities);
	}
}





















