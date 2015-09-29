package foo.bar;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

import foo.bar.queries.Query1;
import javaslang.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy.Fixed;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class App {
	public static void main(String[] args) throws Exception {
		new App()._main();
	}

	public Class<? extends Object> getTypeInfo(Class<?> _class) throws Exception {
		ForLoadedType description = new TypeDescription.ForLoadedType(_class);
		// AnnotationList declaredAnnotations =
		// description.getDeclaredAnnotations();
		MethodList<InDefinedShape> declaredMethods = description.getDeclaredMethods();
		MethodList<InDefinedShape> sqlMethods = declaredMethods.filter(isAnnotatedWith(sql.class));
		for (InDefinedShape m : sqlMethods) {
			AnnotationList methodAnnotations = m.getDeclaredAnnotations();
			// methodAnnotations.forEach(x ->
			// System.out.println(x.getAnnotationType().getName()));
			AnnotationDescription sqlAnnotation = methodAnnotations.stream()
					.filter(x -> x.getAnnotationType().getName().equals("foo.bar.sql")).findFirst().get();
			// sqlAnnotation.getAnnotationType().findVariable(symbol)
			InDefinedShape valueMethod = sqlAnnotation.getAnnotationType().getDeclaredMethods().filter(named("value"))
					.get(0);
			String sqlQuery = (String) sqlAnnotation.getValue(valueMethod);

			// TypeInferer.infereTypes(query);

			// Prepare query (strip named params)

			ParameterList<ParameterDescription.InDefinedShape> parameters = m.getParameters();

			QueryMetadata metadata = JdbcMetadataInferer.infereMetadata(sqlQuery.replaceAll("\\{[^\\}]*\\}", "?"));
			System.out.println(metadata);

			 // +
																							// params!
			
//			Query1<Tuple5<Integer, String, String, String,String>> foo = ((Query1<Tuple5<Integer, String, String, String,String>>)compiledQuery);
			
			
//			Object compiledQuery1 = 
//					 new Query1<Tuple5<Integer, String, String, String,String>>() {
//					 public Tuple5<Integer, String, String, String, String> result() {
//					 return null;
//					 }
//					 };
			// switch(metadata.nResults) {
			// case 5:
			// arity, how to construct arity?
			// compiledQuery = new Query0<Tuple5<Integer, String, String,
			// String, String>>() {
			// public Tuple5<Integer, String, String, String, String> result()
			// {
			// return null;
			// }
			// }; // should construct this at runtime somehow(
			// break;
			// default:
			// throw new RuntimeException("not implemented(");
			// }

			// todo: it's wrong to use fixed value when method takes params!
			// fixed value

			Class<?> dynamicType = 
					new ByteBuddy()
					.subclass(Object.class)
					.implement(_class)
					.defineMethod(m)
					.intercept(MethodDelegation.to(new QueryInterceptor(sqlQuery, metadata, parameters))) // fixed value
																// also takes
																// type
																// description
																// ...
					.make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
			return dynamicType;
		}
		throw new RuntimeException("not implemented");
	}

	public void _main() throws Exception {
		Class<?> myType = getTypeInfo(Genesys.class);
		Genesys instance = (Genesys) myType.newInstance();
		System.out.println(instance.userQ().result());
		// Class<?> dynamicType = new ByteBuddy()
		// .subclass(Object.class)
		// .implement(Genesys.class)
		// .method(named("toString")).intercept(FixedValue.value("Hello
		// World!"))
		//// .method(named("query1")).intercept(MethodDelegation.to(SqlTarget.class))
		// // shouldUseDefineMethod
		// .method(isAnnotatedWith(sql.class)).intercept(MethodDelegation.to(SqlTarget.class))
		// .make()
		// .load(getClass().getClassLoader(),
		// ClassLoadingStrategy.Default.WRAPPER)
		// .getLoaded();
		// Genesys instance = (Genesys)dynamicType.newInstance();
		// System.out.println(instance.userQ(3));
	}

	public static class SqlTarget {
		public static int intercept(@AllArguments Object[] arguments, @This Object _this) {
			return 3;
		}
	}


	
	
	public static class QueryInterceptor {
		String query;
		QueryMetadata metadata;
		ParameterList<ParameterDescription.InDefinedShape> paramsMetadata;

		public QueryInterceptor(String query, QueryMetadata metadata,
				ParameterList<ParameterDescription.InDefinedShape> paramsMetadata) {
			this.query = query;
			this.metadata = metadata;
			this.paramsMetadata = paramsMetadata;
		}
		
		@RuntimeType
		public Object intercept(@AllArguments Object[] args)  {
			 Object compiledQuery = constructCompiledQuery(query, metadata, paramsMetadata, args);
			 return compiledQuery;
		}
		
		private Object constructCompiledQuery(String rawQuery, QueryMetadata metadata,
				ParameterList<ParameterDescription.InDefinedShape> paramsMetadata, Object[] args) {
			Class<?> queryClass = getQueryClass(metadata, paramsMetadata);
			//Class<?> resultClass = getResultClass(metadata, parameters);

			// FIXME: validate parameters
			
			// now only ...
			
			
			
			
			
			//String preparedQuery = rawQuery.replaceAll("\\{[^\\}]*\\}", "1"); 
																				

			// it's not necessary to construct query dynamically
			Class<?> query = 
					new ByteBuddy().subclass(Object.class).implement(queryClass).method(named("result"))
					.intercept(MethodDelegation.to(new QueryExecutorInterceptor(rawQuery, metadata, paramsMetadata, args))) // fixed
																											// value
																											// is
																											// also
																											// not
																											// very
																											// applicable
																											// here,
																											// except
																											// query1
					
					
					.make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded(); // FIXME: why classloadingStrategy default doesn't work?
			
			try {
				return query.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			

			// compiledQuery = new Query0<Tuple5<Integer, String, String, String,
			// String>>() {
			// public Tuple5<Integer, String, String, String, String> result() {
			// return null;
			// }
			// }; // sho

		}

		// select between query0 ... queryN
		private Class<?> getQueryClass(QueryMetadata metadata,
				ParameterList<ParameterDescription.InDefinedShape> parameters) {
			if (metadata.nParams == parameters.size()) {
				return Query1.class;
			} else {
				throw new RuntimeException("not implemented");
			}
		}
	}

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

		public List<?> result(@AllArguments Object[] freeParams) { // fixme, plz, should have paramz actually, I mean no, result doesn't take arguments..., no it does! (sometimes
			try {
				Connection connection = JdbcMetadataInferer.ds.getConnection();
				String preparedQuery = query.replaceAll("\\{[^\\}]*\\}", "?"); 
				PreparedStatement ps = connection.prepareStatement(preparedQuery);
				
			//Map<String, String> valuesMap = new HashMap<>();
				
				// FIXME, FIXME, FIXME, assumed params in order...
				for (int i = 0; i < parameters.size(); i++) {
					// fuck, to string ....
					int paramType = metadata.paramTypes[i];
					
					switch (paramType)  {
					case 4: // int
						ps.setInt(i+1, (int)args[i]);
						break;
					case 12: // String
						ps.setString(i+1, (String)args[i]);
						break;
					default:
						throw new RuntimeException("not implemented");
					
					}
					//preparedQuery = preparedQuery.replaceFirst("\\{" + parameters.get(i).getName() +"\\}", args[i]);
				}
				//StrSubstitutor.replace(source, valueMap); wait, no point :(
				
				ResultSet rs = ps.executeQuery();
				Class<?> resultClass = getResultClass(metadata, parameters);
				Constructor<?> constructor = resultClass.getConstructors()[0]; // FIXME: tuple has just one constructor
				
				List<Object> results = new ArrayList<Object>();
				
				
				
				
				while (rs.next()) {
					Object res[] = new Object[metadata.nResults];
					for (int i = 0; i < metadata.nResults; i++) {
						int resultType = metadata.resultTypes[i];
						switch (resultType)  {
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
		}
		
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



}
