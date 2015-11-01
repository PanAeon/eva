package foo.bar

import foo.bar.annotations._
import scala.collection.JavaConverters._
import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.ByteBuddy
import foo.bar.expression.ExpressionParser
import foo.bar.expression.ExpressionUtils
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.description.method.ParameterList
import net.bytebuddy.description.method.ParameterDescription
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.AllArguments
import foo.bar.queries.Query1
import foo.bar.expression.ExpressionAST.VariableNode
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod
import foo.bar.query.{DeclaredParameter, DeclaredResultType, QueryDetails, QueryEnvironment}


object MainApp extends App {
  val myType = getTypeInfo(classOf[Genesys])
  val instance : Genesys = myType.newInstance()
  System.out.println(instance.usersOlderThanAgeWithOrganizations(10).result().asScala.mkString("\n"));
  
  def getTypeInfo[T]( _class: Class[T]): Class[T] = {
    val description = new TypeDescription.ForLoadedType(_class)
    val declaredMethodsList = description.getDeclaredMethods()
    
    val sqlMethodsList = declaredMethodsList
          .filter(ElementMatchers.isAnnotatedWith(classOf[sql]))
    val sqlMethods = sqlMethodsList.asScala.toList;
          
   
    
    val expressionParser = new ExpressionParser()
    //var queryEnvironment = QueryEnvironment(Map.empty)
    
    def validateSqlAnnotationValues(values: Array[String]) : Unit  = {
      if ( values.isEmpty ) sys.error ("Empty sql annotation is not implemented!")
    }
    
    val queryEnvironment = QueryEnvironment(sqlMethods.map { m =>
      val method = m.asInstanceOf[ForLoadedMethod]
      val annotationValues = method.getLoadedMethod().getAnnotation(classOf[sql]).value()
      validateSqlAnnotationValues(annotationValues)
      val rawSqlQuery = annotationValues.mkString("\n")
      
      val declaredParameters= m.getParameters().asScala.toList.map { p =>
        DeclaredParameter(p.getSourceCodeName, p.getIndex, p)
      }
      val declaredResultType = DeclaredResultType(m.getReturnType)
      val queryAST = expressionParser.parseExpression(rawSqlQuery)
      (m.getName, QueryDetails(rawSqlQuery, queryAST, method, declaredParameters, declaredResultType))
    }.toMap)
    
    queryEnvironment.resolveAllSubstitutions();
    
    // right now we can translate queries
    // how we should substitute variables? (hint: as usual, we have a name in the raw query :)
    
    val environment = QueryEnvironment(queryEnvironment.env.mapValues{q => 
      val translated = ExpressionUtils.translateQuery(q.ast, queryEnvironment)
      val jdbcMetadata =  JdbcMetadataInferer.infereMetadata(translated)
      val argumentsInOrder = ExpressionUtils.translateQueryArguments(q.ast, queryEnvironment)
      q.copy(
          translated = Some(translated),
          jdbcMetadata = Some(jdbcMetadata),
          argumentsInOrder = argumentsInOrder)
    })
    
     var stub : DynamicType.Builder[Object] = new ByteBuddy()
      .subclass(classOf[Object])
      .implement(_class);
    
    environment.env.foreach { case (name, query) =>
      val queryInterceptor = new QueryInterceptor(query)
      stub = stub.defineMethod(query.method).intercept(MethodDelegation.to(queryInterceptor)); 
    }
    /*
     *  val translatedQuery = ExpressionUtils.translateQuery(queryAST, queryEnvironment) // ok, so we can't get translated query at this point
        val jdbcMetadata = JdbcMetadataInferer.infereMetadata(translatedQuery)
        
     */
    
    //      val sqlAnnotation = m.getDeclaredAnnotations.asScala
//        .filter(_.getAnnotationType.getName == "foo.bar.annotations.sql")(0)
//      val valueMethod = sqlAnnotation
//        .getAnnotationType()
//        .getDeclaredMethods().filter(ElementMatchers.named("value"))
//				.get(0);
//      val annotationValues = sqlAnnotation
//        .getValue(valueMethod).asInstanceOf[Array[String]]
    
//    sqlMethods.foreach { m =>
//      val sqlAnnotation = m.getDeclaredAnnotations.asScala
//        .filter(_.getAnnotationType.getName == "foo.bar.annotations.sql")(0)
//      val valueMethod = sqlAnnotation
//        .getAnnotationType()
//        .getDeclaredMethods().filter(ElementMatchers.named("value"))
//				.get(0);
//      val annotationValues = sqlAnnotation
//        .getValue(valueMethod).asInstanceOf[Array[String]]
//      if ( annotationValues.isEmpty ) {
//        sys.error("empty sql annotation is not implemented")
//      }
//      val sqlQuery = annotationValues.mkString("\n")
//      val declaredParametersList = m.getParameters()
//      val parseResult = expressionParser.parseExpression(sqlQuery)
//      val translated = ExpressionUtils.translateQuery(parseResult, queryEnvironment)
//      println(s"translated: '$translated'")
//      val jdbcMetadata = JdbcMetadataInferer.infereMetadata(translated)
//      
//      println(s"jdbcMetadata: $jdbcMetadata")
//      // todo: but also declared result type!
//      stub = stub.defineMethod(m).intercept(MethodDelegation.to(new QueryInterceptor(sqlQuery, jdbcMetadata, declaredParametersList))); 
//      
//    }
    
    stub
      .make()
      .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
      .getLoaded()
      .asInstanceOf[Class[T]];
  }
  
}


class QueryInterceptor(queryDetails:QueryDetails) {
    
  @RuntimeType
		def intercept(@AllArguments  args : Array[Object]) : Object = {
			 constructCompiledQuery(args);
		}
  
  def constructCompiledQuery(actualArguments: Array[Object]) : Object = {
    val queryClass = getQueryClass(queryDetails);
			//Class<?> resultClass = getResultClass(metadata, parameters);

			// FIXME: validate parameters
																			

			// FIXME: it's not necessary to construct query dynamically
			val query = 
					new ByteBuddy().subclass(classOf[Object]).implement(queryClass).method(ElementMatchers.named("result"))
					.intercept(MethodDelegation.to(new QueryExecutorInterceptor(queryDetails, actualArguments))) 
					.make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded(); 
			
			// FIXME: why classloadingStrategy default doesn't work?
			
			
			query.newInstance();
  }
  
  def getQueryClass(queryDetails: QueryDetails) : Class[_] =  {
			if (queryDetails.declaredParameters.size == queryDetails.jdbcMetadata.get.params.size) {
				return classOf[Query1[_]]
			} else {
			  // yeah, except ordering and shit
				sys.error("not implemented")
			}
		}
}

// so, this is our actual query that is returned
class QueryExecutorInterceptor(queryDetails:QueryDetails, actualArgs: Array[Object]) {
  
  def result(@AllArguments  freeParams : Array[Object]) : java.util.List[_] = {
     JdbcMetadataInferer.withConnection { connection =>
     
       val ps = connection.prepareStatement(queryDetails.translated.get);
       
       // now we should bind actual arguments to declared parameters
       // the trick is that we need to do this also in subqueries
       
       // do the same as in the translate query but with actual arguments map
       val argumentsMap = queryDetails.declaredParameters.map { p => 
         (p.name, p)
       }.toMap
       
       // bad, params are evaluated for query and all subqueries (now)
       val jdbcParametersArray = queryDetails.jdbcMetadata.get.params.toArray
       
       // TODO: it's illogical. we should check actual argument types and map them to jdbc types,
       // not the other way around
       
       for ( (a,i) <- queryDetails.argumentsInOrder.zipWithIndex; argument = argumentsMap(a) ) {
         val value = actualArgs(argument.index)
         val jdbcType = jdbcParametersArray(i).jdbcType
         jdbcType match {
           case 4 =>  ps.setInt(i+1, value.asInstanceOf[Int])
           case 12 => ps.setString(i+1, value.asInstanceOf[String])
           case _ => sys.error(s" Jdbc type : $jdbcType is not yet implemented")
         }
       }
       
       import collection.JavaConverters._
       import javaslang._
       
       
       
//       val vars = parseResult
//         .filter { _.isInstanceOf[VariableNode] }
//         .map { _.asInstanceOf[VariableNode] }
       
      // val actualParams = parametersList.asScala.toList
//       actualParams.foreach { p =>
//         println(p.getInternalName)
//         println(p.getSourceCodeName)
//       }
       
//       val  parametersMap = actualParams.map { x =>
//         (x.getName, x.getIndex)
//       }.toMap
//       
//       val variablesSet = vars.map(_.name).toSet
//       val parametersSet = parametersMap.keySet
//       
//       if ( variablesSet != parametersSet ) {
//         sys.error("bad parameters, query: " + query) // FIXME: correct msg
//       }
       
       	// FIXME: implement free variables
				// FIXME: either add param annotation or require to enable compiler's preserve param names...
				// -g // generate all debugging info
				
       // set here query parameters
//       jdbcQueryMetadata.params.zipWithIndex.foreach { case (p, i) =>
//         val variable = vars(i)
//          p.jdbcType match {
//            case 4 =>   // int
//              ps.setInt(i+1, 
//                  actualArgs(parametersMap(variable.name)).asInstanceOf[Int])
//            case 12 =>   // int
//              ps.setString(i+1, 
//                  actualArgs(parametersMap(variable.name)).asInstanceOf[String])
//            case _ =>
//              sys.error("not implemented")
//          }
//       }
       
       val rs = ps.executeQuery();
       val resultClass = getResultClass()
       val constructor = resultClass.getConstructors()(0);
       
       var results = new java.util.ArrayList[Any]();
       
       while (rs.next()) {
         val res = new Array[Object](queryDetails.jdbcMetadata.get.resultCols.length); //new Object[metadata.nResults];
				 queryDetails.jdbcMetadata.get.resultCols.zipWithIndex.map { case (col, i) =>
				   col.jdbcType match {
				     case 4 =>
				       val x:java.lang.Integer = rs.getInt(i+1)
				       res.update(i, x)
				     case 12 =>
				       res.update(i, rs.getString(i+1))
				     case _ =>
				       sys.error("not implemented")
				   }
				 }
         
					results.add(constructor.newInstance(res:_*));
       }
				
       results
     }
  }
  
  
  
  def getResultClass() : Class[_] = {
    import javaslang._
    queryDetails.jdbcMetadata.get.resultCols.length match {
      case 0 => classOf[Tuple0]
      case 1 => classOf[Tuple1[_]]
      case 2 => classOf[Tuple2[_,_]]
      case 3 => classOf[Tuple3[_, _, _]]
      case 4 => classOf[Tuple4[_, _, _, _]]
      case 5 => classOf[Tuple5[_, _, _, _, _]]
      case 6 => classOf[Tuple6[_, _, _, _, _, _]]
      case 7 => classOf[Tuple7[_, _, _, _, _, _, _]]
      case 8 => classOf[Tuple8[_, _, _, _, _, _, _, _]]
      case 9 => classOf[Tuple9[_, _, _, _, _, _, _, _, _]]
      case 10 => classOf[Tuple10[_, _, _, _, _, _, _, _, _, _]]
      case 11 => classOf[Tuple11[_, _, _, _, _, _, _, _, _, _, _]]
      case _ => sys.error("not implemented")
    }
  }
}

