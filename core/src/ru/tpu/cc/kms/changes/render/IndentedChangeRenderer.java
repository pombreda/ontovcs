package ru.tpu.cc.kms.changes.render;

import java.util.Arrays;

import org.semanticweb.owlapi.model.OWLException;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.Op;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.render.IndentedStatementRenderer;

public class IndentedChangeRenderer extends ChangeRenderer {

    public IndentedChangeRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    @Override
    public String getRendering(Change<Statement> change) {
        String s;
        try {
            s = new IndentedStatementRenderer(iriFormat).getRendering(change.getItem());
        } catch (OWLException e) {
            s = Arrays.toString(e.getStackTrace());
        }
        if (change.getOp() == Op.ADD)
            return "+ " + s;
        else
            return "- " + s;
    }

}
