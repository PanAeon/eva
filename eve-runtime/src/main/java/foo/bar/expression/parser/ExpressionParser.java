package foo.bar.expression.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import foo.bar.expression.ExpressionNode;
import foo.bar.expression.IncludeNode;
import foo.bar.expression.TextNode;
import foo.bar.expression.VariableNode;
import foo.bar.internal.InternalQuery;
import foo.bar.internal.QueryEnvironment;

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
	
	// FIXME: should take context, and resolve includes, move to separate class / package
	public static String getTranslatedQuery(List<ExpressionNode> xs) { 
		
		StringBuilder sb = new StringBuilder();
		
		for (ExpressionNode x : xs) {
			if (x instanceof VariableNode) {
				sb.append("?");
			} else if (x instanceof TextNode) {
				TextNode node = (TextNode)x;
				sb.append(node.value);
				
			} else if (x instanceof IncludeNode) {
			  throw new RuntimeException("Not implemented!");
			}
		}
		
		return sb.toString();
	}
	
	// FIXME: move to a separate module ???
	public static String getTranslatedQuery(List<ExpressionNode> xs, QueryEnvironment env) {
	  StringBuilder sb = new StringBuilder();
	  
	  for (ExpressionNode x : xs) {
      if (x instanceof VariableNode) {
        VariableNode node = (VariableNode)x;
        InternalQuery subQuery = env.resolve(node.value);
        if ( subQuery != null) {
          sb.append(" (" + subQuery.getPreCompiledQuery() + ") ");
        } else {
          sb.append("?");
        }
      } else if (x instanceof TextNode) {
        TextNode node = (TextNode)x;
        sb.append(node.value);
        
      } else if (x instanceof IncludeNode) {
        IncludeNode node = (IncludeNode)x;
        InternalQuery subQuery = env.resolve(node.value.value);
        sb.append("(" + subQuery.getPreCompiledQuery() + ")");
      }
    }
	  
	  return sb.toString();
	}
	
	public static List<VariableNode> getVariables(List<ExpressionNode> xs) {
	  return xs.stream().filter(x -> x instanceof VariableNode).map(x -> (VariableNode)x).collect(Collectors.toList());
	}
	
	public static List<IncludeNode> getIncludes(List<ExpressionNode> xs) {
	  return xs.stream().filter(x -> x instanceof IncludeNode).map(x -> (IncludeNode)x).collect(Collectors.toList());
	}
}
