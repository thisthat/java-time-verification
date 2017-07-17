; definition std
(declare-fun max_val () Int)
(declare-fun over_max_val () Int)
(declare-fun min_val () Int)
(declare-fun time () Int)
(declare-fun now () Int)
(declare-fun deadline () Int)
(declare-fun timeout () Int)

(assert (= max_val 9223372036854775807))
(assert (= over_max_val 9223372036854775808))
(assert (= min_val (- 1)))
(assert (= time 0))

(assert (<= min_val now))
(assert (<= now over_max_val))
(assert (= now time))

(assert (<= min_val deadline))
(assert (<= deadline over_max_val))
(assert (<= min_val timeout))
(assert (<= timeout over_max_val))

(assert (= deadline (+ now timeout)))

(assert (<= now deadline))

(assert (= now time))


(check-sat)

