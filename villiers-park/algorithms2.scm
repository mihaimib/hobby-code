;; Algorithms

;; We've written some programs in Scheme.

;; Today I want to talk about the processes that programs create as they run.

;; I'm going to give you some little programs, and you're going to be the computer and execute them.

;; First of all, let's think about factorials.

;; How many ways are there to arrange 3 things?

;; abc acb bac bca cba cab. So six. Are we done?

;; Let's get rid of c

;; ab ab ba ba ba ab

;; Every way of arranging three things is a way of arranging two things, with c put in somewhere

;; ab -> cab, acb, abc
;; ba -> cba, bca, bac

;; So the number of ways you can arrange 3 things is 3 times the number of ways you can arrange 2 things. 
;; And the number of ways you can arrange 2 things is 2 times the number of ways you can arrange 1 thing.
;; And the number of ways you can arrange 1 thing is 1.

;; This number is called the factorial, and it comes up so often that it has its own bit of notation in maths.  
;; We usually write 3! for 3 factorial.

;; 1! = 1
;; n! = n * (n-1)!

;; We can take this mathematical definition and turn it directly into a program

(define (factorial n)
  (if (= n 1) 1
      (* n (factorial (- n 1)))))

(factorial 3)

;; Now suppose you're the computer, how do you execute that program?

(factorial 3)
(if (= 3 1) 1 (* 3 (factorial (- 3 1))))
(if #f 1 (* 3 (factorial (- 3 1))))
(* 3 (factorial (- 3 1)))
(* 3 (factorial 2))
(* 3 (if (= 2 1) 1 (* 2 (factorial (- 2 1)))))
(* 3 (if #f 1 (* 2 (factorial (- 2 1)))))
(* 3 (* 2 (factorial (- 2 1))))
(* 3 (* 2 (factorial 1)))
(* 3 (* 2 1))
(* 3 2)
6

;; 12 steps

;; Now let's get rid of the book-keeping in the middle, where we do the function call and the subtraction and the if, which is 
;; the same every time, and just keep the interesting bits.

(factorial 3)
(* 3 (factorial 2))
(* 3 (* 2 (factorial 1)))
(* 3 (* 2 1))
(* 3 2)
6

;; 6 steps

;; Notice how the program grows, and then when you get to (factorial 1) it bounces and starts to shrink until we're back to 1 number, which is the answer.

;; It's like it spends the first half its time planning a calculation, and the second half working it out.

;; Suppose we wanted (factorial 30), what would that look like?

;; (factorial 30)
;; ....
;; ....
;; (* 30 (* 29 (* 28 (* 27 (* .......                           (* 3 (* 2 (factorial 1))))))))
;; (* 30 (* 29 (* 28 (* 27 (* .......                           (* 3 (* 2 1)))))))
;; (* 30 (* 29 (* 28 (* 27 (* .......                           (* 3 2))))))
;; ....
;; ....
;; (* 30 8841761993739701954543616000000)
;; 265252859812191058636308480000000


;; It takes 30 steps to build up the sum, and 30 steps to collapse it down, 
;; and the longest line, in the middle, is 30 numbers long.

;; What about (factorial 300)?

;; A big number, that, but it only takes 100 times longer to compute it than it did (factorial 3), and in the middle, we need to store a big long string
;; of computations waiting to be done, but it's only 100 times longer than the string of stuff for (factorial 3)

;; We call this a linear recursion.

;; Generally, we ignore the details about whether it's n steps, or 7*n steps, because how many steps it is depends 
;; on how you count them, and how long each step takes depends what sort of computer the program is running on.

;; So we tend to think that if the time taken is 15*n, then the 15 is sort of a fiddly detail and the n is the important bit!

;; Normally that's ok, because the difference between 15 milliseconds and 1 millisecond is not important, and neither is the difference between a million years and 15 million years.

;; We say that we need O(n) time, and O(n) space.

;; Occasionally, you'll fall into the annoying zone where it's the difference between 1 day and two weeks. 
;; At that point you do need to start caring about constant factors. But that's surprisingly rare.

;; At any rate, with an O(n) algorithm, our problem is surprisingly tractable, and we can find quite large factorials.

;; =========================================================================================


;; Here's a different way of computing the same function

(define (factorial n) (fact-iter n 1))

(define (fact-iter n total)
  (if (= n 0) total
      (fact-iter (- n 1) (* n total))))

(factorial 3)

;; We get the same answers, what does the computation look like?

(factorial 3)
(fact-iter 3 1)
(fact-iter 2 3)
(fact-iter 1 6)
(fact-iter 0 6)
6

;; What would (factorial 30) look like now?

(factorial 30)
(fact-iter 30 1)
(fact-iter 29 30)
(fact-iter 28 870)
(fact-iter 27 24360)
(fact-iter 26 657720)
;;....
;;....
(fact-iter 0 265252859812191058636308480000000)
265252859812191058636308480000000

;; We're no longer using up a lot of space storing the computations we intend to do.
;; At every step, we need only store two numbers, our counter, and our total

;; This pattern is called a linear iteration, and it's still O(n) in time, but it's O(1) in space.

;; With an algorithm like this, it may take a long time to do the computation, but we don't really have to worry about running out of memory.

;; With the first process, we might set off a calculation that we expect to take hours, and come back later to 
;; find that the whole thing had failed because the computer had run out of space to store its computation.

;; Notice that in both cases, the programs are 'recursive' in the sense that the procedures call themselves.

;; What makes the difference is the shape that the programs make as they run.








