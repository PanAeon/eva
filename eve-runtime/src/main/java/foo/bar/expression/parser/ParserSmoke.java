package foo.bar.expression.parser;

import java.util.List;

import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import foo.bar.expression.ExpressionNode;
import foo.bar.expression.IncludeNode;
import foo.bar.expression.TextNode;
import foo.bar.expression.VariableNode;

public class ParserSmoke {

	public static void main(String[] args) {
		String input = "select * from Users u where u.firstName=${firstName(p1,p2,p3)} and u.lastName = $lastName";
		ExpressionGrammar parser = Parboiled.createParser(ExpressionGrammar.class);
		ParsingResult<?> result = new ReportingParseRunner(parser.Expression()).run(input);
		
		
		System.out.println("Matched: " + result.matched);
		if(result.hasErrors()) {
			for (ParseError x : result.parseErrors) {
				System.out.println(x.getStartIndex());
				System.out.println(x.getEndIndex());
				
				System.out.println("Parser error on input: " + input.substring(x.getEndIndex(), input.length()));
				System.out.println(x.getErrorMessage());
			}
		}
		//result.valueStack
		
		String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
		System.out.println(parseTreePrintOut);
		System.out.println("result value:");
		System.out.println(result.resultValue);
		
		@SuppressWarnings("unchecked")
		List<ExpressionNode> foo = (List<ExpressionNode>)result.resultValue;
		
		StringBuilder sb = new StringBuilder();
		
		for (ExpressionNode x : foo) {
			if (x instanceof VariableNode) {
				VariableNode node = (VariableNode)x;
				System.out.println(">>>" + node.value);
				sb.append("?");
			} else if (x instanceof TextNode) {
				TextNode node = (TextNode)x;
				sb.append(node.value);
				
			} else if (x instanceof IncludeNode) {
			  IncludeNode node = (IncludeNode) x;
			  sb.append("?");
			}
		}
		System.out.println(sb.toString());
	}

}
