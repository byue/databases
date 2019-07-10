-- CSE 344 HW2 Q8
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT c.name AS name, SUM(f.departure_delay) AS delay
	FROM FLIGHTS AS f
	INNER JOIN MONTHS AS m ON f.month_id = m.mid
	INNER JOIN CARRIERS AS c ON f.carrier_id = c.cid
	GROUP BY c.name, m.month;
