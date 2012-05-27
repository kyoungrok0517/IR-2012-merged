import java.util.List;

import com.joogle.utility.BingSearchHelper;


public class Test {
	public static void main(String args[]) {
		List<String> expanded_terms = BingSearchHelper.getSuggestedTerms("jaguar");
		
		for (String et : expanded_terms) {
			System.out.println(et);
		}
	}
}
