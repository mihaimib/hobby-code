;; The Knapsack Problem : Solution by Dynamic Programming

;; Suppose you've got a budget of twelve.
(def budget 12)

;; And some things
(def things (map (fn[[c v]] {:cost c :value v}) [[1 20][3 30][3 21][6 40]]))

(defn price [things]    (reduce + (map :cost  things)))
(defn evaluate [things] (reduce + (map :value things)))

(evaluate things) ;-> 111
(price things) ;-> 13

;; And you want to know, given your budget, how much of the value you can get.

;; The trick is to notice that there's a structure to this problem.

;; If (knapsack budget things) is the optimal purchase given all the things 
;; then we can say:

;; Should the (first things) be in the basket or not?

;; If it's not in the best basket, then the answer to 
;; (knapsack budget (rest things))
;; had better be the same answer, since if we're not going to buy it anyway, 
;; it shouldn't matter whether it's for sale or not.


;; But if we can afford the first thing, then we can imagine what
;; would happen if we bought it and then worked out what the best
;; thing we could do with the remaining money is.

;; That would be:
;; (knapsack (- budget (:cost (first things))) (rest things))

;; So there are only two possibilities, and if we know the answers to
;; the two simpler problems then we can work out what our answer is by
;; comparing the results of the possibilities.

;; So if we knew what the answers to the two easier problems were, then we could 
;; work out what the answer to our problem would be.

;; Of course, if we've no budget, or there are no things for sale,
;; then we just don't buy anything.

(defn ^:dynamic knapsack [budget things]
  (cond (zero? budget) '()
        (empty? things) '()
        :else (let [basket1 (knapsack budget (rest things))
                    cost (:cost (first things))]
                (if (> cost budget) 
                  basket1
                  (let [basket2 (cons (first things) 
                                      (knapsack (- budget cost) (rest things)))]
                    (if (> (evaluate basket1) (evaluate basket2))
                      basket1
                      basket2))))))
        

;; behold
(knapsack budget things) ; ({:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40})

;; and the maximal value is:
(evaluate (knapsack budget things)) ; -> 91

;; Which is the correct answer.

;; Now the eagle-eyed will notice that this is no better than
;; exhaustive search, since we make roughly two recursive calls per
;; available item, and so our time-to-solve is exponential:

(require 'clojure.tools.trace)
(clojure.tools.trace/dotrace [knapsack] (knapsack budget things))

;; TRACE t2106: (knapsack 12 ({:cost 1, :value 20} {:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2107: | (knapsack 12 ({:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2108: | | (knapsack 12 ({:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2109: | | | (knapsack 12 ({:cost 6, :value 40}))
;; TRACE t2110: | | | | (knapsack 12 ())
;; TRACE t2110: | | | | => ()
;; TRACE t2111: | | | | (knapsack 6 ())
;; TRACE t2111: | | | | => ()
;; TRACE t2109: | | | => ({:cost 6, :value 40})
;; TRACE t2112: | | | (knapsack 9 ({:cost 6, :value 40}))
;; TRACE t2113: | | | | (knapsack 9 ())
;; TRACE t2113: | | | | => ()
;; TRACE t2114: | | | | (knapsack 3 ())
;; TRACE t2114: | | | | => ()
;; TRACE t2112: | | | => ({:cost 6, :value 40})
;; TRACE t2108: | | => ({:cost 3, :value 21} {:cost 6, :value 40})
;; TRACE t2115: | | (knapsack 9 ({:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2116: | | | (knapsack 9 ({:cost 6, :value 40}))
;; TRACE t2117: | | | | (knapsack 9 ())
;; TRACE t2117: | | | | => ()
;; TRACE t2118: | | | | (knapsack 3 ())
;; TRACE t2118: | | | | => ()
;; TRACE t2116: | | | => ({:cost 6, :value 40})
;; TRACE t2119: | | | (knapsack 6 ({:cost 6, :value 40}))
;; TRACE t2120: | | | | (knapsack 6 ())
;; TRACE t2120: | | | | => ()
;; TRACE t2121: | | | | (knapsack 0 ())
;; TRACE t2121: | | | | => ()
;; TRACE t2119: | | | => ({:cost 6, :value 40})
;; TRACE t2115: | | => ({:cost 3, :value 21} {:cost 6, :value 40})
;; TRACE t2107: | => ({:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40})
;; TRACE t2122: | (knapsack 11 ({:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2123: | | (knapsack 11 ({:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2124: | | | (knapsack 11 ({:cost 6, :value 40}))
;; TRACE t2125: | | | | (knapsack 11 ())
;; TRACE t2125: | | | | => ()
;; TRACE t2126: | | | | (knapsack 5 ())
;; TRACE t2126: | | | | => ()
;; TRACE t2124: | | | => ({:cost 6, :value 40})
;; TRACE t2127: | | | (knapsack 8 ({:cost 6, :value 40}))
;; TRACE t2128: | | | | (knapsack 8 ())
;; TRACE t2128: | | | | => ()
;; TRACE t2129: | | | | (knapsack 2 ())
;; TRACE t2129: | | | | => ()
;; TRACE t2127: | | | => ({:cost 6, :value 40})
;; TRACE t2123: | | => ({:cost 3, :value 21} {:cost 6, :value 40})
;; TRACE t2130: | | (knapsack 8 ({:cost 3, :value 21} {:cost 6, :value 40}))
;; TRACE t2131: | | | (knapsack 8 ({:cost 6, :value 40}))
;; TRACE t2132: | | | | (knapsack 8 ())
;; TRACE t2132: | | | | => ()
;; TRACE t2133: | | | | (knapsack 2 ())
;; TRACE t2133: | | | | => ()
;; TRACE t2131: | | | => ({:cost 6, :value 40})
;; TRACE t2134: | | | (knapsack 5 ({:cost 6, :value 40}))
;; TRACE t2135: | | | | (knapsack 5 ())
;; TRACE t2135: | | | | => ()
;; TRACE t2134: | | | => ()
;; TRACE t2130: | | => ({:cost 6, :value 40})
;; TRACE t2122: | => ({:cost 3, :value 30} {:cost 6, :value 40})
;; TRACE t2106: => ({:cost 3, :value 30} {:cost 3, :value 21} {:cost 6, :value 40})


;; But it is also possible to notice that there are only so many
;; subproblems as there are different combinations of lessened budgets
;; and shortened item lists.

;; So with one bound

(def ^:dynamic knapsack (memoize knapsack))

;; We are free:

(clojure.tools.trace/dotrace [knapsack] (knapsack budget things))

;; What was a huge tree recursion is by memoization made equivalent to
;; filling out values in a two dimensional array whose size is
;; proportional to the budget and the number of items.

;; That makes these sorts of computations feasible:
(knapsack 2000 (take 100 (repeatedly (fn[] {:cost (inc (rand-int 10)) :value (inc (rand-int 10))}))))

;; Unfortunately, however, this seems surprisingly prone to blowing stack:
;; Which makes me think that I've got the algorithm wrong.

(knapsack 80 (take 80 (repeatedly (fn[] {:cost (inc (rand-int 10)) :value (inc (rand-int 10))}))))
;; StackOverflowError   clojure.lang.RT.boundedLength (RT.java:1654)

(def stuff (doall (take 80 (repeatedly (fn[] {:cost (inc (rand-int 10)) :value (inc (rand-int 10))})))))

(clojure.tools.trace/dotrace [knapsack] (knapsack 1 stuff))
;; Appears to call itself about thirty times and then explode




;; If we wanted to solve a big version of this problem, we'd have to make an explicit iterative algorithm
;; going through a two-dimensional array
