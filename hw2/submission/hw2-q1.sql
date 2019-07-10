-- CSE 344 HW2 Q1
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT DISTINCT f.flight_num AS flight_num
	FROM FLIGHTS as f
	INNER JOIN CARRIERS as c ON f.carrier_id = c.cid
	INNER JOIN WEEKDAYS as w ON f.day_of_week_id = w.did
	WHERE f.origin_city = 'Seattle WA' 
		AND f.dest_city = 'Boston MA' 
		AND w.day_of_week = 'Monday' 
		AND c.name = 'Alaska Airlines Inc.';
