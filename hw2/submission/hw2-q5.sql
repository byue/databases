-- CSE 344 HW2 Q5
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT c.name AS name, (100.0 * SUM(f.canceled) / COUNT(*)) as percent 
	FROM FLIGHTS AS f
	INNER JOIN CARRIERS as c ON f.carrier_id = c.cid
	WHERE f.origin_city = 'Seattle WA'
	GROUP BY c.name
	HAVING (100.0 * SUM(f.canceled) / COUNT(*)) > 0.5
	ORDER BY (100.0 * SUM(f.canceled) / COUNT(*)) ASC;
