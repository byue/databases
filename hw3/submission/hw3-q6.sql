-- Rows: 4

-- Times: 2 seconds

-- First 20 rows:

-- carrier
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America

SELECT DISTINCT c.name AS carrier
FROM CARRIERS AS c
WHERE c.cid IN (SELECT f.carrier_id
				FROM FLIGHTS f
				WHERE f.origin_city = 'Seattle WA' AND f.dest_city = 'San Francisco CA');
