package foo.bar.expression;

//import java.util.List;
//import java.util.stream.Collectors;
//
//
//public class IncludeNode implements ExpressionNode {
//
//	public final VariableNode value;
//	public final List<VariableNode> params;
//	
//	
//	public IncludeNode(VariableNode value, List<VariableNode> params) {
//		this.value = value;
//		this.params = params;
//	}
//	
//	@Override public String toString() { 
//	  
//	  String p = params.stream().map(x -> x.toString()).collect(Collectors.joining(",", "(", ")"));
//	  return "`Include: " + value  + p + "`"; 
//	}
//	
//}
