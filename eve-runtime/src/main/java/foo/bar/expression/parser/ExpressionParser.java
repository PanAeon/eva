package foo.bar.expression.parser;

import java.util.ArrayList;
import java.util.List;

import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import foo.bar.expression.ExpressionNode;
import foo.bar.expression.TextNode;
import foo.bar.expression.VariableNode;

// todo: cache, performance ...
// ??? should I use antlr for dsl parsing ??? parboiled seems sufficient
public class ExpressionParser {
	
	
	public static ParseResult parse(String input) {
		ParseResult parseResult = new ParseResult();
		
		ExpressionGrammar parser = Parboiled.createParser(ExpressionGrammar.class);
		ParsingResult<List<ExpressionNode>> result = new ReportingParseRunner<List<ExpressionNode>>(parser.Expression()).run(input);
		
		parseResult.hasErrors = result.hasErrors();
		
		if(result.hasErrors()) {
			StringBuilder errorMsg = new StringBuilder();
			for (ParseError x : result.parseErrors) {
//				System.out.println(x.getStartIndex());
//				System.out.println(x.getEndIndex());
				
				errorMsg.append("Parser error on input: " + input.substring(x.getEndIndex(), input.length()));
				
			}
			parseResult.errorMsg = errorMsg.toString();
			return parseResult;
		}
	
		
		//String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
		//System.out.println(parseTreePrintOut);
		//System.out.println("result value:");
		//System.out.println(result.resultValue);
	
//		StringBuilder sb = new StringBuilder();
//		
//		for (ExpressionNode x : result.resultValue) {
//			if (x instanceof VariableNode) {
//				VariableNode node = (VariableNode)x;
//				sb.append("?");
//			} else if (x instanceof TextNode) {
//				TextNode node = (TextNode)x;
//				sb.append(node.value);
//				
//			}
//		}
//		System.out.println(sb.toString());
		parseResult.result = result.resultValue;
		return parseResult; // TODO: maybe just return parboiled ParsingResult ???
	}
	
	public static String getTranslatedQuery(List<ExpressionNode> xs) { // TODO: move to utils
		
		StringBuilder sb = new StringBuilder();
		
		for (ExpressionNode x : xs) {
			if (x instanceof VariableNode) {
				sb.append("?");
			} else if (x instanceof TextNode) {
				TextNode node = (TextNode)x;
				sb.append(node.value);
				
			}
		}
		
		return sb.toString();
	}
	
	public static List<VariableNode> getVariables(List<ExpressionNode> xs) {
		List<VariableNode> result = new ArrayList<>();
		
		for (ExpressionNode x : xs) {
			if (x instanceof VariableNode) {
				result.add((VariableNode)x);
			} 
		}
		
		return result;
	}
}
