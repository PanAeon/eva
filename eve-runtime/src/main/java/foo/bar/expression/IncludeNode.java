package foo.bar.expression;

import java.util.List;

public class IncludeNode implements ExpressionNode {

	public final VariableNode value;
	public final List<VariableNode> params;
	
	public IncludeNode(VariableNode value, List<VariableNode> params) {
		this.value = value;
		this.params = params;
	}
	
}
