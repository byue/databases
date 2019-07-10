# CSE 344 Homework 7: Entity Relationship Diagrams and Conceptual Design

**Objectives:**
To be able to translate from entity relationship diagrams to a relational database, and to understand functional dependencies and normal forms.

**Assignment tools:**
- Pen and paper or any drawing tools you prefer (e.g., powerpoint, [draw.io](https://www.draw.io))

**Assigned date:** Feb 28, 2018

**Due date:** Mar 7, 2018

**What to turn in:**
- `hw7.pdf`: A pdf containing solutions to parts one and two.
**Resources:** 

Textbook chapter 3.1-3.4, 4.1-4.6

## Assignment Details

### Part 1: Theory

1. (10 points) Design an E/R diagram for geography that contains the following kinds of objects or entities together with the listed attributes. 

    Model the relationships between the objects with edges. Note that edges between entities can be labeled with constraints. Make sure to label only the/those primary key(s) that is/are mentioned below.

    **Entities**
    - countries (with attributes): name, area, population, gdp ("gross domestic product")
      - a country's name uniquely identifies the country within all countries
    - cities: name, population, longitude, latitude
      - a city is uniquely identified by its (longitude, latitude) (not by name, since for instance there are 41 different cities and towns are named Springfield in the US!)
    - rivers: name, length
    - seas: name, max depth
      - rivers and seas are uniquely identified within all water entities by their name (e.g., "Ganges" would be a unique water entity)

    **Relationships:**
    - each city belongs to exactly one country
    - each river crosses one or several countries
    - each country can be crossed by zero or several rivers
    - each river ends in either a river or in a sea

    You can draw your diagrams on paper and scan them, take *quality* pictures of your drawn diagram, or use your favorite drawing tool such as Powerpoint, Keynote, or [draw.io](https://www.draw.io/). (FYI: Google Slides lacks a few shapes that you might need such as rounded arrows.)


2. (20 points) Consider the following E/R diagram: 

    ![](figs/hw7-er/Slide1.jpg)
    
    *License plate* can have both letters and numbers;  *driverID* and *Social Security* contain only numbers;  *maxLiability* is a real number;  *year*, *phone* are integers; everything else are strings.
    - Translate the diagram above by writing the SQL `CREATE TABLE` statements to represent this E/R diagram. Include all key constraints; you should specify both primary and foreign keys. Make sure that your statements are syntactically correct (you might want to check them using sqlite / Azure for instance). (10 points)
    - Which relation in your relational schema represents the relationship "insures" in the E/R diagram and why is that your representation? (5 points)
    - Compare the representation of the relationships "drives" and "operates" in your schema, and explain why they are different. (5 points)



## Submission Instructions

Write your answers in a file `hw7.pdf`.

**Important**: To remind you, in order for your answers to be added to the git repo, 
you need to explicitly add each file:

```sh
$ git add hw7.pdf ...
```

**Again, just because your code has been committed on your local machine does not mean that it has been 
submitted -- it needs to be on GitLab!**

Use the same bash script `turnInHw.sh` in the root level directory of your repository that 
commits your changes, deletes any prior tag for the current lab, tags the current commit,
and pushes the branch and tag to GitLab. 

If you are using Linux or Mac OSX, you should be able to run the following:

```sh
$ ./turnInHw.sh hw7
```

Like previous assignments, make sure you check the results afterwards to make sure that your file(s)
have been committed.

If this doesn't work, you can use git to add, commit, tag, and then push your commit/tags yourself.
