package foo.bar.internal;

import java.util.List;

import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

public class InternalQuery {
	String id; // ??
	String query;
	QueryMetadata queryMetadata;
	ParameterList<ParameterDescription.InDefinedShape> methodParameters; // hm ....
	List<QueryParameter> parameters;
	// TODO: querymappings, with result type
}
