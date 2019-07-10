-- Rows: 256

-- Times: 17 seconds

-- First 20 rows:

-- city
-- Dothan AL
-- Toledo OH
-- Peoria IL
-- Yuma AZ
-- Bakersfield CA
-- Daytona Beach FL
-- Laramie WY
-- North Bend/Coos Bay OR
-- Erie PA
-- Guam TT
-- Columbus GA
-- Wichita Falls TX
-- Hartford CT
-- Myrtle Beach SC
-- Arcata/Eureka CA
-- Kotzebue AK
-- Medford OR
-- Providence RI
-- Green Bay WI
-- Santa Maria CA

SELECT DISTINCT f2.dest_city AS city
    FROM FLIGHTS AS f1
    INNER JOIN FLIGHTS AS f2 ON f1.dest_city = f2.origin_city
    WHERE f1.origin_city = 'Seattle WA' AND
    	  f2.dest_city <> 'Seattle WA' AND
          f2.dest_city NOT IN (SELECT f3.dest_city AS dest_city
	                           FROM FLIGHTS AS f3
	                           WHERE f3.origin_city = 'Seattle WA');