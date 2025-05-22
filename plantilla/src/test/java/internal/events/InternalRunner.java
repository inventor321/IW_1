package internal.events;

import com.intuit.karate.junit5.Karate;

class InternalRunner {
    
    @Karate.Test
    Karate testEvent() {
        return Karate.run("event").relativeTo(getClass());
    }      
}
