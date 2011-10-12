package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.model.OWLException;

import ru.tpu.cc.kms.statements.Statement;

public abstract class StatementRenderer {

	public abstract String getRendering(Statement statement) throws OWLException;

}
