package foo.bar.expression

object ExpressionAST {
  trait ExpressionNode
  
  case class TextNode(text:String) extends ExpressionNode
  case class VariableNode(name:String) extends ExpressionNode // TODO: maybe remove Apply node altogether?
  case class ApplyNode(label:String, params:List[VariableNode]) extends ExpressionNode
}

