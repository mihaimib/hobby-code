;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Clojure Macro Tutorial Part III: Syntax Quote
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Let's finally look at how we'd go about writing forloop the correct way,
;; using the built in syntax-quote method.

;; The problem is:

;; We want:
'(forloop [i end]
          code)

;; to turn into:
'(let [finish end]
   (loop [i 0]
     (when (< i finish)
       (print i)
       (recur (inc i)))))

;; First Step

;; Just cut and paste the desired code, and backquote it:
;; This gives us a function which will always return the same  code.

(defn forloop-fn-1 []
  `(let [finish end]
     (loop [i 0]
       (when (< i finish)
         (print i)
         (recur (inc i))))))

;; What does forloop-fn give us?

(forloop-fn-1)
;;evaluates to:
'(clojure.core/let [user/finish user/end]
                   (clojure.core/loop [user/i 0]
                                      (clojure.core/when (clojure.core/< user/i user/finish)
                                                         (clojure.core/print user/i)
                                                         (recur (clojure.core/inc user/i)))))

;; This has done all the name resolution (the really ugly bit) for us! Otherwise it's just like quote.

;; But if we try evaluating this code, we'll get an error:
;; Can't let qualified name: user/finish

;; The problem is that user/finish isn't a thing that you can put in a let.
;; finish is the macro-local variable that we wanted to use a gensym for.

;; So we use the auto-gensym feature:
;; We add # to all occurences of finish, meaning use a gensym here.

(defn forloop-fn-2 []
  `(let [finish# end]
     (loop [i 0]
       (when (< i finish#)
         (print i)
         (recur (inc i))))))

;; Again we'll evaluate:
(forloop-fn-2)
;; to get:
'(clojure.core/let [finish__2254__auto__ user/end]
                   (clojure.core/loop [user/i 0]
                                      (clojure.core/when (clojure.core/< user/i finish__2254__auto__)
                                                         (clojure.core/print user/i)
                                                         (recur (clojure.core/inc user/i)))))

;; This first problem with this code is that it expects a variable user/end to be defined.

;; But actually, end is one of the things that we want to pass into the function.
;; We'll make it an argument of the macro, and use the unquote operator ~ to tell the function

(defn forloop-fn-3 [end]
  `(let [finish# ~end]
     (loop [i 0]
       (when (< i finish#)
         (print i)
         (recur (inc i))))))

;; Now let's evaluate:
(forloop-fn-3 10)
;; to get:
'(clojure.core/let [finish__2276__auto__ 10]
                   (clojure.core/loop [user/i 0]
                                      (clojure.core/when (clojure.core/< user/i finish__2276__auto__)
                                                         (clojure.core/print user/i)
                                                         (recur (clojure.core/inc user/i)))))

;; Looking good so far! If we try to evaluate this code, though, it objects to the fact that user/i doesn't exist.
;; We can fix that in the same manner:

(defn forloop-fn-4 [i end]
  `(let [finish# ~end]
     (loop [~i 0]
       (when (< ~i finish#)
         (print ~i)
         (recur (inc ~i))))))

(forloop-fn-4 'j 10) 
;; ->
(clojure.core/let [finish__2298__auto__ 10]
                  (clojure.core/loop [j 0]
                                     (clojure.core/when (clojure.core/< j finish__2298__auto__)
                                                        (clojure.core/print j)
                                                        (recur (clojure.core/inc j)))))

;; (Notice that we have to quote j, because forloop-4-fn is a function, so its
;; arguments get evaluated before it is called)

;; And this code is actually executable! Try evaluating it directly, or use eval
;; to evaluate the return value of the function:

(eval (forloop-fn-4 'j 10))
;; 0123456789nil
(eval (forloop-fn-4 'loop-variable 15))
;; 01234567891011121314nil

;; So we've got a function that will give us code that will print out different
;; length runs of numbers, using the loop variable of our choice.

;; Of course to make it useful, we've got to make the (print i) bit a variable as well.
;; We could use the unquoting mechanism here too:
(defn forloop-fn-5 [i end code]
  `(let [finish# ~end]
     (loop [~i 0]
       (when (< ~i finish#)
         ~code
         (recur (inc ~i))))))

;;Evaluate:
(forloop-fn-5 'i 10 '(print (* i i)))
;;To get:
(clojure.core/let [finish__2335__auto__ 10]
                  (clojure.core/loop [i 0]
                                     (clojure.core/when (clojure.core/< i finish__2335__auto__)
                                                        (print (* i i))
                                                        (recur (clojure.core/inc i)))))
;; Evaluate that:
(eval (forloop-fn-5 'i 10 '(print (* i i))))
;; 0149162536496481nil

;; But in fact, forloop would be much more useful if we were allowed an
;; unlimited number of expressions in our loop code, So we make the function
;; accept a variable number of arguments.  Because the 3rd, 4th, 5th etc
;; arguments are now made into the list "code", we need to use ~@, the
;; unquote-splicing operator to insert them without their outer brackets.
(defn forloop-fn-6 [i end & code]
  `(let [finish# ~end]
     (loop [~i 0]
       (when (< ~i finish#)
         ~@code
         (recur (inc ~i))))))


(eval (forloop-fn-6 'i 10 '(print i) '(print (* i i))))
;;00112439416525636749864981nil

;; Can you not sense the imminent success?

;; We remember that we wanted (forloop [i 10] code) rather than (forloop i 10
;; code), so we use the destructuring notation for the first two arguments
(defn forloop-fn-7 [[i end] & code]
  `(let [finish# ~end]
     (loop [~i 0]
       (when (< ~i finish#)
         ~@code
         (recur (inc ~i))))))

(eval (forloop-fn-7 '[i 10] '(print i) '(print (* i i))))
;;00112439416525636749864981nil

;; And finally, we change defn to defmacro, so we can dispense with the quotes and the eval

(defmacro forloop [[i end] & code]
  `(let [finish# ~end]
     (loop [~i 0]
       (when (< ~i finish#)
         ~@code
         (recur (inc ~i))))))

(forloop [i 10]
         (print i)
         (print (* i i)))

;;00112439416525636749864981nil

;; And we're done.

;; Let's look at the code our macro makes for us:

(macroexpand-1 '(forloop [i 10]
         (print i)
         (print (* i i))))

(clojure.core/let [finish__2442__auto__ 10]
                  (clojure.core/loop [i 0]
                                     (clojure.core/when (clojure.core/< i finish__2442__auto__)
                                                        (print i)
                                                        (print (* i i))
                                                        (recur (clojure.core/inc i)))))

;; All names resolved to the namespaces that they would have resolved to at the time the macro was defined.
;; Gensyms done automatically, with readable silly names.

;; Bombproof, surprisingly straightforward to write, and the finished macro
;; looks awfully like the code it's trying to generate.

;; And that's how you really write a macro in clojure. Hacking it together by
;; hand is error prone for the reasons given above, and much harder.










































 














