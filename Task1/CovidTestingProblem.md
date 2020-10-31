## Implementing an optimization"

We describe our example problem which is implemented in Task 1.

### Covid Test Distribution

Lately I got a Covid19 test. The test probe was sent in a lab 330 km away, and it took 4 days until the result was available.
In the optimization problem we want to simulate the distribution of test probes to distributed labs.

#### Scenario

Let's assume we are sending probes from all 9 federal states in Austria. Each state has the following amount of probes.

- Wien 31.602
- Oberösterreich 12.526
- Niederösterreich 12.194
- Tirol 10.684
- Steiermark 6.440
- Salzburg 4.876
- Vorarlberg 3.409
- Kärnten 1.877
- Burgenland 1.625


We have **3** big test labs.

- Wien
- Salzburg
- Innsbruck

Sending the probes to a lab takes the following time in days.

| State / Lab      | Wien | Innsbruck | Salzburg |
|------------------|:----:|:---------:|:--------:|
| Wien             |   0  |     4     |     3    |
| Oberösterreich   |   2  |     2     |     1    |
| Niederösterreich |   1  |     3     |     2    |
| Tirol            |   4  |     0     |     1    |
| Steiermark       |   2  |     2     |     1    |
| Salzburg         |   3  |     1     |     0    |
| Vorarlberg       |   5  |     1     |     1    |
| Kärnten          |   3  |     2     |     1    |
| Burgenland       |   2  |     3     |     2    |

#### Question

For our first iteration, we assume **every lab has unlimited test capacity**.

How can we distribute the probes to minimize the shipping time?