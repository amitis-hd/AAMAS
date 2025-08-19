from typing import Union, TypedDict, Tuple
from shapely import Polygon, Point
from sprites import Robot, Box, Chest

Circle = TypedDict("Circle", {"center": Tuple[int, int], "radius": int})
Rect = TypedDict("Rect", {"topLeft": Tuple[int, int], "width": int, "height": int})

RobotShape = TypedDict("RobotShape", {"robot": Robot, "topLeft": Tuple[int, int]})
BoxShape = TypedDict("BoxShape", {"box": Box, "topLeft": Tuple[int, int]})
ChestShape = TypedDict("ChestShape", {"chest": Chest, "topLeft": Tuple[int, int]})

Shape = Union[Circle, Rect, RobotShape, BoxShape, ChestShape]


# Helper function to convert shapes to shapely polygons
def shape_to_polygon(shape: Shape):
    if "robot" in shape:
        robot_rect = shape["robot"].rect
        shape = Rect(
            topLeft=shape["topLeft"],
            width=robot_rect.width,
            height=robot_rect.height
        )
    elif "box" in shape:
        box_rect = shape["box"].rect
        shape = Rect(
            topLeft=shape["topLeft"],
            width=box_rect.width,
            height=box_rect.height
        )
    elif "chest" in shape:
        chest_rec = shape["chest"].rect
        shape = Rect(
            topLeft=shape["topLeft"],
            width=chest_rec.width,
            height=chest_rec.height
        )
    if "radius" in shape:
        # If the shape is a circle
        center = Point(shape['center'])
        return center.buffer(shape['radius'])  # Create circular polygon
    elif "width" in shape and "height" in shape:
        # If the shape is a rectangle
        x, y = shape['topLeft']
        width, height = shape['width'], shape['height']
        return Polygon([(x, y), (x + width, y), (x + width, y + height), (x, y + height)])
    else:
        raise ValueError("Unknown shape type")

# Generalized overlap percentage calculation function
def calculate_overlap_percentage(shape1: Shape, shape2: Shape) -> float:
    # Convert both shapes to shapely polygons
    polygon1 = shape_to_polygon(shape1)
    polygon2 = shape_to_polygon(shape2)
    
    # Calculate the intersection area
    intersection_area = polygon1.intersection(polygon2).area
    
    # Calculate the area of the first shape
    shape1_area = polygon1.area
    
    if shape1_area == 0:
        return 0
     
    # Calculate the percentage of the first shape that overlaps with the second shape
    overlap_percentage = (intersection_area / shape1_area) * 100
    
    return overlap_percentage