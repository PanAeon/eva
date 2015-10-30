package foo.bar.expression

import org.parboiled.scala._
import org.parboiled.errors.{ErrorUtils, ParsingException}
import ExpressionAST._

class ExpressionGrammar extends Parser {
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
  
  def VarOrApply  = rule { Variable | ApplyDef }
  
  def VariableName = rule {
    ("a" - "z" | "A" - "Z" | "_") ~ zeroOrMore("0" - "9" | "a" - "z" | "A" - "Z" | "_")
  }
}