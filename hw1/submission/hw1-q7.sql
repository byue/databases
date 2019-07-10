-- CSE 344 HW1 Q7
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Write a SQL query that returns 
-- all restaurants that are within and including 10 mins from your house.
  SELECT *
    FROM MyRestaurants
   WHERE distance_in_minutes <= 10
ORDER BY restaurant_name ASC;