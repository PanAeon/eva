package foo.bar.internal;

import org.apache.commons.lang3.builder.ToStringBuilder;

//FIXME: delete this shit and start anew
public class QueryMetadata {

	public int nParams; // java.sql.Types
	public int[] paramTypes;
	public String[] paramClassNames;

	public int nResults;
	public int[] resultTypes;
	public String[] resultClassNames;
	public String[] resultNames;
	public String[] tableNames;

	@Override
		public String toString() {
			return  ToStringBuilder.reflectionToString(this);
//			return "QueryMetadata(" + "\nnParams: " + nParams + "\nparamTypes: " + Arrays.toString(paramTypes)
//			        + "\nparamClasses: " + Arrays.toString(paramClassNames)
//					+ "\nnResults: " + nResults + "\nresultTypes: " + Arrays.toString(resultTypes) + "\nresultClassNames: "
//					+ Arrays.toString(resultClassNames) + "\nresultNames: "
//					+ Arrays.toString(resultNames) + "\ntableNames: " + Arrays.toString(tableNames) + "\n)";
}
}
