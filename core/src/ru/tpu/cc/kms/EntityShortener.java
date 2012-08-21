package ru.tpu.cc.kms;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

public class EntityShortener {

    ShortFormProvider provider;

    public EntityShortener(IriFormat iriFormat) {
        super();
        switch (iriFormat) {
        case SIMPLE:
            provider = new SimpleShortFormProvider();
            break;
        case QNAME:
            DefaultPrefixManager prefixManager = new DefaultPrefixManager();
            prefixManager.clear();
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager);
            break;
        case FULL:
            provider = new FullFormProvider();
            break;
        }
    }

    public EntityShortener(IriFormat iriFormat, OWLOntology ontology) {
        this(iriFormat);
        if (iriFormat == IriFormat.QNAME) {
            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(manager, ontology);
        }
    }

    public EntityShortener(IriFormat iriFormat, Map<String, String> prefixMap) {
        this(iriFormat);
        if (iriFormat == IriFormat.QNAME) {
            DefaultPrefixManager prefixManager = new DefaultPrefixManager();
            prefixManager.clear();
            for(Map.Entry<String, String> e : prefixMap.entrySet()) {
                prefixManager.setPrefix(e.getKey(), e.getValue());
            }
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager);
        }
    }

    public String shorten(OWLEntity entity) {
        if (entity == null)
            throw new NullPointerException("Null entity");
        return provider.getShortForm(entity);
    }
}
