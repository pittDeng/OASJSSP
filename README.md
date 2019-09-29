# OASJSSP

## dependency
+ cplex.jar
    + The direction is root\cplex\lib
    + click the project structure in the File menu
    + add dependency
## Introduction
 + current Problem
    + I don't know how to fix a max solving time limit so that the program can terminate before it finds the optimality.**What I figure out the way to terminate the program is to create a new thread as a timer. But I don't know how to record the result of the program in this case.**
    > the solution to the above question is set the DOUBLE_PARAM, the code is as follows 
     
        ```
            // you can replace any time(seconds) you want to set with 20.
            cplex.setParam(IloCplex.DoubleParam.TimeLimit,20);
        ```
    + I know users must define a class extends original Goal class to use the feature of goals. As for details, I don't pay much attention.
 
 Tips: When solving the model you just give, the cplex.solve() print some useful information.
 The explanations of the variables is as follows.
 + Best Integer: The best objective value with integer solution has been found so far
 + Best Bound: The best objective value has been found if the some constraints are relaxed.
 + Gap: $\frac{{Best Integer}-{Best Bound}}{Best Integer}$
 + IINF: the number of infeasible-integer variables.
 > The other columns are explained in [interpretation of the logs](https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.0/ilog.odms.cplex.help/CPLEX/UsrMan/topics/discr_optim/mip/para/52_node_log.html)

 