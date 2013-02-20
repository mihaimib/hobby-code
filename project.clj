(defproject hobby-code "0.0.1"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.trace "0.7.5"]
                 [com.cemerick/pomegranate "0.0.13"]]

;; The magic words are:
;; (require 'cemerick.pomegranate)
;; (cemerick.pomegranate/add-dependencies 
;;  :coordinates '[[ring "1.1.7"]]
;;  :repositories {"clojars" "http://clojars.org/repo" } )

;; (cemerick.pomegranate/add-dependencies :coordinates '[[org.clojure/tools.trace "0.7.5"]])

  :repl-options { 
                 :port 4001 
                 :init (do (println "hello from hobby-code/project.clj")
                           (defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

                           (defmacro def-let
                             "like let, but binds the expressions globally."
                             [bindings & more]
                             (let [let-expr (macroexpand `(let ~bindings))
                                   names-values (partition 2 (second let-expr))
                                   defs   (map #(cons 'def %) names-values)]
                               (concat (list 'do) defs more)))
                           (println "defined user/def-let and user/dbg"))
                 }
  :source-paths ["."]
  :min-lein-version "2.0.0"
)

