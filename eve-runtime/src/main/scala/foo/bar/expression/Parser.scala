package foo.bar.expression

import org.parboiled.scala._
import org.parboiled.errors.{ErrorUtils, ParsingException}
import ExpressionAST._

// TODO: tokenize whitespace, or add it here, see notes about ws in parboiled
class ExpressionParser extends Parser {
  def Expression : Rule1[List[ExpressionNode]] = rule {
    oneOrMore(Text | Term) ~ EOI
  }
  
  def Text = rule {
    oneOrMore(Character) ~> TextNode
  }
  
  def Character = rule { EscapedChar | NormalChar }
  def EscapedChar = rule { str("$$") } 
  def NormalChar = rule { noneOf("$")}
  
  
  def Term: Rule1[ExpressionNode] = rule {
    (( str("$") ~ VarOrApply ) | ( str("${") ~ VarOrApply ~ str("}"))) 
  }

  def Variable = rule {
    VariableName ~> VariableNode
  }
  def Parameters = rule {
   (str("(") ~ zeroOrMore(Variable, separator = ",") ~ str(")"))
  }
  
  def ApplyDef: Rule1[ApplyNode] = rule {
    VariableName ~> (identity)  ~ Parameters ~~> ApplyNode
  }
  
  def VarOrApply  = rule { ApplyDef | Variable }
  
  def VariableName = rule {
    ("a" - "z" | "A" - "Z" | "_") ~ zeroOrMore("0" - "9" | "a" - "z" | "A" - "Z" | "_")
  }
  
  def parseExpression(sql: String): List[ExpressionNode] = {
    val parsingResult = ReportingParseRunner(Expression).run(sql)
    parsingResult.result match {
      case Some(astRoot) => astRoot
      case None => throw new ParsingException("Invalid SQL Expression source:\n" +
              ErrorUtils.printParseErrors(parsingResult)) 
    }
  }
}