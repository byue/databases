-- CSE 344 HW2 Q6
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

SELECT c.name AS carrier, AVG(f.price) AS average_price
	FROM FLIGHTS AS f
	INNER JOIN CARRIERS AS c ON f.carrier_id = c.cid
	INNER JOIN MONTHS AS m ON f.month_id = m.mid
	WHERE ((f.origin_city = 'Seattle WA' AND f.dest_city = 'New York NY') OR
		  (f.origin_city = 'New York NY' AND f.dest_city = 'Seattle WA'))
	GROUP BY c.name, m.month;
