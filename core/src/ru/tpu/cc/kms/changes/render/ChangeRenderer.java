package ru.tpu.cc.kms.changes.render;

import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.statements.Statement;

public abstract class ChangeRenderer {
	public abstract String getRendering(Change<Statement> change);
}
