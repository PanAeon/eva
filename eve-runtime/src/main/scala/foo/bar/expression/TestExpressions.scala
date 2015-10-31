package foo.bar.expression

object TestExpressions {
  val input = "select * from Users u where u.firstName=${firstName(p1,p2,p3)} and u.lastName = $lastName";
  def main(args:Array[String]) : Unit = {
    val parser = new ExpressionParser()
    val result = parser.parseExpression(input)
    println(result)
  }
}