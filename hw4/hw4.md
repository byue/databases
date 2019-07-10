# CSE 344 Homework 4: Finding the Mitochondrial Eve

**Objectives:**
To master writing relational queries in a logic formalism using datalog.

**Assignment tools:**
Soufflé

**Assigned date:** January 31st, 2018

**Due date:** February 9th, 2018

**What to turn in:**  `hw4.dl` along with its output `hw4-1.ans`, `hw4-2.ans`, `hw4-3.ans`, `hw4-4.ans`, `hw4-5.ans`(see details below)

**Resources:** 

- Soufflé (https://github.com/souffle-lang/souffle)
    
- Soufflé [language documentation](http://souffle-lang.org/docs/datalog/)

- [Soufflé tutorial](http://souffle-lang.org/pdf/SoufflePLDITutorial.pdf)

- Starter code in your personal repo.

- General information:
    - The [Mitochondrial Eve](https://en.wikipedia.org/wiki/Mitochondrial_Eve)        
    - List of [women in the Bible](https://en.wikipedia.org/wiki/List_of_women_in_the_Bible)         
    - List of [minor biblical figures](https://en.wikipedia.org/wiki/List_of_minor_biblical_figures,_A%E2%80%93K)        
    - Note that the parent-child relationship is randomly generated and may change.


## Assignment Details


### Finding the Mitochondrial Eve

Every human has a mother, who had her own mother, who in turn had her own mother.  The matrilineal ancestor of an individual consists of the mother, the mother’s mother, and so on, following only the female lineage.  A matrilinial common ancestor, MCA, is a matrilinial ancestor of all living humans.  An MCA is very, very likely to exist (why?), and in fact there are many MCAs.  The matrilineal most recent ancestor, or MRCA, is the only individual (woman) who is the MCA of all living humans and is the most recent such.  Who is she?  When did she live?  In the 1980s three researchers, Cann, Stoneking and Wilson, analyzed the mitocondrial DNA of living humans and determined that the MRCA lived about 200,000 years ago.  The researchers called her the [Mithcondrial Eve](https://en.wikipedia.org/wiki/Mitochondrial_Eve).

In this homework, you will analyze a database of 800 individuals, compute several things, culminating with the the computation of the Mithocondrial Eve.  The genealogy database consists of over 800 biblical names, obtained from Wikipedia, with a randomly generated parent-child relationship.

### Getting Started

1. Install Soufflé
    1. **Mac user**
        * Download the [souffle-1.2.0.pkg](https://github.com/souffle-lang/souffle/releases/tag/1.2.0)
    2. **Windows user**
        * To ease the installation process, we recommand using the pre-built version of Soufflé on Debian
        * Download the [VMPlayer](https://my.vmware.com/en/web/vmware/free#desktop_end_user_computing/vmware_workstation_player/12_0) 
        * Download the [Debian Image](https://www.debian.org/distrib/netinst). Make sure you install the amd64 version.
        * When VMplayer starts running, click on the "Open a Virtual Machine" link.  Navigate to the folder where you sotre the Debian Image. Click "OK".  Then click on the left-side tab that appears containing the VM name. Click "Play virtual machine".
        * When Debian is setup, obtain the pre-built package [souffle_1.2.0-1_amd64.deb](https://github.com/souffle-lang/souffle/releases/tag/1.2.0)
        * Open a terminal and navigate to the location where you downloaded the package (which is probably `~/Downloads`)
        * Then type `sudo apt install ./souffle_1.2.0-1_amd64.deb`

2. Verify Soufflé is working:
    ```
    $ cd hw4/starter-code
    $ souffle hw4.dl
    ```
  
    Congratulations! You just ran your first datalog query.

### Questions
For each question below, write in the file `hw4.dl` a program that computes the answer to that question. See the Example section below.

1. (10 points) Find all descendants of Priscilla and their descriptions.  Name your predicate `p1(x,d)`. Write the output to a file called `hw4-1.ans`(123 rows)


2. (10 points) Find the woman/women with the largest number of children and the man/men with the largest number of children. For each individual, you should return the name of that individual, his/her description, and the number of children. Name your predicate `p2(x,d,n)`. Write the output to a file called `hw4-2.ans`(2 rows)


3. (20 points) For each person x, we call a "complete lineage" any sequence x0=x, x1, x2, … , xn where each person is the parent of the previous person, and the last person has no parents; the length of the sequence is n.  If x has a complete lineage of length n, then we also say that "x is in generation n".  Compute the minimum and maximum generation of each living person x. 

    Name your predicate `p3(x,m1,m2)`, where x is a living person, and `m1`, `m2` are the minimal/maximal generation. (Hint: You may want to first compute all generations for all x: think about when can you say that x is in generation 0, and when can you say that x is in generation n+1.  Of course x can be in multiple generations, e.g., x's mother is in generation 0 and x's father is in generation 2.   Once you know everybody's generations, you can answer the problem easily.) Write the output to a file called `hw4-3.ans` (22 rows)

4. (20 points) Compute all matrilineal common ancestors, MCA. Name your predicate `p4(x)`. Write the output to a file called `hw4-4.ans` (6 rows)

5. (20 points) Find the mitochondrial Eve.  Name your predicate `p5(x)`. Remember that you can utilize your predicates defined earlier. Write the output to a file called `hw4-5.ans` (1 row)


#### Example

For example, suppose the question were: find all children of Priscilla; return their names and their descriptions. Then you write this in the `hw3-q3.dl` file (it’s already there):

```c
.output p0(IO=stdout)
p0(x,d) :- parent_child("Priscilla",x), person(x,d).  //NOTE the period at the end 
```

## Submission Instructions

Write your answers in the provided file `hw4.dl` and name the output generated from p1, p2, p3, p4, p5: `hw4-1.ans`, `hw4-2.ans`, `hw4-3.ans`, `hw4-4.ans`, `hw4-5.ans`

**Important**: To remind you, in order for your answers to be added to the git repo, 
you need to explicitly add each file:

```sh
$ git add *.ans
```

**Again, just because your code has been committed on your local machine does not mean that it has been 
submitted -- it needs to be on GitLab!**

Use the same bash script `turnInHw.sh` in the root level directory of your repository that 
commits your changes, deletes any prior tag for the current lab, tags the current commit,
and pushes the branch and tag to GitLab. 

If you are using Linux or Mac OSX, you should be able to run the following:

```sh
$ ./turnInHw.sh hw4
```

Like previous assignments, make sure you check the results afterwards to make sure that your file(s)
have been committed.
