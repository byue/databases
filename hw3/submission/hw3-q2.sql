-- Rows: 109

-- Times: 8 seconds

-- First 20 rows:

-- city
-- Aberdeen SD
-- Abilene TX
-- Alpena MI
-- Ashland WV
-- Augusta GA
-- Barrow AK
-- Beaumont/Port Arthur TX
-- Bemidji MN
-- Bethel AK
-- Binghamton NY
-- Brainerd MN
-- Bristol/Johnson City/Kingsport TN
-- Butte MT
-- Carlsbad CA
-- Casper WY
-- Cedar City UT
-- Chico CA
-- College Station/Bryan TX
-- Columbia MO
-- Columbus GA

SELECT DISTINCT f1.origin_city AS city
FROM FLIGHTS AS f1
WHERE NOT EXISTS (SELECT f2.actual_time
                  FROM FLIGHTS as f2
                  WHERE f1.origin_city = f2.origin_city 
                    AND f2.actual_time >= 180)
ORDER BY f1.origin_city;
