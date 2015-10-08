package foo.bar.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import foo.bar.QueryMetadata;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

// TODO: wouldn't be nice: $query(5) ??? yeah, but needs special handling like 
// $query(`literal`5)
// $query($#5), hmm too complicated
// $query(`5`)
// $query(...)

// should I rewrite all this internal classes as interfaces ?

public class QueryEnvironment {
	
	// todo: maybe Map<String, InternalQuery>;
    public Map<String, InternalQuery> environment = new HashMap<>();
	
//	public static Map<String, List<String>> parameterNames = new HashMap<String, List<String>>();
//	
//	static {
//		parameterNames.put("foo(...)", Arrays.asList("a", "b", "c"));
//	}
}
