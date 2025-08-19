name(self, boxbot).
name(boxbot, boxbot).

actor(amitis).

diarcAgent(self).
diarcAgent(boxbot).
team(self).

memberOf(X, X).
memberOf(boxbot, self).

object(boxbot, agent).
object(self, agent).
object(floor, surface).
object(box, object).
object(lightButton, lightButton).
object(chest, object).
object(door, object).

subtype(surface, physical).
subtype(physical, object).
subtype(lightButton, physical).
subtype(box, physical).
subtype(chest, physical).
subtype(agent, physical).
subtype(door, physical).

% The lightButton is fixed on the floor
property(lightButton, fixed_on, floor).

% Box properties
object(box, box).
property(box, has_weight, true).
property(box, weight_class, medium).
property(box, can_be_carried_by, agent).

% chest properties
object(chest, chest).
property(chest, has_weight, true).
property(chest, weight_class, high).


admin(admin).
supervisor(amitis).
admin(amitis).