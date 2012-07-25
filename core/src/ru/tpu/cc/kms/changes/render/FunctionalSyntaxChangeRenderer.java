package ru.tpu.cc.kms.changes.render;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.Op;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.render.FunctionalSyntaxStatementRenderer;

public class FunctionalSyntaxChangeRenderer extends ChangeRenderer {

    public FunctionalSyntaxChangeRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    @Override
    public String getRendering(Change<Statement> change) {
        String s = new FunctionalSyntaxStatementRenderer(iriFormat).getRendering(change.getItem());
        if (change.getOp() == Op.ADD)
            return "+ " + s;
        else
            return "- " + s;
    }

}
