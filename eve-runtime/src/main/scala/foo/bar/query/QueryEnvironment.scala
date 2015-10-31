package foo.bar.query

import foo.bar.expression.ExpressionAST._

case class QueryDetails(
    rawSql:String,
    ast:List[ExpressionNode],
    translated:String)
//stores info about scope of the given expression query
case class QueryEnvironment(env:Map[String, QueryDetails]) {
  
}