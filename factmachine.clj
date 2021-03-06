;; A register machine for factorial

(defn fact [n]
  (if (= n 1)
    1
    (* n (fact (- n 1)))))

(fact 10) ; 3628800

;; We can't get away with just data paths and a finite-state controller for factorial
;; We also need a stack

 
;; In the data path we need
;; registers value and n
;; a way of putting the value of n into value
;; a way of telling if n is 1
;; a way of decrementing n
;; a way of multiplying n and value and putting the result into n

;; And we need a stack, let us say that it is twenty registers
;; There needs to be a way of putting n onto the stack

;; And we need a place to note states of the state machine,
;; This continue register needs to be able to store two states AFT and DONE

:begin
(assign continue :done)
:loop
(branch (= 1 (fetch n)) :base)
(save continue)
(save n)
(assign n (dec (fetch n)))
(assign continue :aft)
(goto :loop)
:aft
(restore n)
(restore continue)
(assign value (* (fetch n) (fetch value)))
(goto (fetch continue))
:base
(assign value (fetch n))
(goto (fetch continue))
:done

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn operation [op a b]
  (cond (= op '*) (* a b)
        (= op '>) (> a b)
        (= op 'inc) (inc a)))
  

(defn step [{:keys [pc state controller]}]
  (if (>= pc (count controller)) {:pc pc :state state :controller controller} ;; if the program counter goes off the end, just cycle
      (let [npc (inc pc)                 ;; increment the program counter
            instruction (controller pc)] ;; look up the next instruction
        (cond
          ;; jump over labels
          (keyword? instruction) 
          {:pc npc :state state :controller controller}
          ;; assignment
          (= (first instruction) 'assign) 
          (let [var (second instruction)
                arg (nth instruction 2)
                val (cond
                      ;; immediate values
                      (or (number? arg) (keyword? arg)) arg
                      ;; registers
                      (symbol? arg) (state arg) 
                      ;; operations on registers
                      :else
                      (let [[op val1 val2] arg]
                        (operation op (state val1) (state val2))))]
          {:pc npc :state (assoc state var val ) :controller controller})
          ;; goto
          (= (first instruction) 'goto)
           {:pc (.indexOf controller (second instruction)) :state state :controller controller}
          ;; branch
          (= (first instruction) 'branch)
          (let [[op val1 val2] (second instruction)
                label (nth instruction 2)]
            (if (operation op (state val1) (state val2))
              {:pc (.indexOf controller label) :state state :controller controller}
              {:pc npc :state state :controller controller}))))))


(def basemachine {:pc 0  :state {} :controller '[]})
(def loopmachine (assoc basemachine :controller '[:begin (goto :begin)]))
(def assignmachine (assoc basemachine :controller '[(assign val 10)]))

(list
 (= (step basemachine) basemachine)
 ;; labels and gotos
 (= (step loopmachine) (assoc loopmachine :pc 1))
 (= (step (step loopmachine)) loopmachine)
 ;; assignment
 (= (step assignmachine)
    (assoc assignmachine
           :state '{val 10}
           :pc 1))
 (= (step {:pc 0 :state '{} :controller '[(assign val :keyword)]})
          {:pc 1, :state '{val :keyword}, :controller '[(assign val :keyword)]})
 (= (step {:pc 0 :state '{doom 1} :controller '[(assign val doom)]})
          '{:pc 1, :state {val 1, doom 1}, :controller [(assign val doom)]})
 (= (step {:pc 0 :state '{a 3 b 7} :controller '[(assign val (* a b))]})
          '{:pc 1, :state {val 21, a 3, b 7}, :controller [(assign val (* a b))]})
 ;; branch
 (= (step {:pc 1 :state '{a 1 b 2} :controller '[:begin (branch (> a b) :begin)]}) '{:pc 2, :state {a 1, b 2}, :controller [:begin (branch (> a b) :begin)]})
 (= (step {:pc 1 :state '{a 2 b 1} :controller '[:begin (branch (> a b) :begin)]}) '{:pc 0, :state {a 2, b 1}, :controller [:begin (branch (> a b) :begin)]})
)


(def if {:state
         '{n       0
           product 0
           counter 0}
         :controller
         '[(assign product 1)                     ;0
           (assign counter 1)                     ;1
           :loop                                  ;2
           (branch (> counter n) :done)           ;3
           (assign product (* counter product))   ;4
           (assign counter (inc counter))         ;5
           (goto :loop)                           ;6
           :done                                  ;7 
           (assign n product)]                    ;8
         :pc 0})                  

(def ifrun (iterate step if))

(map :pc ifrun) ; (0 1 2 3 7 8 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 ...)
(take 6 (map (juxt #((:controller %) (:pc %)) :state) ifrun))

([(assign product 1)           {n 0, product 0, counter 0}]
 [(assign counter 1)           {n 0, product 1, counter 0}]
 [:loop                        {n 0, product 1, counter 1}]
 [(branch (> counter n) :done) {n 0, product 1, counter 1}]
 [:done                        {n 0, product 1, counter 1}]
 [(assign n product)           {n 0, product 1, counter 1}]
 )

([0 {n 0, product 0, counter 0}]
 [1 {n 0, product 1, counter 0}]
 [2 {n 0, product 1, counter 1}]
 [3 {n 0, product 1, counter 1}]
 [7 {n 0, product 1, counter 1}]
 [8 {n 0, product 1, counter 1}]
 [9 {n 1, product 1, counter 1}])

(def rf '{:pc 0
          :state {n 0}
          :controller
  [:begin
    (assign continue :done)
    :loop
    (branch (= 1 (fetch n)) :base)
    (save continue)
    (save n)
    (assign n (dec (fetch n)))
    (assign continue :aft)
    (goto :loop)
    :aft
    (restore n)
    (restore continue)
    (assign value (* (fetch n) (fetch value)))
    (goto (fetch continue))
    :base
    (assign value (fetch n))
    (goto (fetch continue))
    :done]})

(:state (step rf)) ; {n 0}
(iterate step rf)



 
;; 
(def value      (ref 0)) ; #'user/value
(def n          (ref 0)) ; #'user/n
(def continue   (ref 0)) ; #'user/continue
(def stack      (ref (list))) ; #'user/stack
(defn dump [] {:value @value :n @n :continue @continue :stack @stack}) ; #'user/dump

(defn move [a b] (dosync (alter a (fn[_] @b)))(dump)) ; #'user/move
(defn assign [v n] (dosync (alter v (fn[_] n))) (dump)) ; #'user/assign
(defn fetch [v] @v) ; #'user/fetch
(defn goto [label] (str "GOTO " label)) ; #'user/goto
(defn save [x] (dosync (alter stack (fn[_] (cons @x _ )))) (dump)) ; #'user/save
(defn restore [v] (dosync (let [a (first @stack)] (alter v (fn[_] a)) (alter stack rest) a)) (dump)) ; #'user/restore
(defn is? [v n] (= @v n)) ; #'user/is?

(defn clearall [] (assign value 0) (assign n 0) (assign continue 'done) (assign stack '()) (dump)) ; #'user/clearall



(clearall)
(dump)
(assign value 10)
(dump)
(save value)
(dump)
(assign value 11)
(dump)
(move n value)
(dump)
(restore value)
(dump)

;; Calculating the factorial of three
(clearall) ; {:value 0, :n 0, :continue done, :stack ()}
(assign n 3) ; {:value 0, :n 3, :continue done, :stack ()}
;; BEGIN
(assign continue 'DONE) ; {:value 0, :n 3, :continue DONE, :stack ()}
;; LOOP 
(is? n 1) ; false
(save continue) ; {:value 0, :n 3, :continue DONE, :stack (DONE)}
(save n) ; {:value 0, :n 3, :continue DONE, :stack (3 DONE)}
(assign n (dec (fetch n))) ; {:value 0, :n 2, :continue DONE, :stack (3 DONE)}
(assign continue 'AFT) ; {:value 0, :n 2, :continue AFT, :stack (3 DONE)}
(goto 'LOOP) ; "GOTO LOOP"
(is? n 1) ; false
(save continue) ; {:value 0, :n 2, :continue AFT, :stack (AFT 3 DONE)}
(save n) ; {:value 0, :n 2, :continue AFT, :stack (2 AFT 3 DONE)}
(assign n (dec (fetch n))) ; {:value 0, :n 1, :continue AFT, :stack (2 AFT 3 DONE)}
(assign continue 'AFT) ; {:value 0, :n 1, :continue AFT, :stack (2 AFT 3 DONE)}
(goto 'LOOP) ; "GOTO LOOP"
(is? n 1) ; true
;;goto BASE
(assign value (fetch n)) ; {:value 1, :n 1, :continue AFT, :stack (2 AFT 3 DONE)}
(goto (fetch continue)) ; "GOTO AFT"
;; goto AFT
(restore n) ; {:value 1, :n 2, :continue AFT, :stack (AFT 3 DONE)}
(restore continue) ; {:value 1, :n 2, :continue AFT, :stack (3 DONE)}
(assign value (* (fetch n) (fetch value))) ; {:value 2, :n 2, :continue AFT, :stack (3 DONE)}
(fetch continue) ; AFT
;; goto AFT
(restore n) ; {:value 2, :n 3, :continue AFT, :stack (DONE)}
(restore continue) ; {:value 2, :n 3, :continue DONE, :stack ()}
(assign value (* (fetch n) (fetch value))) ; {:value 6, :n 3, :continue DONE, :stack ()}
(goto (fetch continue)) ; "GOTO DONE"
;; goto DONE
(fetch value) ; 6






