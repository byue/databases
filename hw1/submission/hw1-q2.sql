-- CSE 344 HW1 Q2
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Create a table called MyRestaurants with the following attributes
-- (you can pick your own names for the attributes, just make sure it is clear which one is for which): 
-- Name of the restaurant: a varchar field
-- Type of food they make: a varchar field
-- Distance (in minutes) from your house: an int
-- Date of your last visit: a varchar field, interpreted as date
-- Whether you like it or not: an int, interpreted as a Boolean

-- varchar length does not matter, we just indicate known lengths for style purposes
CREATE TABLE MyRestaurants(restaurant_name varchar PRIMARY KEY, 
	                       food_type varchar,
	                       distance_in_minutes int,
	                       date_last_visit varchar(10),
	                       likeable int);