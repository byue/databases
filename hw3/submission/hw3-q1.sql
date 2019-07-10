-- Rows: 329

-- Times: 11 seconds

-- First 20 rows:

-- origin_city,dest_city,time
-- Aberdeen SD,Minneapolis MN,106
-- Abilene TX,Dallas/Fort Worth TX,111
-- Adak Island AK,Anchorage AK,471.37
-- Aguadilla PR,New York NY,368.76
-- Akron OH,Atlanta GA,408.69
-- Albany GA,Atlanta GA,243.45
-- Albany NY,Atlanta GA,390.31
-- Albuquerque NM,Houston TX,492.81
-- Alexandria LA,Atlanta GA,391.05
-- Allentown/Bethlehem/Easton PA,Atlanta GA,456.95
-- Alpena MI,Detroit MI,80
-- Amarillo TX,Houston TX,390.73
-- Anchorage AK,Barrow AK,490.01
-- Appleton WI,Atlanta GA,405.07
-- Arcata/Eureka CA,San Francisco CA,476.89
-- Asheville NC,Chicago IL,279.81
-- Ashland WV,Cincinnati OH,84
-- Aspen CO,Los Angeles CA,304.59
-- Atlanta GA,Honolulu HI,649
-- Atlantic City NJ,Fort Lauderdale FL,212

WITH FlightMax AS
	(SELECT f1.origin_city, MAX(f1.actual_time) AS maxTime
	 FROM FLIGHTS AS f1
	 GROUP BY f1.origin_city)
SELECT DISTINCT f2.origin_city AS origin_city, f2.dest_city AS dest_city, f2.actual_time AS time
FROM FLIGHTS AS f2, FlightMax AS f3
WHERE f2.origin_city = f3.origin_city AND f2.actual_time = f3.maxTime
ORDER BY f2.origin_city, f2.dest_city;