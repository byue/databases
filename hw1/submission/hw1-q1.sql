-- CSE 344 HW1 Q1
-- Bryan Yue
.header on
.mode column
.nullvalue NULL

-- Create a table Edges(Source, Destination) where both Source and Destination are integers.
CREATE TABLE Edges(Source int, 
	               Destination int, 
	               PRIMARY KEY (Source, Destination));

-- Insert the tuples (10,5), (6,25), (1,3), and (4,4)
INSERT INTO Edges values(10, 5);
INSERT INTO Edges values(6, 25);
INSERT INTO Edges values(1, 3);
INSERT INTO Edges values(4, 4);

-- Write a SQL statement that returns all tuples.
SELECT * 
  FROM Edges;

-- Write a SQL statement that returns only column Source for all tuples.
SELECT Source 
  FROM Edges;

-- Write a SQL statement that returns all tuples where Source > Destination.
SELECT * 
  FROM Edges
 WHERE Source > Destination;

-- Now insert the tuple ('-1','2000'). 
-- Do you get an error? Why? This is a tricky question, 
-- you might want to check the documentation.
INSERT INTO Edges values('-1', '2000');

-- We don't get an error even though we inserted string constants since sqlite uses
-- a dynamic typing system. "The datatype of the value is associated with the value itself,
-- not with its container. (Official SQLite Documentation)."