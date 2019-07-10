-- CSE 344 HW2 Importing The Flights Database
-- Bryan Yue
PRAGMA foreign_keys=ON;

CREATE TABLE CARRIERS(cid VARCHAR(3) PRIMARY KEY,
	                  name VARCHAR(83));

CREATE TABLE MONTHS(mid INTEGER PRIMARY KEY,
	                month VARCHAR(9));

CREATE TABLE WEEKDAYS(did INTEGER PRIMARY KEY,
	                  day_of_week VARCHAR(9));

CREATE TABLE FLIGHTS(fid INTEGER PRIMARY KEY,
	                 year INTEGER,
	                 month_id INTEGER,
	                 day_of_month INTEGER,
	                 day_of_week_id INTEGER,
	                 carrier_id VARCHAR(3),
	                 flight_num INTEGER,
	                 origin_city VARCHAR(34),
	                 origin_state VARCHAR(47),
	                 dest_city VARCHAR(34),
	                 dest_state VARCHAR(46),
	                 departure_delay REAL,
	                 taxi_out REAL, 
	                 arrival_delay REAL,
                     canceled INTEGER, 
                     actual_time REAL, 
                     distance REAL, 
                     capacity INTEGER, 
                     price REAL,
                     FOREIGN KEY(carrier_id) REFERENCES CARRIERS(cid),
                     FOREIGN KEY(month_id) REFERENCES MONTHS(mid),
                     FOREIGN KEY(day_of_week_id) REFERENCES WEEKDAYS(did));

-- Import data to created tables
.mode csv
--.import ../starter-code/carriers.csv CARRIERS
--.import ../starter-code/months.csv MONTHS
--.import ../starter-code/weekdays.csv WEEKDAYS
--.import ../starter-code/flights-small.csv FLIGHTS
.import ./carriers.csv CARRIERS
.import ./months.csv MONTHS
.import ./weekdays.csv WEEKDAYS
.import ./small.csv FLIGHTS
