-- Rows: 327

-- Times: 12 seconds

-- First 20 rows:

-- origin_city,percentage
-- Guam TT,NULL
-- Pago Pago TT,NULL
-- Aguadilla PR,0.286792452830
-- Anchorage AK,0.316562778272
-- San Juan PR,0.335439168534
-- Charlotte Amalie VI,0.392700729927
-- Ponce PR,0.403225806451
-- Fairbanks AK,0.495391705069
-- Kahului HI,0.533411833971
-- Honolulu HI,0.545336955115
-- San Francisco CA,0.552237084870
-- Los Angeles CA,0.554127883447
-- Seattle WA,0.574109328256
-- New York NY,0.605324373223
-- Long Beach CA,0.617199790246
-- Kona HI,0.629527991218
-- Newark NJ,0.633675652545
-- Plattsburgh NY,0.640000000000
-- Las Vegas NV,0.644710061799
-- Christiansted VI,0.646666666666

WITH FlightsShorter AS
	     (SELECT f1.origin_city AS origin_city, COUNT(*) AS num_flights_less
		 FROM FLIGHTS AS f1
		 WHERE f1.actual_time IS NOT NULL AND f1.actual_time < 180
		 GROUP BY f1.origin_city),
	 TotalFlights AS
	    (SELECT f2.origin_city AS origin_city, COUNT(*) AS num_flights_total
		 FROM FLIGHTS AS f2
		 GROUP BY f2.origin_city)
SELECT f3.origin_city AS origin_city, 
       1.0 * FlightsShorter.num_flights_less / f3.num_flights_total AS percentage
FROM TotalFlights AS f3
LEFT OUTER JOIN FlightsShorter ON FlightsShorter.origin_city = f3.origin_city
ORDER BY 1.0 * FlightsShorter.num_flights_less / f3.num_flights_total;
