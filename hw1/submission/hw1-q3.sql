-- CSE 344 HW1 Q3
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Insert at least five tuples using the SQL INSERT command five (or more) times. 
-- You should insert at least one restaurant you liked, at least one restaurant you did not like, 
-- and at least one restaurant where you leave the “I like” field NULL.
INSERT INTO MyRestaurants values('McDonalds', 'fast_food', 5, '2018-01-15', 1);
INSERT INTO MyRestaurants values('Subway', 'sandwiches', 10, '2018-01-08', 0);
INSERT INTO MyRestaurants values('Olive_Garden', 'Italian', 25, '2018-01-12', NULL);
INSERT INTO MyRestaurants values('Top_Gun', 'Chinese', 30, '2018-01-04', 1);
INSERT INTO MyRestaurants values('Panera_Bread', 'Cafe', 20, '2018-01-01', 0);
INSERT INTO MyRestaurants values('Jack_in_the_Box', 'fast_food', 5, '2017-09-15', 1);
INSERT INTO MyRestaurants values('Haiku', 'Japanese', 23, '2017-09-12', NULL);
INSERT INTO MyRestaurants values('Outback_Steakhouse', 'steak', 23, '2017-10-28', 1);
INSERT INTO MyRestaurants values('Thaiger_Room', 'Thai', 45, '2017-10-01', 1);