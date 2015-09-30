package foo.bar.expression.parser;

import java.util.ArrayList;
import java.util.List;

import foo.bar.expression.ExpressionNode;

public class ParseResult {
	public boolean hasErrors = false;
	public List<ExpressionNode> result = new ArrayList<>();
	public String errorMsg;
}
