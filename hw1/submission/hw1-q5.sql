-- CSE 344 HW1 Q5
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Write a SQL query that returns only the name and distance of all restaurants within and 
-- including 20 minutes of your house. 
-- The query should list the restaurants in alphabetical order of names.
SELECT restaurant_name, distance_in_minutes
  FROM MyRestaurants
  WHERE distance_in_minutes <= 20
  ORDER BY restaurant_name ASC;