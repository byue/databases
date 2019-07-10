-- CSE 344 HW1 Q7
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Write a SQL query that returns all restaurants that you like, but have not visited 
-- since more than 3 months ago.
  SELECT *
    FROM MyRestaurants
   WHERE likeable == 1 AND (date(date_last_visit) < date('now', '-3 month'))
ORDER BY restaurant_name ASC;