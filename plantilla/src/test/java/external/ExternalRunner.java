package external;

import com.intuit.karate.junit5.Karate;
import com.intuit.karate.*;

class ExternalRunner {
    
    @Karate.Test
    Karate testLogin() {
        return Karate.run("login").relativeTo(getClass());
    }    
}
