-- CSE 344 HW1 Q4
-- Bryan Yue
.nullvalue NULL

-- Write a SQL query that returns all restaurants in your table. Experiment with a few of SQLite's 
-- output formats and show the command you use to format the output along with your query:

-- comma-separated form with headers
.header on
.mode csv
SELECT * FROM MyRestaurants;
.print ''

-- comma-separated form without headers
.header off
SELECT * FROM MyRestaurants;
.print ''

-- list form delimted by "|" with headers
.header on
.mode list
SELECT * FROM MyRestaurants;
.print ''

-- list form delimted by "|" without headers
.header off
SELECT * FROM MyRestaurants;
.print ''

-- column form with width 15 with headers
.header on
.mode column
.width 15 15 15 15 15
SELECT * FROM MyRestaurants;
.print ''

-- column form with width 15 without headers
.header off
SELECT * FROM MyRestaurants;
.print ''