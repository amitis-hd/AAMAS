package edu.tufts.hrilab.config.boxbot;

import edu.tufts.hrilab.diarc.DiarcConfiguration;
import edu.tufts.hrilab.action.GoalManagerComponent;
import edu.tufts.hrilab.boxbot.BoxBotComponent;

import ai.thinkingrobots.trade.TRADE;
import ai.thinkingrobots.trade.TRADEException;

import edu.tufts.hrilab.simspeech.SimSpeechRecognitionComponent;
import edu.tufts.hrilab.simspeech.SimSpeechProductionComponent;
import edu.tufts.hrilab.slug.nlg.SimpleNLGComponent;
import edu.tufts.hrilab.slug.parsing.tldl.TLDLParserComponent;
import edu.tufts.hrilab.slug.pragmatics.PragmaticsComponent;
import edu.tufts.hrilab.slug.refResolution.ReferenceResolutionComponent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for running the DIARC part of the BoxBot simulation.
 */
public class BoxBotConfig extends DiarcConfiguration {

  protected static Logger log = LoggerFactory.getLogger(BoxBotConfig.class);
    
  // start the configuration
  @Override
  public void runConfiguration() {
    createInstance(BoxBotComponent.class);
    
    createInstance(edu.tufts.hrilab.llm.LLMComponent.class, "-service openai");
    
    createInstance(edu.tufts.hrilab.slug.listen.ListenerComponent.class);
    createInstance(edu.tufts.hrilab.llm.LLMGoalParser.class);
    //createInstance(edu.tufts.hrilab.slug.dialogue.DialogueComponent.class);
    createInstance(SimpleNLGComponent.class);
    String gmArgs = "-beliefinitfile agents/boxbot.pl " +
            "-selector edu.tufts.hrilab.action.selector.GoalPlanningActionSelector " +
            "-asl domains/boxbot.asl parseGoals.asl " + 
            "-goal listen(self)";

    
    createInstance(GoalManagerComponent.class, gmArgs);
    //createInstance(edu.tufts.hrilab.slug.parsing.llm.LLMParserComponent.class, "-service parseGoals");
  }
}
