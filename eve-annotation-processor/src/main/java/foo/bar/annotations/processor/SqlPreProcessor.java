package foo.bar.annotations.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import foo.bar.annotations.sql;

//@AutoService(Processor.class)
@SupportedAnnotationTypes(
  {"foo.bar.annotations.sql"}
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SqlPreProcessor extends AbstractProcessor {
	
	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;
	
	Map<TypeElement, Set<ExecutableElement>> annotatedClasses = new HashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment env){
		super.init(env);
		typeUtils = env.getTypeUtils();
	    elementUtils = env.getElementUtils();
	    filer = env.getFiler();
	    messager = env.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {	
		messager.printMessage(Kind.NOTE, "Starting proecessing annotations...");
		
		 for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(sql.class)) {
//			 annotatedElement.getAnnotationMirrors().forEach( (x) -> {
//				 messager.printMessage(Kind.NOTE, "'" + x.getAnnotationType().asElement().getSimpleName() + "'");
//			 });
//			 AnnotationMirror annotationMirror = annotatedElement.getAnnotationMirrors().stream().filter( (x) -> x.getAnnotationType().asElement().getSimpleName().equals("sql")).findFirst().get();
			 if (annotatedElement.getKind() != ElementKind.METHOD) {
				 messager.printMessage(Kind.ERROR, "Only methods could be annotated with 'sql'", annotatedElement /*,annotationMirror*/);
			 } else {
				 ExecutableElement annotatedMethod = (ExecutableElement)annotatedElement;
				 TypeElement classElement = (TypeElement)annotatedElement.getEnclosingElement();
				 
//				 try {
//					//String pkg = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
//					 String foo = classElement.getQualifiedName().toString().replace(".", "/");
//					 // looks like source path is not provided ....
//					FileObject fo = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "",  "src/main/java/foo/bar/example/SimpleSql.java");
//					note(fo.getCharContent(true).toString());
//				} catch (IOException e) {
//					
//					e.printStackTrace();
//					note(e.getMessage());
//				} 
				 
				 if (!annotatedClasses.containsKey(classElement)) {
					 annotatedClasses.put(classElement, new HashSet<>());
				 }
				 
				 Set<ExecutableElement> set = annotatedClasses.get(classElement);
				 
				
//				 for (VariableElement x : annotatedMethod.getParameters()) {
//					 x.asType();
//					// typeUtils.isSubsignature(m1, m2)
//					// note(x.asType().
//				 }
				 
				 set.add(annotatedMethod);
				 
				// classElement.
				// elementUtils.getBinaryName(classElement.asType().);
			//	 messager.printMessage(Kind.NOTE, "simple name: " + classElement.getSimpleName().toString());
			//	 messager.printMessage(Kind.NOTE, "fqn: " + classElement.getQualifiedName().toString());
				 
				 
				 
//				 for ( VariableElement x : annotatedMethod.getParameters()) {
//					 note("Param name: " + x.getSimpleName().toString());
//				 }
//				 
//				 for (Element x : classElement.getEnclosedElements()) {
//					 note("comments: " + elementUtils.getDocComment(x));
//				 }
				 
			 }

		 }
		 
		 if (roundEnv.processingOver()) {
			 // generate code
			 generateCode();
		 }
		
		messager.printMessage(Kind.NOTE, "Annotation Processor finished....");
		 
		return true;
	}
	
	public void note(String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}
	
//	public String getFQN(Element classElement) {
//		String qualifiedSuperClassName; 
//		sql annotation = classElement.getAnnotation(sql.class);
//		try {
//		      Class<?> clazz = annotation.type();
//		      qualifiedSuperClassName = clazz.getCanonicalName();
////		      simpleTypeName = clazz.getSimpleName();
//		    } catch (MirroredTypeException mte) {
//		      DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
//		      TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
//		      qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
////		      simpleTypeName = classTypeElement.getSimpleName().toString();
//		    }
//		return qualifiedSuperClassName;
//	}
	
	// getCompletions?
	
	public void generateCode() {
		try {
		for ( Entry<TypeElement, Set<ExecutableElement>> x : annotatedClasses.entrySet()) {
			TypeElement classElement = x.getKey();
			Set<ExecutableElement> methods = x.getValue();
			
			// generate class ...
			JavaFileObject jfo = filer.createSourceFile(classElement.getQualifiedName() + "SqlInfo");
			 BufferedWriter bw = new BufferedWriter(jfo.openWriter());
			 PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
			 if (packageElement.getQualifiedName() != null && packageElement.getQualifiedName().length() > 0) {
		       bw.append("package ");
		       bw.append(packageElement.getQualifiedName());
		       bw.append(";");
		       bw.newLine();
			   bw.newLine();
			 }
			 
			 bw.append("import java.util.List;");
			 bw.newLine();
			 bw.append("import java.util.Map;");
			 bw.newLine();
			 bw.append("import java.util.ArrayList;");
			 bw.newLine();
			 bw.append("import java.util.HashMap;");
			 bw.newLine();
			 bw.append("import java.util.Arrays;");
			 bw.newLine();
			 bw.append("public class ");
			 bw.append(classElement.getSimpleName() + "SqlInfo");
			 bw.append(" {");
			 bw.newLine();
			 bw.newLine();
			 
			 bw.append("public static Map<String, List<String>> parameterNames = new HashMap<String, List<String>>();");
			 bw.newLine();
			 
			 bw.append("static {");
			 bw.newLine();
			 
			 for (ExecutableElement m : methods) {
				 bw.append("parameterNames.put(\"");//foo(...)", Arrays.asList("a", "b", "c"));")
				 String methodSignature = m.getSimpleName().toString();// + "(" +  String.join(",", m.getParameters().stream().map(param -> param.asType().g)).toArray());
				
				 ExecutableType executableType = (ExecutableType)m.asType();
				 
				// note(executableType.toString());
				 
				 bw.append(methodSignature + "" + executableType.toString()); // todo: think something more appropriate...
				 bw.append("\", Arrays.asList(");
				 bw.append(String.join(", ", m.getParameters().stream().map( (param) -> "\"" + param.getSimpleName() + "\"").collect(Collectors.toList())));
				 bw.append("));");
				 bw.newLine();
//				 for ( VariableElement x : annotatedMethod.getParameters()) {
//				 note("Param name: " + x.getSimpleName().toString());
//			 }
			 }
			 bw.append("}");
			 bw.newLine();
			 
			 
			 bw.append("}");
			 bw.newLine();
			 bw.newLine();
			 
			 bw.close();
			
		}
		} catch (Exception e) {
			e.printStackTrace();
			messager.printMessage(Kind.ERROR, "Error! " + e.getMessage());
		}
		// shit should return new class, and get it by reflection than....
		// ok, generate class FQN+"_sqlInfo" with static fields presenting needed information
	}
	

	@Override
	public SourceVersion getSupportedSourceVersion() { // Java 8
		return SourceVersion.latestSupported();
	}

}
