package foo.bar.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import foo.bar.QueryMetadata;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

// TODO: wouldn't be nice: $query(5) ??? yeah, but needs special handling like
// hm, actually no, query(5), query("5"), even query(five) if five is defined somehow
// $query(`literal`5)
// $query($#5), hmm too complicated
// $query(`5`)
// $query(...)

// should I rewrite all this internal classes as interfaces ?

// FIXME: should be a query graph,
// but right now, just one pass top down
public class QueryEnvironment {
	
	// todo: maybe Map<String, InternalQuery>;
    public Map<String, InternalQuery> environment = new HashMap<>();
    
    public InternalQuery resolve(String path) { // return null, should return validation, don't throw up exception here and there
      throw new RuntimeException("not implemented");
    }
	
//	public static Map<String, List<String>> parameterNames = new HashMap<String, List<String>>();
//	
//	static {
//		parameterNames.put("foo(...)", Arrays.asList("a", "b", "c"));
//	}
}
