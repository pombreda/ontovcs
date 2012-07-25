package ru.tpu.cc.kms.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

class ChangesByEntity extends HashMap<OWLEntity, ChangeSet<Statement>> {
    private static final long serialVersionUID = 1L;

    public final void addChange(OWLEntity e, Change<Statement> c) {
        ChangeSet<Statement> changes = this.get(e);
        if (changes == null) {
            changes = new ChangeSet<Statement>();
            this.put(e, changes);
        }
        changes.add(c);
    }
};

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

    private ChangesByEntity changesByEntity = new ChangesByEntity();

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

    public List<OWLEntity> getEntities() {
        List<OWLEntity> entities = new ArrayList<OWLEntity>();
        entities.addAll(removedEntities);
        entities.addAll(newEntities);
        entities.addAll(modifiedEntities);
        return entities;
    }

    public Map<String, String> getPrefixes() {
        ComparableOntology parent = changeSet.getParent();
        ComparableOntology child = changeSet.getChild();
        Set<Statement> prefixStatements = new HashSet<Statement>();
        prefixStatements.addAll(parent.getStatementsByType(StatementType.PREFIX));
        prefixStatements.addAll(child.getStatementsByType(StatementType.PREFIX));
        Map<String, String> prefixes = new HashMap<String, String>();
        for (OWLEntity e : getEntities()) {
            String start = e.getIRI().getStart();
            for (Statement s : prefixStatements) {
                String namespace = ((NamespacePrefixStatement)s).getNamespace();
                if (namespace.equals(start)) {
                    String prefix = ((NamespacePrefixStatement)s).getPrefix();
                    prefixes.put(prefix, namespace);
                }
            }
        }
        return prefixes;
    }

    public ChangesSummary(final CategorizedChangeSet cs)
            throws OWLOntologyCreationException {
        this.changeSet = cs;
        // Format
        if (cs.getChangesByType(StatementType.FORMAT).size() == 2) {
            Collection<Statement> f = cs.getChangesByType(StatementType.FORMAT)
                    .getAddedItems();
            newFormat = ((OntologyFormatStatement) f.toArray()[0]).getFormat();
        }
        // IDs
        if (cs.getChangesByType(StatementType.OIRI).size() == 2) {
            Collection<Statement> i = cs.getChangesByType(StatementType.OIRI)
                    .getAddedItems();
            newOntologyIRI = ((OntologyIRIStatement) i.toArray()[0]).getIRI();
        }
        if (cs.getChangesByType(StatementType.VIRI).size() == 2) {
            Collection<Statement> i = cs.getChangesByType(StatementType.VIRI)
                    .getAddedItems();
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
        for (Statement s : cs.getChangesByType(StatementType.IMPORT)
                .getAddedItems())
            newImports.add(((ImportStatement) s).getImport());
        for (Statement s : cs.getChangesByType(StatementType.IMPORT)
                .getRemovedItems())
            removedImports.add(((ImportStatement) s).getImport());
        // Entities
        Collection<OWLEntity> newEntitiesSet = new LinkedHashSet<OWLEntity>();
        Collection<OWLEntity> modifiedEntitiesSet = new LinkedHashSet<OWLEntity>();
        Collection<OWLEntity> removedEntitiesSet = new LinkedHashSet<OWLEntity>();
        for (Change<Statement> c : cs.getChangesByType(StatementType.AXIOM)) {
            OWLAxiom a = ((AxiomStatement) c.getItem()).getAxiom();
            if (c.getOp() == Op.ADD)
                for (OWLEntity e : a.getSignature()) {
                    newEntitiesSet.add(e);
                    changesByEntity.addChange(e, c);
                }
            else
                for (OWLEntity e : a.getSignature()) {
                    removedEntitiesSet.add(e);
                    changesByEntity.addChange(e, c);
                }
        }
        for (OWLEntity e : newEntitiesSet)
            if (cs.getParent().getOntology()
                    .containsEntityInSignature(e, false))
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

    public ChangeSet<Statement> getChangesByEntity(OWLEntity e) {
        return changesByEntity.get(e);
    }
}
