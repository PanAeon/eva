package foo.bar.expression

import foo.bar.expression.ExpressionAST._
import foo.bar.query._

object ExpressionUtils {
  // this is very crude way to go about things
  def translateQuery( xs : List[ExpressionNode], env:QueryEnvironment) : String = {
    val ys = xs.map {
      case TextNode(text) => text
      case VariableNode(label) if !env.env.contains(label) => " ? "
      case VariableNode(label) =>
        " ( " + translateQuery(env.env(label).ast, env) + " ) "
        // when variable is in query scope we treat it as subquery with 0 arity
      case ApplyNode(label, _) =>
        " ( " + translateQuery(env.env(label).ast, env) + " ) "
    }
    
    ys.mkString(" ")
  }
  
  def translateQueryArguments( xs : List[ExpressionNode], env:QueryEnvironment) : List[String] = {
    xs.flatMap {
      case TextNode(text) => List.empty
      case VariableNode(label) if !env.env.contains(label) => List(label)
      case VariableNode(label) =>
         translateQueryArguments(env.env(label).ast, env) 
      case ApplyNode(label, _) =>
         translateQueryArguments(env.env(label).ast, env) 
    }
    
  }
  
  
}

