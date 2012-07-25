package ru.tpu.cc.kms.statements.render;

import java.io.StringWriter;
import java.util.Collections;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import ru.tpu.cc.kms.FullFormProvider;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.statements.AxiomStatement;
import ru.tpu.cc.kms.statements.ImportStatement;
import ru.tpu.cc.kms.statements.NamespacePrefixStatement;
import ru.tpu.cc.kms.statements.OntologyFormatStatement;
import ru.tpu.cc.kms.statements.OntologyIRIStatement;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.VersionIRIStatement;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxFrameRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.SectionMap;

public class ManchesterSyntaxStatementRenderer extends StatementRenderer {

    public ManchesterSyntaxStatementRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    @Override
    public String getRendering(Statement statement) throws OWLOntologyCreationException, OWLRendererException {
        String r = "";
        switch (statement.getType()) {
            case AXIOM:
                DefaultPrefixManager prefixManager = new DefaultPrefixManager();
                prefixManager.clear();
                PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) new ManchesterOWLSyntaxOntologyFormat();
                ShortFormProvider provider = null;
                switch (iriFormat) {
                case SIMPLE:
                    provider = new SimpleShortFormProvider();
                    break;
                case QNAME:
                    provider = new QNameShortFormProvider(prefixFormat.getPrefixName2PrefixMap());
                    break;
                case FULL:
                    provider = new FullFormProvider();
                    break;
                }
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                OWLOntology o = m.createOntology();
                OWLAxiom a = ((AxiomStatement) statement).getAxiom();
                m.addAxiom(o, a);
                StringWriter w = new StringWriter();
                ManchesterOWLSyntaxFrameRenderer sr = new ManchesterOWLSyntaxFrameRenderer(m, o, w, provider);

                if (a.getAxiomType() == AxiomType.DISJOINT_CLASSES) {
                    OWLDisjointClassesAxiom ax = (OWLDisjointClassesAxiom) a;
                    if (ax.getClassExpressions().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getClassExpressions(), ax);
                        sr.writeSection(ManchesterOWLSyntax.DISJOINT_CLASSES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
                    OWLEquivalentClassesAxiom ax = (OWLEquivalentClassesAxiom) a;
                    if (ax.getClassExpressions().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getClassExpressions(), ax);
                        sr.writeSection(ManchesterOWLSyntax.EQUIVALENT_CLASSES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.DISJOINT_OBJECT_PROPERTIES) {
                    OWLDisjointObjectPropertiesAxiom ax = (OWLDisjointObjectPropertiesAxiom) a;
                    if (ax.getProperties().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getProperties(), ax);
                        sr.writeSection(ManchesterOWLSyntax.DISJOINT_PROPERTIES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.EQUIVALENT_OBJECT_PROPERTIES) {
                    OWLEquivalentObjectPropertiesAxiom ax = (OWLEquivalentObjectPropertiesAxiom) a;
                    if (ax.getProperties().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getProperties(), ax);
                        sr.writeSection(ManchesterOWLSyntax.EQUIVALENT_PROPERTIES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.DISJOINT_DATA_PROPERTIES) {
                    OWLDisjointDataPropertiesAxiom ax = (OWLDisjointDataPropertiesAxiom) a;
                    if (ax.getProperties().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getProperties(), ax);
                        sr.writeSection(ManchesterOWLSyntax.DISJOINT_PROPERTIES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.EQUIVALENT_DATA_PROPERTIES) {
                    OWLEquivalentDataPropertiesAxiom ax = (OWLEquivalentDataPropertiesAxiom) a;
                    if (ax.getProperties().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getProperties(), ax);
                        sr.writeSection(ManchesterOWLSyntax.EQUIVALENT_PROPERTIES, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.DIFFERENT_INDIVIDUALS) {
                    OWLDifferentIndividualsAxiom ax = (OWLDifferentIndividualsAxiom) a;
                    if (ax.getIndividuals().size() > 2) {
                        SectionMap map = new SectionMap();
                        map.add(ax.getIndividuals(), ax);
                        sr.writeSection(ManchesterOWLSyntax.DIFFERENT_INDIVIDUALS, map, ",", false, o);
                    }
                }
                if (a.getAxiomType() == AxiomType.SWRL_RULE) {
                    SWRLRule rule = (SWRLRule) a;
                    sr.writeSection(ManchesterOWLSyntax.RULE, Collections.singleton(rule), ", ", false);
                }
                if (a.getAxiomType() == AxiomType.CLASS_ASSERTION) {
                    OWLClassAssertionAxiom ax = (OWLClassAssertionAxiom) a;
                    sr.writeFrame((OWLEntity) ax.getIndividual());
                }
                for (OWLEntity e : a.getSignature()) {
                        sr.writeFrame(e);
                }
                w.flush();
                r = w.toString();
                break;
            case IMPORT:
                r = "Import: <" + ((ImportStatement) statement).getImport().getIRI() + ">";
                break;
            case PREFIX:
                NamespacePrefixStatement s = (NamespacePrefixStatement) statement;
                r = "Prefix: " + s.getPrefix() + " <" + s.getNamespace() + ">";
                break;
            case FORMAT:
                r = "OntologyFormat: \"" + ((OntologyFormatStatement) statement).getFormat() + "\"";
                break;
            case OIRI:
                r = "OntologyIRI: <" + ((OntologyIRIStatement) statement).getIRI() + ">";
                break;
            case VIRI:
                r = "VersionIRI: <" + ((VersionIRIStatement) statement).getIRI() + ">";
                break;

        }
        return r;
    }

}
