package foo.bar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

// TODO: wouldn't be nice: $query(5) ???
// interface ???
public class QueryScope {
	private Map<String, ScopedQuery> visibleQueries; // other queries, except we should also store result types...
	
	public static class ScopedQuery {
		public String query;
		public QueryMetadata metadata;
		public ParameterList<ParameterDescription.InDefinedShape> parameters;
	}
	
//	public static Map<String, List<String>> parameterNames = new HashMap<String, List<String>>();
//	
//	static {
//		parameterNames.put("foo(...)", Arrays.asList("a", "b", "c"));
//	}
}
