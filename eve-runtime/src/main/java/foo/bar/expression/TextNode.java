package foo.bar.expression;


public class TextNode implements ExpressionNode {
	public final String value;
	public TextNode(String value) { this.value = value; }
	@Override public String toString() { return "`" + value + "`"; }
}