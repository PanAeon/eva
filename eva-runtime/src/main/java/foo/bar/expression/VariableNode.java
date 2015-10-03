package foo.bar.expression;


public class VariableNode implements ExpressionNode {
	public final String value;
	public VariableNode(String value) { this.value = value; }
	@Override public String toString() { return "`" + value + "`"; }
}
