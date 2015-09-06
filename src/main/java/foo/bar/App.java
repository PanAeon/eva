package foo.bar;

import java.lang.annotation.Annotation;

import javaslang.Tuple5;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy.Fixed;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;
import static net.bytebuddy.matcher.ElementMatchers.*;



public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	new App()._main();
    }
    
    public Class<? extends Object> getTypeInfo(Class<?> _class) throws Exception {
    	ForLoadedType description = new TypeDescription.ForLoadedType(_class);
    	//AnnotationList declaredAnnotations = description.getDeclaredAnnotations();
    	MethodList<InDefinedShape> declaredMethods = description.getDeclaredMethods();
    	MethodList<InDefinedShape> sqlMethods = declaredMethods.filter(isAnnotatedWith(sql.class));
    	for ( InDefinedShape m : sqlMethods) {
    		AnnotationList methodAnnotations = m.getDeclaredAnnotations();
    		//methodAnnotations.forEach(x -> System.out.println(x.getAnnotationType().getName()));
    		AnnotationDescription sqlAnnotation = methodAnnotations.stream().filter(x -> x.getAnnotationType().getName().equals("foo.bar.sql")).findFirst().get();
    		//sqlAnnotation.getAnnotationType().findVariable(symbol)
    		InDefinedShape valueMethod = sqlAnnotation.getAnnotationType().getDeclaredMethods().filter(named("value")).get(0);
    		String query = (String)sqlAnnotation.getValue(valueMethod);
    		//TypeInferer.infereTypes(query);
    		QueryMetadata metadata = JdbcMetadataInferer.infereMetadata(query);
    		System.out.println(metadata);
    		
    		Object pureValue = null;
    		// now how do we construct the value
    		
//    		switch(metadata.nResults) {
//    		case 5:
    			pureValue = new Query<Tuple5<Integer, String, String, String, String>>() {}; // should construct this at runtime somehow(
    			//break;
//    		default:
//    			throw new RuntimeException("not implemented(");
//    		}
    		
    		Class<?> dynamicType = new ByteBuddy()
    		.subclass(Object.class)
    		.implement(Genesys.class)
    		.defineMethod(m).intercept(FixedValue.value(pureValue)) // fixed value also takes type description ...
    		.make()
    		.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
      	    .getLoaded();
    		return dynamicType;
    	}
    	throw new RuntimeException("not implemented");
    }
    
    public void _main() throws Exception {
    	Class<?> myType = getTypeInfo(Genesys.class);
    	Genesys instance = (Genesys)myType.newInstance();
    	System.out.println(instance.userQ(3)); // and yes, query param -> result;
//    	Class<?> dynamicType = new ByteBuddy()
//  	  .subclass(Object.class)
//  	  .implement(Genesys.class)
//  	  .method(named("toString")).intercept(FixedValue.value("Hello World!"))
////  	  .method(named("query1")).intercept(MethodDelegation.to(SqlTarget.class))
//  	  // shouldUseDefineMethod
//  	  .method(isAnnotatedWith(sql.class)).intercept(MethodDelegation.to(SqlTarget.class))
//  	  .make()
//  	  .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
//  	  .getLoaded();
//      Genesys instance = (Genesys)dynamicType.newInstance();
//      System.out.println(instance.userQ(3));
    }
    
  public static  class SqlTarget {
  	  public static int intercept(
  			  @AllArguments Object[] arguments,
  			  @This Object _this) {
  	    return 3;
  	  }
  	}

}
