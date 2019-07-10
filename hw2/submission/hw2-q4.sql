-- CSE 344 HW2 Q4
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT DISTINCT c.name AS name
	FROM FLIGHTS AS f
	INNER JOIN CARRIERS AS c ON f.carrier_id = c.cid
	INNER JOIN MONTHS AS m ON f.month_id = m.mid
	GROUP BY c.name, f.day_of_month, m.month, f.year
	HAVING count(*) > 1000;
	