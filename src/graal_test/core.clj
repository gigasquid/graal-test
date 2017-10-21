(ns graal-test.core
  (:import (org.graalvm.polyglot Context)))

;; note that is also supports Ruby, LLVM, and JS
(def context (Context/create (into-array ["python" "R"])))


;;;;;;;;;;; INTEROP WITH R ;;;;;;;;;;;;;;;;;;

(.eval context "R" "
3^2 + 2^2
")
;=> #object[org.graalvm.polyglot.Value 0x7ff40e4d "13.0"]

(def result1 (.eval context "R" "
sum.of.squares <- function(x,y) {
  x^2 + y^2
}
sum.of.squares(3,4)
"))

(defn ->clojure [polyglot-value]
  (-> polyglot-value
      (.toString)
      (clojure.edn/read-string)))

(->clojure result1) ;=> 25

(.eval context "R" "
install.packages(\"numDeriv\", repos = \"http://cran.case.edu/\")
")

(def result2 (.eval context "R" "
library(numDeriv)
grad(sin, (0:10)*2*pi/10)
"))
result2 ;=> #object[org.graalvm.polyglot.Value 0x76765898 "c(1,
        ;0.809016994367249, 0.309016994372158, -0.309016994373567,
        ;-0.809016994368844, -0.999999999993381, -0.809016994370298,
        ;-0.309016994373312, 0.309016994372042, 0.809016994369185,
        ;0.999999999993381)"]

(.hasArrayElements result2) ;=> true
(.getArraySize result2) ;=> 11

(for [i (range 10)]
  (-> (.getArrayElement result2 i) (->clojure)))
;=> (1.0 0.8090169943672489 0.3090169943721585 -0.3090169943735675
;-0.8090169943688436 -0.9999999999933814
; -0.8090169943702977 -0.3090169943733122 0.30901699437204233
; 0.8090169943691851)


;;;;;;;;;;; INTEROP WITH PYTHON ;;;;;;;;;;;;;;;;;;
;; Note Python dev is very early and expected to develop in 2018


(.eval context "python" "
import time;
time.clock()
")
 ;=> #object[org.graalvm.polyglot.Value 0x4a6b3b70 "1.508202803249E9"]


(comment
;;; This is what I would love to do one day!
  (.eval py-context "python" "
import tensorflow as tf
hello = tf.constant('Hello, TensorFlow!')
sess = tf.Session()
print(sess.run(hello))
"))
