import edu.tufts.hrilab.fol.Symbol;
import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.slug.common.Utterance;

() = listen() {
    Utterance !utterance; 

    while (true) {
        // wait for input utterance
        op:log("debug", "Waiting for utterance.");
        !utterance = act:waitForUtterance();
        op:log("debug", "Received utterance: !utterance");

        // to exit while loop during shutdown
        if (op:isNull(!utterance)) {
            op:log("warn", "Received null utterance. Exiting listen action.");
            return;
        }

        // asynchronously pass utterance to parseGoals and immediately go back to listening for new utterances
        async {
            op:log("debug", "Calling handleSpeechInput...");
            act:handleSpeechInput(!utterance);
            op:log("debug", "... done calling handleSpeechInput.");
        }
    }
}


() = handleSpeechInput(Utterance ?utterance) {

    Symbol !speaker; 
    Symbol !addressee;

    op:log("debug", "handleSpeechInput for utterance: ?utterance");

    // parse goals
    op:log("debug", "calling parser ...");
    act:parseGoals(?utterance);
    op:log("debug", "parser results: ?utterance");





}