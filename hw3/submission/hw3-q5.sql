-- Rows: 4

-- Times: 3 minutes 52 seconds

-- First 20 rows:

-- city
-- Devils Lake ND
-- Hattiesburg/Laurel MS
-- St. Augustine FL
-- Victoria TX

WITH DIRECT_DESTINATIONS AS
	(SELECT DISTINCT f1.dest_city as destination
		 FROM FLIGHTS AS f1
		 WHERE f1.origin_city = 'Seattle WA'),
	ONE_STOP_DESTIONATIONS AS
	(SELECT DISTINCT f3.dest_city as destination
	     FROM FLIGHTS AS f2
	     INNER JOIN FLIGHTS AS f3 ON f2.dest_city = f3.origin_city
	     WHERE f2.origin_city = 'Seattle WA')
SELECT DISTINCT f5.origin_city AS city
    FROM FLIGHTS AS f5
    WHERE f5.origin_city NOT IN (SELECT d1.destination 
    							 FROM DIRECT_DESTINATIONS AS d1)
      AND f5.origin_city NOT IN (SELECT d2.destination 
    							 FROM ONE_STOP_DESTIONATIONS AS d2);
