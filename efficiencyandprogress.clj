;; Efficiency and Progress
;; Are ours once again
;; Now that we have the neut-ron bomb
;; It's nice and quick and clean and ge-ets things done...

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; When you program in Clojure, you get the raw speed of assembler.

;; Unfortunately, that is, assembler on a ZX81, running a Z80 processor at 4MHz in 1981.

;; If anything, that comparison is unfair to my old ZX81. Does anyone
;; remember '3D Invaders', a fast and exciting first person shooter /
;; flight simulator that ran in 1K of RAM *including memory for the
;; screen*?

;; Once upon a time, I had the knack of making clojure run at the same
;; speed as Java, which is not far off the same speed as C, which is
;; not far off the speed of the sort of hand-crafted machine code which
;; no-one in their right mind ever writes, in these degenerate latter
;; days which we must reluctantly learn to call the future.

;; But I seem to have lost the knack. Can anyone show me what I am doing wrong?

;; At any rate, it isn't too hard to get it to run at something like
;; the real speed of the machine, as long as you're prepared to write
;; code that is more like Java or C than Clojure.

;; So here are some thoughts about how to do this.

;; Which I offer up only as a basis for discussion, and not in any way
;; meaning to stir up controversy, or as flame-bait or ammunition for
;; trolls or anything of that sort.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Clojure is very slow:

(time (reduce + (map + (range 1000000) (range 1000000))))
"Elapsed time: 5316.638869 msecs"
;-> 999999000000

;; The greater part of its slowness seems to be do with lazy sequences

(time (def seqa (doall (range 1000000))))
"Elapsed time: 3119.468963 msecs"
(time (def seqb (doall (range 1000000))))
"Elapsed time: 2839.593429 msecs"

(time (reduce + (map + seqa seqb)))
"Elapsed time: 3558.975552 msecs"
;-> 999999000000

;; It looks as though making a new sequence is the expensive bit
(time (doall (map + seqa seqb)))
"Elapsed time: 3612.553803 msecs"
;-> (0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 ...)

;; Just adding things up is way faster
(time (reduce + seqa))
"Elapsed time: 315.717033 msecs"
499999500000


;; I wondered if there was a way of avoiding lazy-seqs

(time (def veca (vec seqa)))
"Elapsed time: 470.512696 msecs"

(time (def vecb (vec seqb)))
"Elapsed time: 374.796054 msecs"

;; After all, 'use the right data structure for the problem' is pretty much lesson 1, and if vectors are not a good data structure
;; for this problem, then what is?

;; But it seems that despite the speed of making the vectors, it doesn't help much when we do our thing.
;; In fact it's a bit slower
(time (reduce + (mapv + veca vecb)))
"Elapsed time: 4329.070268 msecs"
999999000000

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; So lets say 3600ms to add together two arrays of 1000000 elements and sum the result.

;; In C on the same machine (my little netbook with its 1.66GHz Atom
;; and 512kb cache) this seems to take 16 ms, being 8ms for the map
;; and 8 ms for the reduce.  I'm assuming that that time is mostly
;; spent waiting on the main memory, but I may be wrong. Who knows how
;; these things are done?

(/ 3600 16) ;-> 225

;; So shall we call this a 225x slowdown for the natural expression in
;; the two languages of mapping and reducing?

(time (reduce + seqa))
"Elapsed time: 358.152249 msecs"
499999500000

;; If we just look at the reduction, then that's 
(/ 358 8.) ; 44.75


;; So around 50x


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(time (doall (map + (range 1000000) (range 1000000))))
"Elapsed time: 8109.315787 msecs"
(0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 ...)

(time (reduce + (range 1000000)))
"Elapsed time: 1261.971232 msecs"
499999500000

;; The greater part of its slowness is to do with lazy sequences

(time (def seqa (doall (range 1000000))))
"Elapsed time: 3119.468963 msecs"
(time (def seqb (doall (range 1000000))))
"Elapsed time: 2839.593429 msecs"

;; Actually adding things up is not so bad

(time (reduce + seqa))
"Elapsed time: 427.602351 msecs"
499999500000

;; Although if we want to keep the results

(time (doall (map + seqa seqb)))
"Elapsed time: 5330.860859 msecs"
(0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 ...)

;; Then we end up generating lazy sequences anyway

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; We can make java arrays though

(time (def a (int-array (range 1000000))))
"Elapsed time: 2004.260737 msecs"
(time (def b (int-array (range 1000000))))
"Elapsed time: 1563.989388 msecs"

;; And can operate on them
(defn asum [^ints xs]
  (areduce xs i ret (int 0)
    (+ ret (aget xs i))))

;; rather more quickly
(time (asum a))
"Elapsed time: 85.445153 msecs" ;; 52 with *unchecked-math*
499999500000

;; It turns out that areduce is a macro
(macroexpand '(areduce xs i ret (int 0)
                       (+ ret (aget xs i))))

;; And so we can reproduce its magic
(defn my-asum [^ints xs]
  (let* [a__4717__auto__ xs] 
        (clojure.core/loop [i 0 ret (int 0)] 
          (if (clojure.core/< i (clojure.core/alength a__4717__auto__))
            (recur (clojure.core/unchecked-inc i) 
                   (+ ret (aget xs i))) ret))))

;; And in fact slightly improve it
(defn my-asum [^ints xs]
  (let [N (clojure.core/alength xs)]
    (clojure.core/loop [i 0 ret (int 0)] 
      (if (clojure.core/< i N)
        (recur (clojure.core/unchecked-inc i) 
               (+ ret (aget xs i))) ret))))


(time (my-asum a))
"Elapsed time: 73.69296 msecs" ;; 44 with *unchecked-math*
499999500000

;; This just makes it a spot more readable
(defn my-asum [^ints xs]
  (let [N (alength xs)]
    (loop [i 0 ret 0] 
      (if (< i N)
        (recur (unchecked-inc i) (+ ret (aget xs i))) 
        ret))))

(time (my-asum a))
"Elapsed time: 73.981615 msecs" ;; 44
499999500000


;; What about adding two vectors?

;; Here's a truly horrid way to do it

(time (def c (int-array 1000000)))
"Elapsed time: 4.698991 msecs"

(aget c 0) ; 0
(aget c 999999) ; 0

(defn my-amap [^ints xs ^ints ys ^ints cs]
  (let [N (dec (clojure.core/alength xs))]
    (loop [i 0]
      (aset cs i (+ (aget xs i) (aget ys i)))
      (if (< i N) (recur (unchecked-inc i))))))

(time (my-amap a b c))
"Elapsed time: 188.844755 msecs" ;; 77 with *unchecked-math*

(aget c 0) ;-> 0
(aget c 999999) ;-> 1999998

;; Better perhaps is:

(defn my-amap [^ints xs ^ints ys]
  (let [N (dec (clojure.core/alength xs))
        c (int-array (clojure.core/alength xs))]
    (loop [i 0]
      (aset c i (+ (aget xs i) (aget ys i)))
      (if (< i N) 
        (recur (unchecked-inc i))
        c))))

;; Which, thank the Lord Harry, is not particularly slower

(def c (time (my-amap a b)))
"Elapsed time: 192.613251 msecs" ;; 77 with *unchecked math*

(aget c 0) ;-> 0
(aget c 999999) ;-> 1999998

;; This is even nicer I think
(defn my-amap [^ints xs ^ints ys]
  (let [c (int-array (clojure.core/alength xs))]
    (dotimes[i (clojure.core/alength xs)]
      (aset c i (+ (aget xs i) (aget ys i))))
    c))

(def c (time (my-amap a b)))
"Elapsed time: 190.621796 msecs" ;; 74

(aget c 0) ;-> 0
(aget c 999999) ;-> 1999998

;; It actually speeds up a bit with longs

(time (def a (long-array (range 1000000))))
"Elapsed time: 2004.260737 msecs"
(time (def b (long-array (range 1000000))))
"Elapsed time: 1563.989388 msecs"

(defn my-amap [^longs xs ^longs ys]
  (let [c (long-array (clojure.core/alength xs))]
    (dotimes[i (clojure.core/alength xs)]
      (aset c i (+ (aget xs i) (aget ys i))))
    c))

(def c (time (my-amap a b))) 
"Elapsed time: 172.431145 msecs" ;; 88 with *unchecked-math*, so that's a slowdown

(aget c 0) ;-> 0
(aget c 999999) ;-> 1999998

;; this is my best shot at adding up two arrays


;; trying the reduce with longs too
(defn my-asum [^longs xs]
  (let [N (alength xs)]
    (loop [i 0 ret 0] 
      (if (< i N)
        (recur (unchecked-inc i) (+ ret (aget xs i))) 
        ret))))

;; Doesn't seem enormously faster, but it's not slower, at least.

(time (my-asum a))
"Elapsed time: 72.29411 msecs" ;; 44 with *unchecked-math*
499999500000



;; 100 maps and 100 reduces take 25 seconds on my machine
(time (dotimes [i 100] 
        (my-amap a b)
        (my-asum a)))
"Elapsed time: 24742.951321 msecs" ;; 12040 with *unchecked-math*

;; So from our original ten seconds, we're down to 0.25 seconds, a speed up of 40x

;; But we're still 15 times slower than C.

;; The corresponding C program takes 1.7 seconds on this machine (and java about half that speed)

(/ 25 1.7) ;-> 14.705882352941178

;; I can live with that, but once upon a time, I could make clojure run at
;; the same speed as java, and that was nice.

;; It's possible that Java and C are both doing some sort of dead code elimination and 
;; so looking artificially fast.

;; This doubles the speed of everything. sigh.
(set! *unchecked-math* true)

(/ 12 1.7) ;-> 7.0588235294117645 

;; 7x slower than C, and 1/3 the speed of java itself



