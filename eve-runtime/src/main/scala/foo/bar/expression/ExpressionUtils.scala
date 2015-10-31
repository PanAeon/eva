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
        " ( " + env.env(label).translated + " ) "
        // when variable is in query scope we treat it as subquery with 0 arity
      case ApplyNode(label, _) =>
        " ( " + env.env(label).translated + " ) "
    }
    
    ys.mkString(" ")
  }
}

