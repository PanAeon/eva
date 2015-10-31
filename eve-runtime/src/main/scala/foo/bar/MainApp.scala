package foo.bar

import foo.bar.annotations._
import scala.collection.JavaConverters._
import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.ByteBuddy
import foo.bar.expression.ExpressionParser
import foo.bar.query.QueryEnvironment
import foo.bar.expression.ExpressionUtils
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.description.method.ParameterList
import net.bytebuddy.description.method.ParameterDescription
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.AllArguments
import foo.bar.queries.Query1
import foo.bar.expression.ExpressionAST.VariableNode
import foo.bar.JdbcQueryMetadata
import foo.bar.JdbcMetadataInferer

object MainApp extends App {
  val myType = getTypeInfo(classOf[Genesys])
  val instance : Genesys = myType.newInstance()
  System.out.println(instance.userWithOrgQ(1).result());
  
  def getTypeInfo[T]( _class: Class[T]): Class[T] = {
    val description = new TypeDescription.ForLoadedType(_class)
    val declaredMethodsList = description.getDeclaredMethods()
    
    val sqlMethodsList = declaredMethodsList
          .filter(ElementMatchers.isAnnotatedWith(classOf[sql]))
    val sqlMethods = sqlMethodsList.asScala.toList;
          
    var stub : DynamicType.Builder[Object] = new ByteBuddy()
      .subclass(classOf[Object])
      .implement(_class);
    
    val expressionParser = new ExpressionParser()
    var queryEnvironment = QueryEnvironment(Map.empty)
    
    sqlMethods.foreach { m =>
      val methodAnnotations = m.getDeclaredAnnotations.asScala
      val sqlAnnotation = methodAnnotations
        .filter(_.getAnnotationType.getName == "foo.bar.annotations.sql")
        .apply(0)
      val valueMethod = sqlAnnotation
        .getAnnotationType()
        .getDeclaredMethods().filter(ElementMatchers.named("value"))
				.get(0);
      val annotationValues = sqlAnnotation
        .getValue(valueMethod).asInstanceOf[Array[String]]
      if ( annotationValues.isEmpty ) {
        sys.error("empty sql annotation is not implemented")
      }
      val sqlQuery = annotationValues.mkString("\n")
      val declaredParametersList = m.getParameters()
      val parseResult = expressionParser.parseExpression(sqlQuery)
      val translated = ExpressionUtils.translateQuery(parseResult, queryEnvironment)
      println(s"translated: '$translated'")
      val jdbcMetadata = JdbcMetadataInferer.infereMetadata(translated)
      
      println(s"jdbcMetadata: $jdbcMetadata")
      // todo: but also declared result type!
      stub = stub.defineMethod(m).intercept(MethodDelegation.to(new QueryInterceptor(sqlQuery, jdbcMetadata, declaredParametersList))); 
      
    }
    stub
      .make()
      .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
      .getLoaded()
      .asInstanceOf[Class[T]];
  }
  
}


class QueryInterceptor( query : String, jdbcMetadata : JdbcQueryMetadata,
      declaredParametersList: ParameterList[ParameterDescription.InDefinedShape]) {
    
  @RuntimeType
		def intercept(@AllArguments  args : Array[Object]) : Object = {
			 constructCompiledQuery(query, jdbcMetadata, declaredParametersList, args);
		}
  
  def constructCompiledQuery(rawQuery : String,
      jdbcMetadata : JdbcQueryMetadata,
      declaredParametersList: ParameterList[ParameterDescription.InDefinedShape],
      actualParameters: Array[Object]) : Object = {
    val queryClass = getQueryClass(jdbcMetadata, declaredParametersList);
			//Class<?> resultClass = getResultClass(metadata, parameters);

			// FIXME: validate parameters
																			

			// FIXME: it's not necessary to construct query dynamically
			val query = 
					new ByteBuddy().subclass(classOf[Object]).implement(queryClass).method(ElementMatchers.named("result"))
					.intercept(MethodDelegation.to(new QueryExecutorInterceptor(rawQuery, jdbcMetadata, declaredParametersList, actualParameters))) 
					.make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded(); 
			
			// FIXME: why classloadingStrategy default doesn't work?
			
			
			query.newInstance();
  }
  
  def getQueryClass( jdbcQueryMetadata:JdbcQueryMetadata,
				parameters:ParameterList[ParameterDescription.InDefinedShape]) : Class[_] =  {
			if (jdbcQueryMetadata.params.size == parameters.size) {
				return classOf[Query1[_]]
			} else {
			  // yeah, except ordering and shit
				sys.error("not implemented")
			}
		}
}

// so, this is our actual query that is returned
class QueryExecutorInterceptor(query:String,
    jdbcQueryMetadata:JdbcQueryMetadata,
    parametersList:ParameterList[ParameterDescription.InDefinedShape],
    actualArgs: Array[Object]) {
  
  def result(@AllArguments  freeParams : Array[Object]) : java.util.List[_] = {
     JdbcMetadataInferer.withConnection { connection =>
       val parser = new ExpressionParser()
       val parseResult = parser.parseExpression(query)
       
       // hm, bad, very bad
       val env = QueryEnvironment(Map.empty)
       val translated = ExpressionUtils.translateQuery(parseResult, env)
       
       val ps = connection.prepareStatement(translated);
       val vars = parseResult
         .filter { _.isInstanceOf[VariableNode] }
         .map { _.asInstanceOf[VariableNode] }
       import collection.JavaConverters._
       import javaslang._
       val actualParams = parametersList.asScala.toList
       actualParams.foreach { p =>
         println(p.getInternalName)
         println(p.getSourceCodeName)
       }
       
       val  parametersMap = actualParams.map { x =>
         (x.getName, x.getIndex)
       }.toMap
       
       val variablesSet = vars.map(_.name).toSet
       val parametersSet = parametersMap.keySet
       
       if ( variablesSet != parametersSet ) {
         sys.error("bad parameters, query: " + query) // FIXME: correct msg
       }
       
       	// FIXME: implement free variables
				// FIXME: either add param annotation or require to enable compiler's preserve param names...
				// -g // generate all debugging info
				
       // set here query parameters
       jdbcQueryMetadata.params.zipWithIndex.foreach { case (p, i) =>
         val variable = vars(i)
          p.jdbcType match {
            case 4 =>   // int
              ps.setInt(i+1, 
                  actualArgs(parametersMap(variable.name)).asInstanceOf[Int])
            case 12 =>   // int
              ps.setString(i+1, 
                  actualArgs(parametersMap(variable.name)).asInstanceOf[String])
            case _ =>
              sys.error("not implemented")
          }
       }
       
       val rs = ps.executeQuery();
       val resultClass = getResultClass()
       val constructor = resultClass.getConstructors()(0);
       
       var results = new java.util.ArrayList[Any]();
       
       while (rs.next()) {
         val res = new Array[Object](jdbcQueryMetadata.resultCols.length); //new Object[metadata.nResults];
				 jdbcQueryMetadata.resultCols.zipWithIndex.map { case (col, i) =>
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
    jdbcQueryMetadata.resultCols.length match {
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
/*

	

	public static class QueryExecutorInterceptor { 
		String query;
		QueryMetadata metadata;
		ParameterList<ParameterDescription.InDefinedShape> parameters;
		Object[] args; // boundParams

		public QueryExecutorInterceptor(String query, QueryMetadata metadata,
				ParameterList<ParameterDescription.InDefinedShape> parameters, Object[] args) {
			this.query = query;
			this.metadata = metadata;
			this.parameters = parameters;
			this.args = args;
		}
		
    /*
		public List<?> result(@AllArguments Object[] freeParams) {
			try {
				Connection connection = JdbcMetadataInferer.ds.getConnection();
				
				ParseResult parseResult = ExpressionParser.parse(query);
				
			//	String preparedQuery = query.replaceAll("\\{[^\\}]*\\}", "?"); 
				PreparedStatement ps = connection.prepareStatement(ExpressionParser.getTranslatedQuery(parseResult.result));
				
				List<VariableNode> variables = ExpressionParser.getVariables(parseResult.result); // btw, in order
				
				parameters.forEach((x) -> {
//					x.getInternalName()
					System.out.println(x.getInternalName());
					System.out.println(x.getSourceCodeName());
				});
				
				List<Tuple2<String, Integer>> params = parameters.stream().map( (x) -> new Tuple2<>(x.getName(), x.getIndex())).collect(Collectors.toList());
				Map<String, Integer> parametersMap = 
						parameters.stream().map( (x) -> new Tuple2<>(x.getName(), x.getIndex()))
						.collect(Collectors.toMap((x) -> x. _1, (x) -> x._2));
				
				Set<String> variablesSet = variables.stream().map( (x) -> x.value).collect(Collectors.toSet());
				Set<String> parametersSet = parametersMap.keySet();
				
				if (!variablesSet.equals(parametersSet)) {
					throw new RuntimeException("Wrong parameters something.... query:`" + query + "`"); // FIXME: correct message ....
				}
				// FIXME: implement free variables
				// FIXME: either add param annotation or require to enable compiler's preserve param names...
				// -g // generate all debugging info
				
				
				
			//Map<String, String> valuesMap = new HashMap<>();
				
				for (int i = 0; i < parameters.size(); i++) {
					
					VariableNode var = variables.get(i);
					// fuck, to string ....
					int paramType = metadata.paramTypes[i];
					
					switch (paramType)  {
					case 4: // int
						ps.setInt(i+1, (int)args[parametersMap.get(var.value)]);
						break;
					case 12: // String
						ps.setString(i+1, (String)args[parametersMap.get(var.value)]);
						break;
					default:
						throw new RuntimeException("not implemented");
					
					}
					//preparedQuery = preparedQuery.replaceFirst("\\{" + parameters.get(i).getName() +"\\}", args[i]);
				}
				//StrSubstitutor.replace(source, valueMap); wait, no point :(
				
				ResultSet rs = ps.executeQuery();
				Class<?> resultClass = getResultClass(metadata, parameters); // FIXME: return class && constructor or just constructor
				Constructor<?> constructor = resultClass.getConstructors()[0]; // FIXME: tuple has just one constructor
				
				List<Object> results = new ArrayList<Object>();
				
				
				
				
				while (rs.next()) {
					Object res[] = new Object[metadata.nResults];
					for (int i = 0; i < metadata.nResults; i++) {
						int resultType = metadata.resultTypes[i];
						switch (resultType)  { // should make this easier somehow...
						case 4: // int
							res[i] = rs.getInt(i+1);
							break;
						case 12: // String
							res[i] = rs.getString(i+1);
							break;
						default:
							throw new RuntimeException("not implemented");
						
						}
						
					}
					results.add(constructor.newInstance(res));
				}
				return results;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} */
		
		// select between Tuple0 ... TupleN ... for Now, also need Void type
		private Class<?> getResultClass(QueryMetadata metadata,
				ParameterList<ParameterDescription.InDefinedShape> parameters) {
			switch (metadata.nResults) { // stupid! class for name? map?
			case 0:
				return Tuple0.class;
			case 1:
				return Tuple1.class;
			case 2:
				return Tuple2.class;
			case 3:
				return Tuple3.class;
			case 4:
				return Tuple4.class;
			case 5:
				return Tuple5.class;
			case 6:
				return Tuple6.class;
			case 7:
				return Tuple7.class;
			case 8:
				return Tuple8.class;
			case 9:
				return Tuple9.class;
			case 10:
				return Tuple10.class;
			case 11:
				return Tuple11.class;
			default:
				throw new RuntimeException("Not implemented");
			}
		}
	}
*/