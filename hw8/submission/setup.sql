-- add all your SQL setup statements here. 

-- You can assume that the following base table has been created with data loaded for you when we test your submission 
-- (you still need to create and populate it in your instance however),
-- although you are free to insert extra ALTER COLUMN ... statements to change the column 
-- names / types if you like.

-- CREATE TABLE FLIGHTS
-- (
--  fid int NOT NULL PRIMARY KEY,
--  year int,
--  month_id int,
--  day_of_month int,
--  day_of_week_id int,
--  carrier_id varchar(3),
--  flight_num int,
--  origin_city varchar(34),
--  origin_state varchar(47),
--  dest_city varchar(34),
--  dest_state varchar(46),
--  departure_delay double precision,
--  taxi_out double precision,
--  arrival_delay double precision,
--  canceled int,
--  actual_time double precision,
--  distance double precision,
--  capacity int,
--  price double precision
--)

CREATE TABLE USERS(
	username varchar(20) NOT NULL PRIMARY KEY,
	password varchar(20),
	balance double precision
);

CREATE TABLE BOOKINGS(
	fid int NOT NULL,
	reservations int NOT NULL,
	FOREIGN KEY (fid) References FLIGHTS(fid),
	UNIQUE(fid),
	CHECK (reservations >= 0)
);

create INDEX book_index ON BOOKINGS(fid);

-- no foreign key constraint on fid2 is -1 if reservation is direct
CREATE TABLE RESERVATIONS(
	rid int IDENTITY(1, 1) PRIMARY KEY,
	username varchar(20) NOT NULL,
	fid1 int NOT NULL,
	fid2 int NOT NULL,
	paid int NOT NULL,
	FOREIGN KEY (username) References USERS(username),
	FOREIGN KEY (fid1) References FLIGHTS(fid),
	CHECK (fid1 <> fid2)
);
