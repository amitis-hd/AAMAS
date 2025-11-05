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

subtype(surface, physical).
subtype(physical, object).
subtype(agent, physical).

admin(admin).
supervisor(amitis).
admin(amitis).


subtype(surface, physical).
subtype(physical, object).




% lightButton properties
subtype(lightButton, physical).
object(lightButton, object).
property(lightButton, fixed_on, floor).

% Box properties
subtype(box, physical).
object(box, object).
object(box, box).
property(box, has_weight, true).
property(box, weight_class, medium).
property(box, can_be_carried_by, agent).

% chest properties
object(chest, object).
subtype(chest, physical).
object(chest, chest).
property(chest, has_weight, true).
property(chest, weight_class, high).
property(box, can_be_carried_by, agent).


% Door properties
object(door, object).
object(door, door).
subtype(door, physical).
property(door, has_part, keyhole).
subtype(keyhole, physical).
property(keyhole, cavity).


% ========================================
% BOOKSHELF SCENARIO OBJECTS
% ========================================

% Bookcase - immovable structure
object(bookcase, object).
subtype(bookcase, physical).
property(bookcase, fixed_on, floor).
property(bookcase, has_weight, true).
property(bookcase, weight_class, immovable).
property(bookcase, has_part, shelf).
property(bookcase, tightly_packed, true).

% Books on shelf 
object(book1, object).
subtype(book1, physical).
property(book1, fixed_on, bookcase).
property(book1, has_weight, true).
property(book1, weight_class, medium).

object(book2, object).
subtype(book2, physical).
property(book2, fixed_on, bookcase).
property(book2, has_weight, true).
property(book2, weight_class, medium).

% Gap between books
property(bookcase, has_gap_between, [book1, book2]).
property([book1, book2], gap_width, narrow).

% Broom (without bristles)
object(broom, object).
subtype(broom, physical).
subtype(broom, tool).
property(broom, material, wood).
property(broom, has_weight, true).
property(broom, weight_class, light).
property(broom, can_be_carried_by, agent).
property(broom, length, long).


% Mouse pad
object(mousepad, object).
subtype(mousepad, physical).
subtype(mousepad, tool).
property(mousepad, has_weight, true).
property(mousepad, weight_class, light).
property(mousepad, can_be_carried_by, agent).
property(mousepad, flexible, true).

% Retractable pen
object(pen, object).
subtype(pen, physical).
subtype(pen, tool).
property(pen, has_weight, true).
property(pen, weight_class, light).
property(pen, can_be_carried_by, agent).
property(pen, length, short).
property(pen, retractable, true).

% Rolling pin
object(rollingpin, object).
subtype(rollingpin, physical).
subtype(rollingpin, tool).
property(rollingpin, material, wood).
property(rollingpin, has_weight, true).
property(rollingpin, weight_class, medium).
property(rollingpin, can_be_carried_by, agent).
property(rollingpin, cylindrical, true).


% Metal spatula 
object(spatula, object).
subtype(spatula, physical).
subtype(spatula, tool).
property(spatula, has_weight, true).
property(spatula, weight_class, light).
property(spatula, can_be_carried_by, agent).
property(spatula, material, metal).
property(spatula, flexible, false).
property(spatula, width, wide).


% Wool blanket
object(blanket, object).
subtype(blanket, physical).
property(blanket, material, wool).

% --- Beliefs for fork ---
object(fork, object).
subtype(fork, physical).
property(fork, has_weight, true).
property(fork, weight_class, light).
property(fork, can_be_carried_by, agent).
property(fork, is_tool, true).

% --- Beliefs for suctioncup ---
object(suctioncup, object).
subtype(suctioncup, physical).
property(suctioncup, has_weight, true).
property(suctioncup, weight_class, light).
property(suctioncup, can_be_carried_by, agent).
property(suctioncup, is_tool, true).
property(suctioncup, can_attach_to_surface, true).

% --- Beliefs for magnet ---
object(magnet, object).
subtype(magnet, physical).
property(magnet, has_weight, true).
property(magnet, weight_class, light).
property(magnet, can_be_carried_by, agent).
property(magnet, is_tool, true).
property(magnet, is_magnetic, true).
property(magnet, can_attract_metal, true).

% --- Beliefs for shoelace ---
object(shoelace, object).
subtype(shoelace, physical).
property(shoelace, has_weight, true).
property(shoelace, weight_class, light).
property(shoelace, can_be_carried_by, agent).
property(shoelace, is_flexible, true).
property(shoelace, is_rope_like, true).

% --- Beliefs for paperclip ---
object(paperclip, object).
subtype(paperclip, physical).
property(paperclip, has_weight, true).
property(paperclip, weight_class, light).
property(paperclip, can_be_carried_by, agent).
property(paperclip, is_metal, true).
property(paperclip, is_bendable, true).

% --- Beliefs for drawer ---
object(drawer, object).
subtype(drawer, physical).
subtype(drawer, container).
property(drawer, can_open, true).
property(drawer, can_close, true).
property(drawer, can_contain_items, true).
property(drawer, is_immovable, true).


% Garden hose
object(gardenhose, object).
subtype(gardenhose, physical).
subtype(gardenhose, tool).
property(gardenhose, has_weight, true).
property(gardenhose, weight_class, medium).
property(gardenhose, can_be_carried_by, agent).
property(gardenhose, is_flexible, true).
property(gardenhose, length, long).
property(gardenhose, has_holes).
property(gardenhose, leaks_through_holes).

% Large soda bottle
object(sodabottle, object).
subtype(sodabottle, physical).
subtype(sodabottle, container).
property(sodabottle, has_weight, true).
property(sodabottle, weight_class, light).
property(sodabottle, can_be_carried_by, agent).
property(sodabottle, material, plastic).
property(sodabottle, transparent, true).

% Duct tape
object(ducttape, object).
subtype(ducttape, physical).
subtype(ducttape, tool).
property(ducttape, has_weight, true).
property(ducttape, weight_class, light).
property(ducttape, can_be_carried_by, agent).
property(ducttape, is_adhesive, true).
property(ducttape, is_flexible, true).

% Ladder
object(ladder, object).
subtype(ladder, physical).
subtype(ladder, tool).
property(ladder, has_weight, true).
property(ladder, weight_class, heavy).
property(ladder, can_be_carried_by, agent).
property(ladder, can_be_climbed, true).
property(ladder, provides_elevation, true).

% Hanging plants (elevated object)
object(hangingplants, object).
subtype(hangingplants, physical).
property(hangingplants, has_weight, true).
property(hangingplants, weight_class, light).
property(hangingplants, fixed_on, ceiling).
property(hangingplants, requires_elevation, true).
property(hangingplants, height_class, high).


% Gap constraints
constraint(gap_between_books, very_narrow).
