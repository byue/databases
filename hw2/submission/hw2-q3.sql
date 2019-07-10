-- CSE 344 HW2 Q3
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT w.day_of_week AS day_of_week, AVG(f.arrival_delay) AS delay
	FROM FLIGHTS as f
	INNER JOIN WEEKDAYS as w ON f.day_of_week_id = w.did
	GROUP BY w.day_of_week
	ORDER BY AVG(f.arrival_delay) DESC
	LIMIT 1;
