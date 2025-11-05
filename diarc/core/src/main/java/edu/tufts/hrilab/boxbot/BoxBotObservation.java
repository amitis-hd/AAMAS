/*
 * Copyright © Thinking Robots, Inc., Tufts University, and others 2024.
 */

 package edu.tufts.hrilab.boxbot;


 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ai.thinkingrobots.trade.TRADEService;
 import edu.tufts.hrilab.action.annotations.Observes;
 import edu.tufts.hrilab.fol.Symbol;
 import edu.tufts.hrilab.fol.Variable;
 import java.lang.StringBuilder;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 /**
  * Contains information about the state of the simulation at a point in time.
  */
 public class BoxBotObservation {
   private static final Logger log = LoggerFactory.getLogger(BoxBotObservation.class);
 
   
   /*************************************
    * FIELDS
    *************************************/

   /**
    * If true, the robot is holding the box.
    */

   public String itemInAgentsHand;

   public boolean isAgentPushing;

   /**
    * If true, the the light is off.
    */
   public String lights;
   /**
    * If true, the lightButton is pressed down.
    */
   public boolean islightButtonPressed;
   /**
    * If true, the box is close enough to the robot to be picked up.
    */
   public boolean isInPickupRange;

   public boolean isAtlightButton;

   public boolean isHandExtended;
   public boolean isHandRotated;
   /**
    * The position of the robot, as an (x, y) tuple.
    */
   public int[] robotPos;

   public int[] doorPos;

   public int[] windowPos;

   public int[] stoolPos;

   public int[] plantsPos;
   public int[] bottlePos;
   public int[] faucetPos;

   /**
    * The position of the lightButton, as an (x, y) tuple.
    */
   public int[] lightButtonPos;
   /**
    * The position of the box, as an (x, y) tuple.
    */
   public int[] boxPos;

   public int[] chestPos;

   public boolean chestOpen;
   /**
    * The width of the robot, in pixels.
    */
   public int robotWidth;
   /**
    * The height of the robot, in pixels.
    */
   public int robotHeight;
   /**
    * The width of the box, in pixels.
    */
   public int boxWidth;
   /**
    * The height of the box, in pixels.
    */
   public int boxHeight;

   public int bottleHeight;
   public int bottleWidth;
   public int faucetHeight;
   public int faucetWidth;
   public int plantsHeight;
   public int plantsWidth;
   /**
    * The width of the lightButton, in pixels.
    */
   public int lightButtonWidth;
   /**
    * The height of the lightButton, in pixels.
    */
   public int lightButtonHeight;
   /**
    * The y coordinate of the top of the door.
    */
   public int doorHeight;
   /**
    * The y coordinate of the bottom of the door.
    */
   public int doorWidth;
   /**
    * The width of each wall.
    */
   public int wallWidth;

   public boolean doorOpen;

   public int windowHeight;
   public int windowWidth;
   public int stoolHeight;
   public int stoolWidth;

   public String location;

    public int handRotation; 

   public boolean lemonPierced;
   
   /**
    * If true, an object has been moved recently.
    */
   public boolean objectMoved;
   
   /**
    * If true, the object has been squeezed.
    */
   public boolean lemonSqueezed;
   
   /**
    * If true, the mug is full.
    */
   public boolean mugFull;

   /**
    * If true, the robot is elevated (on ladder or stool).
    */
   public boolean isElevated;

   public Map<String, String> underRelationships;

 
   /*************************************
    * UTILITY FUNCTIONS
    *************************************/
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
 
     builder.append('{');
     builder.append("itemInAgentsHand: ");
     builder.append(this.itemInAgentsHand);
     builder.append(", ");
     builder.append("isHandExtended: ");
     builder.append(this.isHandExtended);
     builder.append(", ");
     builder.append("handRotation: ");
     builder.append(this.handRotation);
     builder.append(", ");
     builder.append("isAgentPushing: ");
     builder.append(this.isAgentPushing);
     builder.append(", ");
     builder.append("lights: ");
     builder.append(this.lights);
     builder.append(", ");
     builder.append("isInPickupRange: ");
     builder.append(this.isInPickupRange);
     builder.append(", ");
     builder.append("isAtlightButton: ");
     builder.append(this.isAtlightButton);
     builder.append(", ");
     builder.append("islightButtonPressed: ");
     builder.append(this.islightButtonPressed);
     builder.append(", ");
     builder.append("robotPos: [");
     builder.append(this.robotPos[0]);
     builder.append(", ");
     builder.append(this.robotPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("lightButtonPos: [");
     builder.append(this.lightButtonPos[0]);
     builder.append(", ");
     builder.append(this.lightButtonPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("boxPos: [");
     builder.append(this.boxPos[0]);
     builder.append(", ");
     builder.append(this.boxPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("chestPos: [");
     builder.append(this.chestPos[0]);
     builder.append(", ");
     builder.append(this.chestPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("doorPos: [");
     builder.append(this.doorPos[0]);
     builder.append(", ");
     builder.append(this.doorPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("windowPos: [");
     builder.append(this.windowPos[0]);
     builder.append(", ");
     builder.append(this.windowPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("stoolPos: [");
     builder.append(this.stoolPos[0]);
     builder.append(", ");
     builder.append(this.stoolPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("bottlePos: [");
     builder.append(this.bottlePos[0]);
     builder.append(", ");
     builder.append(this.bottlePos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("plantsPos: [");
     builder.append(this.plantsPos[0]);
     builder.append(", ");
     builder.append(this.plantsPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("faucetPos: [");
     builder.append(this.faucetPos[0]);
     builder.append(", ");
     builder.append(this.faucetPos[1]);
     builder.append("]");
     builder.append(", ");
     builder.append("robotWidth: ");
     builder.append(this.robotWidth);
     builder.append(", ");
     builder.append("robotHeight: ");
     builder.append(this.robotHeight);
     builder.append(", ");
     builder.append("windowWidth: ");
     builder.append(this.windowWidth);
     builder.append(", ");
     builder.append("windowHeight: ");
     builder.append(this.windowHeight);
     builder.append(", ");
     builder.append("stoolWidth: ");
     builder.append(this.stoolWidth);
     builder.append(", ");
     builder.append("stoolHeight: ");
     builder.append(this.stoolHeight);
     builder.append(", ");
     builder.append("boxWidth: ");
     builder.append(this.boxWidth);
     builder.append(", ");
     builder.append("boxHeight: ");
     builder.append(this.boxHeight);
     builder.append(", ");
     builder.append("lightButtonWidth: ");
     builder.append(this.lightButtonWidth);
     builder.append(", ");
     builder.append("lightButtonHeight: ");
     builder.append(this.lightButtonHeight);
     builder.append(", ");
     builder.append("doorHeight: ");
     builder.append(this.doorHeight);
     builder.append(", ");
     builder.append("doorWidth: ");
     builder.append(this.doorWidth);
     builder.append(", ");
     builder.append("wallWidth: ");
     builder.append(this.wallWidth);
     builder.append(", ");
     builder.append("chestOpen: ");
     builder.append(this.chestOpen);
     builder.append(", ");
     builder.append("location: ");
     builder.append(this.location);
     builder.append(", ");
     builder.append("lemonPierced: ");
     builder.append(this.lemonPierced);
     builder.append(", ");
     builder.append("objectMoved: ");
     builder.append(this.objectMoved);
     builder.append(", ");
     builder.append("lemonSqueezed: ");
     builder.append(this.lemonSqueezed);
     builder.append(", ");
     builder.append("mugFull: ");
     builder.append(this.mugFull);
     builder.append(", ");
     builder.append("isElevated: ");
     builder.append(this.isElevated);
     builder.append(", ");
     builder.append("underRelationships: ");
     builder.append(this.underRelationships);
     builder.append(", ");
     builder.append('}');
 
     return builder.toString();
   }
 
   @Override
   public boolean equals(Object other) {
     if (other == null) {
       return false;
     }
     if (other == this) {
       return true;
     }
     if (other.getClass() != getClass()) {
       return false;
     }
     BoxBotObservation obs = (BoxBotObservation) other;
     return new EqualsBuilder().append(this.itemInAgentsHand, obs.itemInAgentsHand)
             .append(this.isAgentPushing, obs.isAgentPushing)
             .append(this.lights, obs.lights)
             .append(this.isHandExtended, obs.isHandExtended)
             .append(this.handRotation, obs.handRotation)
             .append(this.islightButtonPressed, obs.islightButtonPressed)
             .append(this.isInPickupRange, obs.isInPickupRange)
             .append(this.isAtlightButton, obs.isAtlightButton)
             .append(this.robotPos, obs.robotPos)
             .append(this.lightButtonPos, obs.lightButtonPos)
             .append(this.boxPos, obs.boxPos)
             .append(this.chestPos, obs.chestPos)
             .append(this.doorPos, obs.doorPos)
             .append(this.windowPos, obs.windowPos)
             .append(this.stoolPos, obs.stoolPos)
             .append(this.plantsPos, obs.plantsPos)
             .append(this.faucetPos, obs.faucetPos)
             .append(this.bottlePos, obs.bottlePos)
             .append(this.robotWidth, obs.robotWidth)
             .append(this.robotHeight, obs.robotHeight)
             .append(this.boxWidth, obs.boxWidth)
             .append(this.boxHeight, obs.boxHeight)
             .append(this.lightButtonWidth, obs.lightButtonWidth)
             .append(this.lightButtonHeight, obs.lightButtonHeight)
             .append(this.doorHeight, obs.doorHeight)
             .append(this.doorWidth, obs.doorWidth)
             .append(this.windowHeight, obs.windowHeight)
             .append(this.windowWidth, obs.windowWidth)
             .append(this.stoolHeight, obs.stoolHeight)
             .append(this.stoolWidth, obs.stoolWidth)
             .append(this.wallWidth, obs.wallWidth)
             .append(this.chestOpen, obs.chestOpen)
             .append(this.doorOpen, obs.doorOpen)
             .append(this.location, obs.location)
             .append(this.lemonPierced, obs.lemonPierced)
             .append(this.objectMoved, obs.objectMoved)
             .append(this.lemonSqueezed, obs.lemonSqueezed)
             .append(this.mugFull, obs.mugFull)
             .append(this.isElevated, obs.isElevated)
             .append(this.underRelationships, obs.underRelationships)
             .isEquals();
   }
 
   @Override
   public int hashCode() {
     return new HashCodeBuilder().append(this.itemInAgentsHand)
             .append(this.isAgentPushing)
             .append(this.lights)
             .append(this.islightButtonPressed)
             .append(this.isInPickupRange)
             .append(this.isAtlightButton)
             .append(this.robotPos)
             .append(this.lightButtonPos)
             .append(this.boxPos)
             .append(this.doorPos)
             .append(this.windowPos)
             .append(this.plantsPos)
             .append(this.faucetPos)
             .append(this.bottlePos)
             .append(this.stoolPos)
             .append(this.chestPos)
             .append(this.robotWidth)
             .append(this.robotHeight)
             .append(this.boxWidth)
             .append(this.boxHeight)
             .append(this.lightButtonWidth)
             .append(this.lightButtonHeight)
             .append(this.doorHeight)
             .append(this.doorWidth)
             .append(this.windowHeight)
             .append(this.windowWidth)
             .append(this.stoolHeight)
             .append(this.stoolWidth)
             .append(this.plantsWidth)
             .append(this.plantsHeight)
             .append(this.faucetWidth)
             .append(this.faucetHeight)
             .append(this.bottleWidth)
             .append(this.bottleHeight)
             .append(this.wallWidth)
             .append(this.isHandExtended)
             .append(this.handRotation)
             .append(this.chestOpen)
             .append(this.doorOpen)
             .append(this.location)
             .append(this.lemonPierced)
             .append(this.objectMoved)
             .append(this.lemonSqueezed)
             .append(this.mugFull)
             .append(this.isElevated)
             .append(this.underRelationships)
             .hashCode();
   }
 }
 