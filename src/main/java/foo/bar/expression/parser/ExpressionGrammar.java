package foo.bar.expression.parser;


import java.util.ArrayList;
import java.util.List;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.support.Var;

import foo.bar.expression.ExpressionNode;
import foo.bar.expression.TextNode;
import foo.bar.expression.VariableNode;

/**
 * 
    Expression <- (Text | Variable)*
    Text <- [^$]
    Variable <- ('$' SimpleVariableDef) | ( '${' QuotedVariable '}' )
    SimpleVariableDef <- [0-9a-zA-Z_]+
    QuotedVariable <- [0-9a-zA-Z_]+
 *
 */


@BuildParseTree 
public class ExpressionGrammar extends BaseParser<List<ExpressionNode>> {
	
	@SuppressNode
	public Rule Text(Var<List<ExpressionNode>> variables) {
		return Sequence(OneOrMore(FirstOf("$$", NoneOf("$"))),
				variables.get().add(new TextNode(match())));
	}
	
	@SuppressNode
	public Rule SimpleVariableName(Var<List<ExpressionNode>> variables) {
		return Sequence(
				OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_')),
				variables.get().add(new VariableNode(match()))
			); // TODO: not exactly java var definition....
	}
	
	@SuppressNode
	public Rule Variable(Var<List<ExpressionNode>> variables) {
		return FirstOf(
				Sequence('$', SimpleVariableName(variables)),
				Sequence("${", SimpleVariableName(variables), "}")
			   );
	}
	
	public Rule Expression() {
		Var<List<ExpressionNode>> results = new Var<>(new ArrayList<>());
        return Sequence(OneOrMore(FirstOf(Text(results), Variable(results))), EOI, push(results.get()));
    }

}







