package foo.bar.internal;

import java.util.List;

import foo.bar.expression.parser.ParseResult;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

public class InternalQuery {
  public String id; // ??
  public String query;
  public QueryMetadata queryMetadata;
  public ParameterList<ParameterDescription.InDefinedShape> methodParameters; // hm ....
  public List<QueryParameter> parameters;
  
  public ParseResult parseResult;
  public String getPreCompiledQuery() { // FIXME: should be valid AST in the end, with resolved variables
    throw new RuntimeException("Not implemented!"); 
  }
	// TODO: querymappings, with result type
}
