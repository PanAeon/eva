package foo.bar.query

import foo.bar.expression.ExpressionAST._
import net.bytebuddy.description.method.ParameterDescription
import net.bytebuddy.description.`type`.generic.GenericTypeDescription
import net.bytebuddy.description.method.MethodDescription
import scala.collection.mutable
import foo.bar.JdbcQueryMetadata


case class DeclaredParameter(name:String, index:Int, typeDescription:ParameterDescription.InDefinedShape)
case class DeclaredResultType(typeDescription:GenericTypeDescription)

case class QueryDetails(
    rawSql:String,
    ast:List[ExpressionNode],
    method: MethodDescription.ForLoadedMethod,
    declaredParameters: List[DeclaredParameter],
    declaredResultType: DeclaredResultType,
    translated:Option[String] = None,
    hasErrors:Boolean = false,
    var substitutions:Map[String, QueryDetails] = Map.empty, // this is very bad music
    jdbcMetadata: Option[JdbcQueryMetadata] = None,
    argumentsInOrder: List[String] = List.empty) {
  lazy val variables : Set[String] = { // FIXME: bad, should include type or resolve it later
    ast.flatMap {
      case VariableNode(name) =>  List(name)
      case ApplyNode(name, vars) => name :: vars.map(_.name)
      case TextNode(_) => List.empty
    }.toSet
  }
  

}
    
    
//stores info about scope of the given expression query
//FIXME: check for cycles
// TODO: this code MUST be unit tested
case class QueryEnvironment(env:Map[String, QueryDetails]) {
  def resolveAllSubstitutions() : Unit = {
    // for now don't build graph, just resolve vars one by one, hm..., ok build graph
    val resolvedSubstitutions = mutable.Map.empty[String, String]
    env.foreach { case (name,queryDetails) =>
      val substitutions = queryDetails.variables.flatMap { 
        case v if env contains v => Set((v, env(v)))
        case _ => Set.empty[(String, QueryDetails)]
      }.toMap
      queryDetails.substitutions = substitutions
    }
    
    def checkCycles(query:QueryDetails, traversed:Set[String]=Set.empty) : Unit = {
      query.substitutions.foreach { case (k, v) =>
        if (traversed.contains(k)) {
          sys.error(s"there're cycles in query $query on variable $k")
        } else {
          checkCycles(v, traversed + k)
        }
      }
    }
    
    env.values.foreach (checkCycles(_))
    
    // check that all variables are resolved:
    env.foreach { case (k,v) =>
      val queryParameters = v.declaredParameters.map(_.name).toSet
      val xs = v.variables -- v.substitutions.keySet -- queryParameters
      if (!xs.isEmpty) {
        sys.error(s"Unresolved variables $xs in query: '$k'")
      }
    }
    
    
    
  }
}