package foo.bar.queries;

import java.util.List;

// @FunctionalInterface, maybe
// like in:
/*
 * public class main {

    @FunctionalInterface
    interface Function3 <A, B, C, R> { 
    //R is like Return, but doesn't have to be last in the list nor named R.
        public R apply (A a, B b, C c);
    }

    public static void main (String [] args) {
        Function3 <String, Integer, Double, Double> multiAdder = (a, b, c) -> {
            return Double.parseDouble (a) + b + c;
        };
        System.out.println (multiAdder.apply ("22.4", 2, 40.0));
    }
}
 */
public interface Query1<R> {
	public List<R> result(); // Actually Seq....
	
	// public R single(); // or null
	// public R singleOption();
	// count
	// 
	// ??? drop, filter ? map, flatMap, take ... 
}

// TODO: Tuple<Optional<Something> and I'm not sure about Tuple<@Nullable something>