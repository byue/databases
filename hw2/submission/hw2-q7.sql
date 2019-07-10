-- CSE 344 HW2 Q7
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT sum(f.capacity) AS capacity
	FROM FLIGHTS AS f
	INNER JOIN MONTHS AS m ON f.month_id = m.mid
	WHERE (((f.origin_city = 'Seattle WA' AND f.dest_city = 'San Francisco CA') OR
		    (f.origin_city = 'San Francisco CA' AND f.dest_city = 'Seattle WA')) AND
		     m.month = 'July' AND f.day_of_month = 10 AND f.year = 2015);
